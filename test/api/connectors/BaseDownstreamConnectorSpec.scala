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

package api.connectors

import api.connectors.DownstreamUri.{DesUri, Ifs1Uri, Ifs2Uri}
import api.mocks.MockHttpClient
import api.models.outcomes.ResponseWrapper
import config.AppConfig
import mocks.MockAppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads}

import scala.concurrent.Future

class BaseDownstreamConnectorSpec extends ConnectorSpec {

  // WLOG
  case class Result(value: Int)

  // WLOG
  val body                               = "body"
  val queryParams: Seq[(String, String)] = Seq("aParam" -> "aValue")
  val outcome                            = Right(ResponseWrapper(correlationId, Result(2)))

  val url         = "some/url?param=value"
  val absoluteUrl = s"$baseUrl/$url"

  implicit val httpReads: HttpReads[DownstreamOutcome[Result]] = mock[HttpReads[DownstreamOutcome[Result]]]

  class DesTest(desEnvironmentHeaders: Option[Seq[String]]) extends MockHttpClient with MockAppConfig {

    val connector: BaseDownstreamConnector = new BaseDownstreamConnector {
      val http: HttpClient     = mockHttpClient
      val appConfig: AppConfig = mockAppConfig
    }

    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns desEnvironmentHeaders
  }

  "BaseDesConnector" when {
    val requiredHeaders: Seq[(String, String)] = Seq(
      "Environment"       -> "des-environment",
      "Authorization"     -> s"Bearer des-token",
      "User-Agent"        -> "self-assessment-accounts-api",
      "CorrelationId"     -> correlationId,
      "Gov-Test-Scenario" -> "DEFAULT"
    )

    val excludedHeaders: Seq[(String, String)] = Seq(
      "AnotherHeader" -> "HeaderValue"
    )

    "making a HTTP request to a downstream service (i.e DES)" must {
      desTestHttpMethods(dummyDesHeaderCarrierConfig, requiredHeaders, excludedHeaders, Some(allowedDesHeaders))

      "exclude all `otherHeaders` when no external service header allow-list is found" should {
        val requiredHeaders: Seq[(String, String)] = Seq(
          "Environment"   -> "des-environment",
          "Authorization" -> s"Bearer des-token",
          "User-Agent"    -> "self-assessment-accounts-api",
          "CorrelationId" -> correlationId
        )

        desTestHttpMethods(dummyDesHeaderCarrierConfig, requiredHeaders, otherHeaders, None)
      }
    }
  }

  def desTestHttpMethods(config: HeaderCarrier.Config,
                         requiredHeaders: Seq[(String, String)],
                         excludedHeaders: Seq[(String, String)],
                         desEnvironmentHeaders: Option[Seq[String]]): Unit = {

    "complete the request successfully with the required headers" when {
      "GET" in new DesTest(desEnvironmentHeaders) {
        MockHttpClient
          .get(absoluteUrl, config, requiredHeaders, excludedHeaders)
          .returns(Future.successful(outcome))

        await(connector.get(DesUri[Result](url))) shouldBe outcome
      }

      "GET with query params" in new DesTest(desEnvironmentHeaders) {
        val params = Seq("param1" -> "value")
        MockHttpClient
          .parameterGet(absoluteUrl, params, config, requiredHeaders, excludedHeaders)
          .returns(Future.successful(outcome))

        await(connector.get(DesUri[Result](url), params)) shouldBe outcome
      }

      "POST" in new DesTest(desEnvironmentHeaders) {
        implicit val hc: HeaderCarrier                 = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
        val requiredHeadersPost: Seq[(String, String)] = requiredHeaders ++ Seq("Content-Type" -> "application/json")

        MockHttpClient
          .post(absoluteUrl, config, body, requiredHeadersPost, excludedHeaders)
          .returns(Future.successful(outcome))

        await(connector.post(body, DesUri[Result](url))) shouldBe outcome
      }

      "PUT" in new DesTest(desEnvironmentHeaders) {
        implicit val hc: HeaderCarrier                = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
        val requiredHeadersPut: Seq[(String, String)] = requiredHeaders ++ Seq("Content-Type" -> "application/json")

        MockHttpClient
          .put(absoluteUrl, config, body, requiredHeadersPut, excludedHeaders)
          .returns(Future.successful(outcome))

        await(connector.put(body, DesUri[Result](url))) shouldBe outcome
      }

      "DELETE" in new DesTest(desEnvironmentHeaders) {
        MockHttpClient
          .delete(absoluteUrl, config, requiredHeaders, excludedHeaders)
          .returns(Future.successful(outcome))

        await(connector.delete(DesUri[Result](url))) shouldBe outcome
      }
    }
  }

