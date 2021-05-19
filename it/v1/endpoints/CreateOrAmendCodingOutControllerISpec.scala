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
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class CreateOrAmendCodingOutControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String = "AA123456A"
    val taxYear: String = "2020-21"

    val requestBodyJson: JsValue = Json.parse(
      s"""|{
          |  "payeUnderpayments": 987.93,
          |  "selfAssessmentUnderPayments": 179.00,
          |  "debts": 724.02,
          |  "inYearAdjustments": 342.87
          |}
          |""".stripMargin
    )

    val responseBody: JsValue = Json.parse(
      s"""
         |{
         |  "links": [
         |    {
         |      "href": "/accounts/self-assessment/$nino/$taxYear/collection/tax-code",
         |      "method": "PUT",
         |      "rel": "create-or-amend-coding-out-underpayments"
         |    },
         |    {
         |      "href": "/accounts/self-assessment/$nino/$taxYear/collection/tax-code",
         |      "method": "GET",
         |      "rel": "self"
         |    },
         |    {
         |      "href": "/accounts/self-assessment/$nino/$taxYear/collection/tax-code",
         |      "method": "DELETE",
         |      "rel": "delete-coding-out-underpayments"
         |    }
         |  ]
         |}
         |""".stripMargin)

    def uri: String = s"/$nino/$taxYear/collection/tax-code"

    def desUri: String = s"/income-tax/accounts/self-assessment/collection/tax-code/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }

    def errorBody(code: String): String =
      s"""
         |      {
         |        "code": "$code",
         |        "reason": "des message"
         |      }
        """.stripMargin
  }

  "Calling the amend endpoint" should {

    "return a 200 status code" when {

      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.PUT, desUri, NO_CONTENT, JsObject.empty)
        }

        val response: WSResponse = await(request().put(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }

    "return error according to spec" when {
      "validation error" when {
        "an invalid NINO format is provided" in new Test {
          override val nino: String = "INVALID_NINO"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(NinoFormatError)
        }
        "an invalid taxYear format is provided" in new Test {
          override val taxYear: String = "INVALID_TAXYEAR"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(TaxYearFormatError)
        }
        "an unsupported taxYear is provided" in new Test {
          override val taxYear: String = "2016-17"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(RuleTaxYearNotSupportedError)
        }
        "a taxYear with an incorrect range is provided" in new Test {
          override val taxYear: String = "2020-22"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }
          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(RuleTaxYearRangeInvalidError)
        }
        "a taxYear which has not ended is provided" in new Test {
          override val taxYear: String = "2021-22"

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }
          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(RuleTaxYearNotEndedError)
        }
        "an invalid payeUnderpayments is submitted" in new Test {
          override val requestBodyJson: JsValue = Json.parse(
            s"""
               |{
               |  "payeUnderpayments": 987.956783,
               |  "selfAssessmentUnderPayments": 179.00,
               |  "debts": 724.02,
               |  "inYearAdjustments": 342.87
               |}
               |""".stripMargin
          )

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }
          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(ValueFormatError.copy(
            paths = Some(List(
              "/payeUnderpayments"
            ))
          ))
        }
        "an invalid selfAssessmentUnderPayments is submitted" in new Test {
          override val requestBodyJson: JsValue = Json.parse(
            s"""
               |{
               |  "payeUnderpayments": 987.93,
               |  "selfAssessmentUnderPayments": 179.0056789876,
               |  "debts": 724.02,
               |  "inYearAdjustments": 342.87
               |}
               |""".stripMargin
          )

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }
          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(ValueFormatError.copy(
            paths = Some(List(
              "/selfAssessmentUnderPayments"
            ))
          ))
        }
        "an invalid debts is submitted" in new Test {
          override val requestBodyJson: JsValue = Json.parse(
            s"""
               |{
               |  "payeUnderpayments": 987.93,
               |  "selfAssessmentUnderPayments": 179.00,
               |  "debts": 724.04567898762,
               |  "inYearAdjustments": 342.87
               |}
               |""".stripMargin
          )

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }
          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(ValueFormatError.copy(
            paths = Some(List(
              "/debts"
            ))
          ))
        }
        "an invalid inYearAdjustments is submitted" in new Test {
          override val requestBodyJson: JsValue = Json.parse(
            s"""
               |{
               |  "payeUnderpayments": 987.93,
               |  "selfAssessmentUnderPayments": 179.00,
               |  "debts": 724.02,
               |  "inYearAdjustments": 342.8456788767
               |}
               |""".stripMargin
          )

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }
          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(ValueFormatError.copy(
            paths = Some(List(
              "/inYearAdjustments"
            ))
          ))
        }
        "all values submitted are invalid" in new Test {
          override val requestBodyJson: JsValue = Json.parse(
            s"""
               |{
               |  "payeUnderpayments": 987.945673,
               |  "selfAssessmentUnderPayments": 179.045670,
               |  "debts": 724.045672,
               |  "inYearAdjustments": 342.8456788767
               |}
               |""".stripMargin
          )

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }
          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(ValueFormatError.copy(
            paths = Some(List(
              "/payeUnderpayments",
              "/selfAssessmentUnderPayments",
              "/debts",
              "/inYearAdjustments"
            ))
          ))
        }
        "an empty body is submitted" in new Test {
          override val requestBodyJson: JsValue = Json.parse("{}")

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }
          val response: WSResponse = await(request().put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(RuleIncorrectOrEmptyBodyError)
        }
      }

      "des service error" when {
        def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"des returns an $desCode error and status $desStatus" in new Test {

            override def setupStubs(): StubMapping = {
              AuditStub.audit()
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DesStub.onError(DesStub.PUT, desUri, desStatus, errorBody(desCode))
            }

            val response: WSResponse = await(request().put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val input = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, DownstreamError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, DownstreamError),
          (UNPROCESSABLE_ENTITY, "BEFORE_TAXYEAR_END", BAD_REQUEST, RuleTaxYearNotEndedError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError))

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}
