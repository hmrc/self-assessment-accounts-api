/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.endpoints

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.fixtures.RetrieveCodingOutFixture._
import v1.models.errors._
import v1.stubs.{AuthStub, DesStub, MtdIdLookupStub}

class RetrieveCodingOutControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"
    val taxYear: String = "2021-22"

    def mtdSource: String = "hmrcHeld"

    def desSource: String = "HMRC HELD"

    val desResponse: JsValue = Json.parse(
      s"""
         |{
         |        "taxCodeComponents": {
         |            "selfAssessmentUnderpayment": [
         |                {
         |                    "amount": 0,
         |                    "relatedTaxYear": "$taxYear",
         |                    "submittedOn": "2019-08-24T14:15:22Z",
         |                    "source": "$mtdSource",
         |                    "componentIdentifier": 12345678910
         |                }
         |            ],
         |            "payeUnderpayment": [
         |                {
         |                    "amount": 0,
         |                    "relatedTaxYear": "$taxYear",
         |                    "submittedOn": "2019-08-24T14:15:22Z",
         |                    "source": "$mtdSource",
         |                    "componentIdentifier": 12345678910
         |                }
         |            ],
         |            "debt": [
         |                {
         |                    "amount": 0,
         |                    "relatedTaxYear": "$taxYear",
         |                    "submittedOn": "2019-08-24T14:15:22Z",
         |                    "source": "CUSTOMER",
         |                    "componentIdentifier": 12345678910
         |                }
         |            ],
         |            "inYearAdjustment": {
         |                "amount": 0,
         |                "relatedTaxYear": "$taxYear",
         |                "submittedOn": "2019-08-24T14:15:22Z",
         |                "source": "CUSTOMER",
         |                "componentIdentifier": 12345678910
         |            }
         |        },
         |        "unmatchedCustomerSubmissions": {
         |            "selfAssessmentUnderpayment": [
         |                {
         |                    "amount": 0,
         |                    "submittedOn": "2019-08-24T14:15:22Z",
         |                    "componentIdentifier": 12345678910
         |                }
         |            ],
         |            "payeUnderpayment": [
         |                {
         |                    "amount": 0,
         |                    "submittedOn": "2019-08-24T14:15:22Z",
         |                    "componentIdentifier": 12345678910
         |                }
         |            ],
         |            "debt": [
         |                {
         |                    "amount": 0,
         |                    "submittedOn": "2019-08-24T14:15:22Z",
         |                    "componentIdentifier": 12345678910
         |                }
         |            ],
         |            "inYearAdjustment": {
         |                "amount": 0,
         |                "submittedOn": "2019-08-24T14:15:22Z",
         |                "componentIdentifier": 12345678910
         |            }
         |        }
         |}
       """.stripMargin
    )

    val mtdResponse: JsValue = mtdResponseWithHateoas(nino, taxYear, mtdSource)

    def uri: String = s"/$nino/$taxYear/collection/tax-code"

    def desUri: String = s"/income-tax/accounts/self-assessment/collection/tax-code/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(source: Option[String]): WSRequest = {
      def queryParams: Seq[(String, String)] = Seq("source" -> source).collect {
        case (k, Some(v)) => (k, v)
      }

      setupStubs()
      buildRequest(uri)
        .addQueryStringParameters(queryParams: _*)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }
  }

  "Calling the 'retrieve coding out' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made to retrieve latest view when no query parameter is specified" in new Test {

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, OK, desResponse)
        }

        val response: WSResponse = await(request(None).get())
        response.status shouldBe OK
        response.json shouldBe mtdResponse
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made to retrieve the latest view" in new Test {

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, Map("view" -> "LATEST"), OK, desResponse)
        }

        val response: WSResponse = await(request(Some("latest")).get())
        response.status shouldBe OK
        response.json shouldBe mtdResponse
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made to retrieve hmrcHeld data" in new Test {

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, Map("view" -> "HMRC-HELD"), OK, desResponse)
        }

        val response: WSResponse = await(request(Some(mtdSource)).get())
        response.status shouldBe OK
        response.json shouldBe mtdResponse
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made to retrieve user submitted data" in new Test {

        override def mtdSource: String = "user"

        override def desSource: String = "CUSTOMER"

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUri, Map("view" -> desSource), OK, desResponse)
        }

        val response: WSResponse = await(request(Some(mtdSource)).get())
        response.status shouldBe OK
        response.json shouldBe mtdResponse
        response.header("X-CorrelationId").nonEmpty shouldBe true
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      "validation error" when {
        def validationErrorTest(requestNino: String, requestTaxYear: String, requestSource: String,
                                expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"validation fails with ${expectedBody.code} error" in new Test {

            override val nino: String = requestNino
            override val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request(Some(requestSource)).get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input = Seq(
          ("AA1123A", "2021-22", "latest", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20199", "latest", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2021-22", "source", BAD_REQUEST, SourceFormatError),
          ("AA123456A", "2018-19", "latest", BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2021-23", "latest", BAD_REQUEST, RuleTaxYearRangeInvalidError)
        )

        input.foreach(args => (validationErrorTest _).tupled(args))
      }

      "des service error" when {
        def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"des returns an $desCode error and status $desStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DesStub.onError(DesStub.GET, desUri, desStatus, errorBody(desCode))
            }

            val response: WSResponse = await(request(None).get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        def errorBody(code: String): String =
          s"""
             |{
             |  "code": "$code",
             |  "reason": "des message"
             |}
            """.stripMargin

        val input = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_VIEW", BAD_REQUEST, SourceFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, DownstreamError),
          (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, CodingOutNotFoundError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError)
        )

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}