  def getRequiredHeaders(id: String): Seq[(String, String)] = Seq(
    "Environment"       -> s"ifs$id-environment",
    "Authorization"     -> s"Bearer ifs$id-token",
    "User-Agent"        -> "self-assessment-accounts-api",
    "CorrelationId"     -> correlationId,
    "Gov-Test-Scenario" -> "DEFAULT"
  )

  val excludedHeaders: Seq[(String, String)] = Seq(
    "AnotherHeader" -> "HeaderValue"
  )

  class Ifs1Test(ifsEnvironmentHeaders: Option[Seq[String]]) extends MockHttpClient with MockAppConfig {

    val connector: BaseDownstreamConnector = new BaseDownstreamConnector {
      val http: HttpClient     = mockHttpClient
      val appConfig: AppConfig = mockAppConfig
    }

    MockAppConfig.ifs1BaseUrl returns baseUrl
    MockAppConfig.ifs1Token returns "ifs1-token"
    MockAppConfig.ifs1Environment returns "ifs1-environment"
    MockAppConfig.ifs1EnvironmentHeaders returns ifsEnvironmentHeaders

  }

  class Ifs2Test(ifsEnvironmentHeaders: Option[Seq[String]]) extends MockHttpClient with MockAppConfig {

    val connector: BaseDownstreamConnector = new BaseDownstreamConnector {
      val http: HttpClient     = mockHttpClient
      val appConfig: AppConfig = mockAppConfig
    }

    MockAppConfig.ifs2BaseUrl returns baseUrl
    MockAppConfig.ifs2Token returns "ifs2-token"
    MockAppConfig.ifs2Environment returns "ifs2-environment"
    MockAppConfig.ifs2EnvironmentHeaders returns ifsEnvironmentHeaders
  }

  "BaseDownstreamConnector" should {
    "return the correct headers" when {
      def downstreamHeaders(id: String): Unit = {
        s"making a HTTP request to IFS$id" must {
          val requiredHeaders: Seq[(String, String)] = getRequiredHeaders(id)

          id match {
            case "1" => ifs1TestHttpMethods(dummyIfs1HeaderCarrierConfig, requiredHeaders, excludedHeaders, Some(allowedIfs1Headers))
            case _   => ifs2TestHttpMethods(dummyIfs2HeaderCarrierConfig, requiredHeaders, excludedHeaders, Some(allowedIfs2Headers))
          }

          "exclude all `otherHeaders` when no external service header allow-list is found" should {
            val requiredHeaders: Seq[(String, String)] = Seq(
              "Environment"   -> s"ifs$id-environment",
              "Authorization" -> s"Bearer ifs$id-token",
              "User-Agent"    -> "self-assessment-accounts-api",
              "CorrelationId" -> correlationId
            )
            id match {
              case "1" => ifs1TestHttpMethods(dummyIfs1HeaderCarrierConfig, requiredHeaders, otherHeaders, None)
              case _   => ifs2TestHttpMethods(dummyIfs2HeaderCarrierConfig, requiredHeaders, otherHeaders, None)
            }

          }
        }
      }
      Seq("1", "2").foreach(c => downstreamHeaders(c))
    }
  }

