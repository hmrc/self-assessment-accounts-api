/*
 * Copyright 2023 HM Revenue & Customs
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

package v3.endpoints.createOrAmendCodingOut.def1

import api.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec

class Def1_CreateOrAmendCodingOutISpec extends IntegrationBaseSpec {

  private trait Test {

    protected val nino = "AA123456A"

    protected def taxYear: String

    protected val requestBodyJson: JsValue = Json.parse(
      """
        |{
        |  "taxCodeComponents": {
        |    "payeUnderpayment": [
        |      {
        |        "amount": 123.45,
        |        "id": 1
        |      }
        |    ],
        |    "selfAssessmentUnderpayment": [
        |      {
        |        "amount": 123.45,
        |        "id": 2
        |      }
        |    ],
        |    "debt": [
        |      {
        |        "amount": 123.45,
        |        "id": 3
        |      }
        |    ],
        |    "inYearAdjustment": {
        |      "amount": 123.45,
        |      "id": 4
        |    }
        |  }
        |}
    """.stripMargin
    )

    protected val responseBody: JsValue = Json.parse(
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
     """.stripMargin
    )

    def setupStubs(): Unit = ()

    def request: WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(s"/$nino/$taxYear/collection/tax-code")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String =
      s"""
         |{
         |   "code": "$code",
         |   "reason": "A message from downstream"
         |}
      """.stripMargin

  }

  private class NonTysTest extends Test {
    def taxYear: String = "2020-21"

    def downstreamUri: String = s"/income-tax/accounts/self-assessment/collection/tax-code/$nino/2020-21"
  }

  private class TysTest extends Test {
    def taxYear = "2023-24"

    def downstreamUri = s"/income-tax/23-24/accounts/self-assessment/collection/tax-code/$nino"

    override def request: WSRequest =
      super.request.addHttpHeaders("suspend-temporal-validations" -> "true")

  }

  "The controller" should {
    "return a 200 status code" when {
      "any valid request is made" in new NonTysTest {
        override def setupStubs(): Unit =
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, JsObject.empty)

        val response: WSResponse = await(request.put(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }

      "any valid request is made (TYS)" in new TysTest {
        override def setupStubs(): Unit =
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, JsObject.empty)

        val response: WSResponse = await(request.put(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }
    }

    "return an error according to the spec" when {
      "validation error" when {

        "an invalid NINO format is provided" in new NonTysTest {
          override val nino: String = "INVALID_NINO"

          val response: WSResponse = await(request.put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe NinoFormatError.asJson
        }

        "an invalid taxYear format is provided" in new NonTysTest {
          override val taxYear: String = "INVALID_TAXYEAR"

          val response: WSResponse = await(request.put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe TaxYearFormatError.asJson
        }

        "an unsupported taxYear is provided" in new NonTysTest {
          override val taxYear: String = "2016-17"

          val response: WSResponse = await(request.put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe RuleTaxYearNotSupportedError.asJson
        }

        "a taxYear with an incorrect range is provided" in new NonTysTest {
          override val taxYear: String = "2020-22"

          val response: WSResponse = await(request.put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe RuleTaxYearRangeInvalidError.asJson
        }

        "a taxYear which has not ended is provided" in new NonTysTest {
          override val taxYear: String = "2098-99"

          val response: WSResponse = await(request.put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe RuleTaxYearNotEndedError.asJson
        }

        "an invalid value and id is submitted" in new NonTysTest {
          override val requestBodyJson: JsValue = Json.parse(
            """
                |{
                |  "taxCodeComponents": {
                |    "payeUnderpayment": [
                |      {
                |        "amount": 123498394893843.4,
                |        "id": 12345.35
                |      }
                |    ]
                |  }
                |}
            """.stripMargin
          )

          val response: WSResponse = await(request.put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe Json.toJson(
            ErrorWrapper(
              correlationId = "",
              error = BadRequestError,
              errors = Some(List(
                IdFormatError.copy(
                  paths = Some(List(
                    "/taxCodeComponents/payeUnderpayment/0/id"
                  ))
                ),
                ValueFormatError.copy(
                  paths = Some(List(
                    "/taxCodeComponents/payeUnderpayment/0/amount"
                  ))
                )
              ))
            )
          )
        }

        "an empty body is submitted" in new NonTysTest {
          override val requestBodyJson: JsValue = JsObject.empty

          val response: WSResponse = await(request.put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe RuleIncorrectOrEmptyBodyError.asJson
        }
      }

      "service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns a $downstreamCode error and status $downstreamStatus" in new NonTysTest {

            override def setupStubs(): Unit =
              DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, downstreamStatus, errorBody(downstreamCode))

            val response: WSResponse = await(request.put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val errors = List(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "INVALID_REQUEST_TAX_YEAR", BAD_REQUEST, RuleTaxYearNotEndedError),
          (UNPROCESSABLE_ENTITY, "DUPLICATE_ID_NOT_ALLOWED", BAD_REQUEST, RuleDuplicateIdError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError)
        )

        val extraTysErrors = List(
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))

      }
    }
  }

}
