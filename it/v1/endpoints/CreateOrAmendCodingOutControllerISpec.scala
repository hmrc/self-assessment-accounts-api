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
import play.api.libs.json.{ JsObject, JsValue, Json }
import play.api.libs.ws.{ WSRequest, WSResponse }
import support.IntegrationBaseSpec
import v1.models.errors._
import v1.stubs.{ AuditStub, AuthStub, DesStub, MtdIdLookupStub }

class CreateOrAmendCodingOutControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String    = "AA123456A"
    val taxYear: String = "2020-21"

    val requestBodyJson: JsValue = Json.parse(
      s"""|{
          |  "taxCodeComponents": {
          |    "payeUnderpayment": [
          |      {
          |        "amount": 123.45,
          |        "id": 12345
          |      }
          |    ],
          |    "selfAssessmentUnderpayment": [
          |      {
          |        "amount": 123.45,
          |        "id": 12345
          |      }
          |    ],
          |    "debt": [
          |      {
          |        "amount": 123.45,
          |        "id": 12345
          |      }
          |    ],
          |    "inYearAdjustment": {
          |      "amount": 123.45,
          |      "id": 12345
          |    }
          |  }
          |}
          |""".stripMargin
    )

    val responseBody: JsValue = Json.parse(s"""
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
               |  "taxCodeComponents": {
               |    "payeUnderpayment": [
               |      {
               |        "amount": 123498394893843.4,
               |        "id": 12345.35
               |      }
               |    ],
               |    "selfAssessmentUnderpayment": [
               |      {
               |        "amount": 123.45,
               |        "id": 12345
               |      }
               |    ],
               |    "debt": [
               |      {
               |        "amount": 123.45,
               |        "id": 12345
               |      }
               |    ],
               |    "inYearAdjustment": {
               |      "amount": 123.45,
               |      "id": 12345
               |    }
               |  }
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
          response.json shouldBe Json.toJson(
            ErrorWrapper(
              correlationId = "",
              error = BadRequestError,
              errors = Some(Seq(
                ValueFormatError.copy(
                  paths = Some(List(
                    "/taxCodeComponents/payeUnderpayment/0/amount"
                  ))
                ),
                IdFormatError.copy(
                  paths = Some(List(
                    "/taxCodeComponents/payeUnderpayment/0/id"
                  ))
                )
              ))
            )
          )
        }
        "an invalid selfAssessmentUnderPayments is submitted" in new Test {
          override val requestBodyJson: JsValue = Json.parse(
            s"""
               |{
               |  "taxCodeComponents": {
               |    "payeUnderpayment": [
               |      {
               |        "amount": 123.45,
               |        "id": 12345
               |      }
               |    ],
               |    "selfAssessmentUnderpayment": [
               |      {
               |        "amount": 123498394893843.4,
               |        "id": 12345.35
               |      }
               |    ],
               |    "debt": [
               |      {
               |        "amount": 123.45,
               |        "id": 12345
               |      }
               |    ],
               |    "inYearAdjustment": {
               |      "amount": 123.45,
               |      "id": 12345
               |    }
               |  }
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
          response.json shouldBe Json.toJson(
            ErrorWrapper(
              correlationId = "",
              error = BadRequestError,
              errors = Some(Seq(
                ValueFormatError.copy(
                  paths = Some(List(
                    "/taxCodeComponents/selfAssessmentUnderpayment/0/amount"
                  ))
                ),
                IdFormatError.copy(
                  paths = Some(List(
                    "/taxCodeComponents/selfAssessmentUnderpayment/0/id"
                  ))
                )
              ))
            ))
        }
        "an invalid debts is submitted" in new Test {
          override val requestBodyJson: JsValue = Json.parse(
            s"""
               |{
               |  "taxCodeComponents": {
               |    "payeUnderpayment": [
               |      {
               |        "amount": 123.45,
               |        "id": 12345
               |      }
               |    ],
               |    "selfAssessmentUnderpayment": [
               |      {
               |        "amount": 123.45,
               |        "id": 12345
               |      }
               |    ],
               |    "debt": [
               |      {
               |        "amount": 123498394893843.4,
               |        "id": 12345.35
               |      }
               |    ],
               |    "inYearAdjustment": {
               |      "amount": 123.45,
               |      "id": 12345
               |    }
               |  }
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
          response.json shouldBe Json.toJson(
            ErrorWrapper(
              correlationId = "",
              error = BadRequestError,
              errors = Some(Seq(
                ValueFormatError.copy(
                  paths = Some(List(
                    "/taxCodeComponents/debt/0/amount"
                  ))
                ),
                IdFormatError.copy(
                  paths = Some(List(
                    "/taxCodeComponents/debt/0/id"
                  ))
                )
              ))
            ))
        }
        "an invalid inYearAdjustments is submitted" in new Test {
          override val requestBodyJson: JsValue = Json.parse(
            s"""
               |{
               |  "taxCodeComponents": {
               |    "payeUnderpayment": [
               |      {
               |        "amount": 123.45,
               |        "id": 12345
               |      }
               |    ],
               |    "selfAssessmentUnderpayment": [
               |      {
               |        "amount": 123.45,
               |        "id": 12345
               |      }
               |    ],
               |    "debt": [
               |      {
               |        "amount": 123.45,
               |        "id": 12345
               |      }
               |    ],
               |    "inYearAdjustment": {
               |      "amount": 123498394893843.4,
               |      "id": 12345.35
               |    }
               |  }
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
          response.json shouldBe Json.toJson(
            ErrorWrapper(
              correlationId = "",
              error = BadRequestError,
              errors = Some(Seq(
                ValueFormatError.copy(
                  paths = Some(List(
                    "/taxCodeComponents/inYearAdjustment/amount"
                  ))
                ),
                IdFormatError.copy(
                  paths = Some(List(
                    "/taxCodeComponents/inYearAdjustment/id"
                  ))
                )
              ))
            ))
        }
        "all values submitted are invalid" in new Test {
          override val requestBodyJson: JsValue = Json.parse(
            s"""
               |{
               |  "taxCodeComponents": {
               |    "payeUnderpayment": [
               |      {
               |        "amount": 123.455,
               |        "id": -12345
               |      }
               |    ],
               |    "selfAssessmentUnderpayment": [
               |      {
               |        "amount": 123498394893843.4,
               |        "id": 12345.35
               |      }
               |    ],
               |    "debt": [
               |      {
               |        "amount": -123.45,
               |        "id": 123453456789098765434567897654567890987654
               |      }
               |    ],
               |    "inYearAdjustment": {
               |      "amount": 11111111111111111111111111111123.45,
               |      "id": -12345
               |    }
               |  }
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
          response.json shouldBe Json.toJson(
            ErrorWrapper(
              correlationId = "",
              error = BadRequestError,
              errors = Some(Seq(
                ValueFormatError.copy(
                  paths = Some(List(
                    "/taxCodeComponents/payeUnderpayment/0/amount",
                    "/taxCodeComponents/selfAssessmentUnderpayment/0/amount",
                    "/taxCodeComponents/debt/0/amount",
                    "/taxCodeComponents/inYearAdjustment/amount"
                  ))
                ),
                IdFormatError.copy(
                  paths = Some(List(
                    "/taxCodeComponents/payeUnderpayment/0/id",
                    "/taxCodeComponents/selfAssessmentUnderpayment/0/id",
                    "/taxCodeComponents/debt/0/id",
                    "/taxCodeComponents/inYearAdjustment/id"
                  ))
                )
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
          (UNPROCESSABLE_ENTITY, "INVALID_REQUEST_TAX_YEAR", BAD_REQUEST, RuleTaxYearNotEndedError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError)
        )

        input.foreach(args => (serviceErrorTest _).tupled(args))
      }
    }
  }
}
