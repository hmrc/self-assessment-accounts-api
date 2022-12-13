/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package routing

import akka.actor.ActorSystem
import api.models.errors.{InvalidAcceptHeaderError, NotFoundError, UnsupportedVersionError}
import com.typesafe.config.{Config, ConfigFactory}
import mocks.MockAppConfig
import org.scalatest.Inside
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.http.HeaderNames.ACCEPT
import play.api.http.{HttpConfiguration, HttpErrorHandler, HttpFilters}
import play.api.libs.json.Json
import play.api.mvc._
import play.api.routing.Router
import play.api.test.FakeRequest
import play.api.test.Helpers._
import support.UnitSpec

class VersionRoutingRequestHandlerSpec extends UnitSpec with Inside with MockAppConfig with GuiceOneAppPerSuite {
  test =>
  implicit private val actorSystem: ActorSystem = ActorSystem("test")
  val action: DefaultActionBuilder              = app.injector.instanceOf[DefaultActionBuilder]

  import play.api.mvc.Handler
  import play.api.routing.sird._

  object DefaultHandler extends Handler
  object V1Handler      extends Handler
  object V2Handler      extends Handler

  private val defaultRouter = Router.from { case GET(p"") =>
    DefaultHandler
  }

  private val v1Router = Router.from {
    case GET(p"/oldResource")           => V1Handler
    case GET(p"/x/collection/tax-code") => V1Handler
  }

  private val v2Router = Router.from { case GET(p"/resource") =>
    V2Handler
  }

  private val routingMap = new VersionRoutingMap {
    override val defaultRouter: Router = test.defaultRouter

    override val map: Map[Version, Router] = Map(Version2 -> v2Router, Version1 -> v1Router)
  }

  private val confWithAllEnabled: Config = ConfigFactory.parseString("""
                                                                       |version-2.enabled = true
    """.stripMargin)

  private val confWithV2Disabled: Config = ConfigFactory.parseString("""
                                                                       |version-1.enabled = true
    """.stripMargin)

  private val confWithV1DisabledV2Enabled: Config = ConfigFactory.parseString("""
   version-2.enabled = true
    """.stripMargin)

  class Test(implicit acceptHeader: Option[String], conf: Config) {
    val httpConfiguration: HttpConfiguration = HttpConfiguration("context")
    private val errorHandler                 = mock[HttpErrorHandler]
    private val filters                      = mock[HttpFilters]
    (() => filters.filters).stubs().returns(Nil)

    MockAppConfig.featureSwitches.returns(Configuration(conf))

    lazy val requestHandler: VersionRoutingRequestHandler =
      new VersionRoutingRequestHandler(routingMap, errorHandler, httpConfiguration, mockAppConfig, filters, action)

    def buildRequest(path: String): RequestHeader =
      acceptHeader
        .foldLeft(FakeRequest("GET", path)) { (req, accept) =>
          req.withHeaders((ACCEPT, accept))
        }

  }

  "Routing requests with no version" should {
    implicit val acceptHeader: None.type = None
    handleWithDefaultRoutes()
  }

  "Routing requests with any enabled version" should {
    implicit val acceptHeader: Option[String] = Some("application/vnd.hmrc.2.0+json")

    handleWithDefaultRoutes()
  }

  "Routing a request with v2" when {
    "the v2 endpoint exists" should {
      "use the v2 handler" in {
        implicit val acceptHeader: Option[String] = Some("application/vnd.hmrc.2.0+json")
        handleWithVersionRoutes("/resource", V2Handler)
      }
    }
  }

  private def handleWithDefaultRoutes()(implicit acceptHeader: Option[String]): Unit = {
    implicit val useConf: Config = confWithAllEnabled

    "if the request ends with a trailing slash" when {
      "handler found" should {
        "use it" in new Test {
          requestHandler.routeRequest(buildRequest("/")) shouldBe Some(DefaultHandler)
        }
      }

      "handler not found" should {
        "try without the trailing slash" in new Test {
          requestHandler.routeRequest(buildRequest("")) shouldBe Some(DefaultHandler)
        }
      }
    }
  }

