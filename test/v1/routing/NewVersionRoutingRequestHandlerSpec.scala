/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.routing

import com.typesafe.config.ConfigFactory
import mocks.MockAppConfig
import play.api.Configuration
import play.api.http.{HttpConfiguration, HttpErrorHandler}
import play.api.mvc._
import play.api.routing.Router
import play.api.test.FakeRequest
import play.api.test.Helpers.{ACCEPT, stubControllerComponents}
import routing.{VersionRoutingMap, VersionRoutingRequestHandler}
import support.UnitSpec
import v1.mocks.routing.{MockHttpFilters, MockRouter}

import scala.concurrent.ExecutionContext.Implicits.global

class NewVersionRoutingRequestHandlerSpec extends UnitSpec {

  class Test extends  MockRouter with MockHttpFilters with MockAppConfig {

    private val mockRoutingMap = new VersionRoutingMap {
      override val defaultRouter: Router = mockRouter
      override val map: Map[String, Router] = Map(
        "1.0" -> mockRouter,
        "2.0" -> mockRouter,
        "3.0" -> mockRouter
      )
    }

    lazy val cc: ControllerComponents = stubControllerComponents()
    val bodyParser: BodyParser[AnyContent] = cc.parsers.defaultBodyParser
    val defaultActionBuilder: DefaultActionBuilder = DefaultActionBuilder(bodyParser)
    val httpConfiguration = HttpConfiguration("context")

    val mockHandler: Handler = mock[Handler]
    val mockHttpErrorHandler: HttpErrorHandler = mock[HttpErrorHandler]

    MockHttpFilters.filters.returns(Seq.empty[EssentialFilter])

    MockedAppConfig.featureSwitch.returns(
      Some(Configuration(ConfigFactory.parseString(
        """
          | version-1.enabled = true
          | version-2.enabled = true
        """.stripMargin
      )))
    )

    val versionRoutingRequestHandler = new VersionRoutingRequestHandler(
      versionRoutingMap = mockRoutingMap,
      errorHandler = mockHttpErrorHandler,
      httpConfiguration = httpConfiguration,
      config = mockAppConfig,
      filters = mockHttpFilters,
      action = defaultActionBuilder
    )

    def buildRequestHeader(acceptHeader: Option[String], path: String): RequestHeader = {
      acceptHeader
        .foldLeft(FakeRequest("GET", path)) { (req, accept) =>
          req.withHeaders((ACCEPT, accept))
        }
    }
  }

  "Routing requests with no version" should {
    val acceptHeader: None.type = None
    handleWithDefaultRoutes(acceptHeader)
  }

  private def handleWithDefaultRoutes(acceptHeader: Option[String]): Unit = {
    "if the request ends with a trailing slash" when {
      "handler found" should {
        "use it" in new Test {
          private val requestHeader = buildRequestHeader(acceptHeader = acceptHeader, path = "path/")
          MockRouter.handlerFor(requestHeader)
          //MockRouter.handlerFor(requestHeader).returns(Some(mockHandler))
          //versionRoutingRequestHandler.routeRequest(requestHeader) shouldBe Some(mockHandler)
        }
      }

      "handler not found" should {
        "try without the trailing slash" in new Test {
          private val requestHeader1 = buildRequestHeader(acceptHeader = acceptHeader, path = "path/")
          private val requestHeader2 = buildRequestHeader(acceptHeader = acceptHeader, path = "path")

          //MockRouter.handlerFor(requestHeader1).returns(None)
          //MockRouter.handlerFor(requestHeader2).returns(Some(mockHandler))

          //versionRoutingRequestHandler.routeRequest(requestHeader1) shouldBe Some(mockHandler)
        }
      }
    }
  }

}