  def ifs1TestHttpMethods(config: HeaderCarrier.Config,
                          requiredHeaders: Seq[(String, String)],
                          excludedHeaders: Seq[(String, String)],
                          ifsEnvironmentHeaders: Option[Seq[String]]): Unit = {

    "complete the request successfully with the required headers" when {
      "POST" in new Ifs1Test(ifsEnvironmentHeaders) {
        implicit val hc: HeaderCarrier                 = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
        val requiredHeadersPost: Seq[(String, String)] = requiredHeaders ++ Seq("Content-Type" -> "application/json")

        MockHttpClient
          .post(absoluteUrl, config, body, requiredHeadersPost, excludedHeaders)
          .returns(Future.successful(outcome))

        await(connector.post(body, Ifs1Uri[Result](url))) shouldBe outcome
      }

      "complete the request successfully with the required headers" when {
        "GET" in new Ifs1Test(ifsEnvironmentHeaders) {
          MockHttpClient
            .get(absoluteUrl, config, requiredHeaders, excludedHeaders)
            .returns(Future.successful(outcome))

          await(connector.get(Ifs1Uri[Result](url))) shouldBe outcome
        }

        "GET with query params" in new Ifs1Test(ifsEnvironmentHeaders) {
          val params = Seq("param1" -> "value")
          MockHttpClient
            .parameterGet(absoluteUrl, params, config, requiredHeaders, excludedHeaders)
            .returns(Future.successful(outcome))

          await(connector.get(Ifs1Uri[Result](url), params)) shouldBe outcome
        }

        "PUT" in new Ifs1Test(ifsEnvironmentHeaders) {
          implicit val hc: HeaderCarrier                = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
          val requiredHeadersPut: Seq[(String, String)] = requiredHeaders ++ Seq("Content-Type" -> "application/json")

          MockHttpClient
            .put(absoluteUrl, config, body, requiredHeadersPut, excludedHeaders)
            .returns(Future.successful(outcome))

          await(connector.put(body, Ifs1Uri[Result](url))) shouldBe outcome
        }

        "DELETE" in new Ifs1Test(ifsEnvironmentHeaders) {
          MockHttpClient
            .delete(absoluteUrl, config, requiredHeaders, excludedHeaders)
            .returns(Future.successful(outcome))

          await(connector.delete(Ifs1Uri[Result](url))) shouldBe outcome
        }
      }
    }
  }

  def ifs2TestHttpMethods(config: HeaderCarrier.Config,
                          requiredHeaders: Seq[(String, String)],
                          excludedHeaders: Seq[(String, String)],
                          ifsEnvironmentHeaders: Option[Seq[String]]): Unit = {

    "complete the request successfully with the required headers" when {
      "POST" in new Ifs2Test(ifsEnvironmentHeaders) {
        implicit val hc: HeaderCarrier                 = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
        val requiredHeadersPost: Seq[(String, String)] = requiredHeaders ++ Seq("Content-Type" -> "application/json")

        MockHttpClient
          .post(absoluteUrl, config, body, requiredHeadersPost, excludedHeaders)
          .returns(Future.successful(outcome))

        await(connector.post(body, Ifs2Uri[Result](url))) shouldBe outcome
      }

      "complete the request successfully with the required headers" when {
        "GET" in new Ifs2Test(ifsEnvironmentHeaders) {
          MockHttpClient
            .get(absoluteUrl, config, requiredHeaders, excludedHeaders)
            .returns(Future.successful(outcome))

          await(connector.get(Ifs2Uri[Result](url))) shouldBe outcome
        }

        "GET with query params" in new Ifs2Test(ifsEnvironmentHeaders) {
          val params = Seq("param1" -> "value")
          MockHttpClient
            .parameterGet(absoluteUrl, params, config, requiredHeaders, excludedHeaders)
            .returns(Future.successful(outcome))

          await(connector.get(Ifs2Uri[Result](url), params)) shouldBe outcome
        }

        "PUT" in new Ifs2Test(ifsEnvironmentHeaders) {
          implicit val hc: HeaderCarrier                = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
          val requiredHeadersPut: Seq[(String, String)] = requiredHeaders ++ Seq("Content-Type" -> "application/json")

          MockHttpClient
            .put(absoluteUrl, config, body, requiredHeadersPut, excludedHeaders)
            .returns(Future.successful(outcome))

          await(connector.put(body, Ifs2Uri[Result](url))) shouldBe outcome
        }

        "DELETE" in new Ifs2Test(ifsEnvironmentHeaders) {
          MockHttpClient
            .delete(absoluteUrl, config, requiredHeaders, excludedHeaders)
            .returns(Future.successful(outcome))

          await(connector.delete(Ifs2Uri[Result](url))) shouldBe outcome
        }
      }
    }
  }

}