  private def handleWithVersionRoutes(path: String, handler: Handler, conf: Config = confWithAllEnabled)(implicit
      acceptHeader: Option[String]): Unit = {

    implicit val useConf: Config = conf

    withClue("request ends with a trailing slash...") {
      new Test {

        requestHandler.routeRequest(buildRequest(s"$path/")) shouldBe Some(handler)
      }
    }
    withClue("request doesn't end with a trailing slash...") {
      new Test {
        requestHandler.routeRequest(buildRequest(s"$path")) shouldBe Some(handler)
      }
    }
  }

  "Routing requests to non-default router with no version" should {
    implicit val acceptHeader: None.type = None
    implicit val useConf: Config         = confWithAllEnabled

    "return 406" in new Test {
      val request: RequestHeader = buildRequest("/resource")
      inside(requestHandler.routeRequest(request)) { case Some(a: EssentialAction) =>
        val result = a.apply(request)

        status(result) shouldBe NOT_ACCEPTABLE
        contentAsJson(result) shouldBe Json.toJson(InvalidAcceptHeaderError)
      }
    }
  }

  "Routing requests with an incorrect URL" should {
    implicit val acceptHeader: Option[String] = Some("application/vnd.hmrc.2.0+json")
    implicit val useConf: Config              = confWithAllEnabled

    "return 404 with a UnsupportedVersionError" in new Test {
      val request: RequestHeader = buildRequest("/missing_resource")
      inside(requestHandler.routeRequest(request)) { case Some(a: EssentialAction) =>
        val result = a.apply(request)
        status(result) shouldBe NOT_FOUND
        contentAsJson(result) shouldBe Json.toJson(NotFoundError)
      }
    }
  }

  "Routing requests with an undefined version" should {
    implicit val acceptHeader: Option[String] = Some("application/vnd.hmrc.5.0+json")
    implicit val useConf: Config              = confWithAllEnabled

    "return 404" in new Test {
      val request: RequestHeader = buildRequest("/resource")

      inside(requestHandler.routeRequest(request)) { case Some(a: EssentialAction) =>
        val result = a.apply(request)

        status(result) shouldBe NOT_FOUND
        contentAsJson(result) shouldBe Json.toJson(UnsupportedVersionError)
      }
    }
  }

  "Routing requests for a defined but disabled version" when {
    implicit val acceptHeader: Option[String] = Some("application/vnd.hmrc.3.0+json")
    implicit val useConf: Config              = confWithV2Disabled

    "the version has a route for the resource" must {
      "return 404 with an UnsupportedVersionError" in new Test {
        val request: RequestHeader = buildRequest("/resource")

        inside(requestHandler.routeRequest(request)) { case Some(a: EssentialAction) =>
          val result = a.apply(request)

          status(result) shouldBe NOT_FOUND
          contentAsJson(result) shouldBe Json.toJson(UnsupportedVersionError)
        }
      }
    }
  }

  "Routing requests with route that does not exist for V1, but exists in V1" should {
    implicit val acceptHeader: Option[String] = Some("application/vnd.hmrc.2.0+json")
    "neither does the route exist in the V1 handler" must {
      implicit val useConf: Config = confWithAllEnabled
      "return a 404 error" in new Test {
        val request: RequestHeader = buildRequest("/missing_resource")
        inside(requestHandler.routeRequest(request)) { case Some(a: EssentialAction) =>
          val result = a.apply(request)
          status(result) shouldBe NOT_FOUND
          contentAsJson(result) shouldBe Json.toJson(NotFoundError)
        }
      }
    }

    "Routing requests with route that does not exist for V1 or V2" should {
      implicit val acceptHeader: Option[String] = Some("application/vnd.hmrc.2.0+json")
      "the V1 has a route, but the requested route does not match the V2 allowed routes" must {
        implicit val useConf: Config = confWithV1DisabledV2Enabled

        "return a 404 error" in new Test {
          val request = buildRequest("/oldResource")

          inside(requestHandler.routeRequest(request)) { case Some(a: EssentialAction) =>
            val result = a.apply(request)

            status(result) shouldBe NOT_FOUND
            contentAsJson(result) shouldBe Json.toJson(NotFoundError)
          }
        }
      }
    }

    "Routing requests with route that does not exist for V2, but exists in V1" should {
      implicit val acceptHeader: Option[String] = Some("application/vnd.hmrc.2.0+json")
      "the V1 has a route, but the requested route matches the V2 allowed routes" must {

        handleWithVersionRoutes("/x/collection/tax-code", V1Handler, confWithV1DisabledV2Enabled)

      }
    }
  }

}
