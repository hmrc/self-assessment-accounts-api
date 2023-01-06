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

package v1Andv2.endpoints

import api.models.errors._
import api.stubs.{AuditStub, AuthStub, MtdIdLookupStub}
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.stubs.DownstreamStub

class CreateOrAmendCodingOutControllerISpec extends IntegrationBaseSpec {

  val versions: Seq[String] = Seq("1.0", "2.0")

  private trait Test {
    val version: String

    val nino: String = "AA123456A"

    def taxYear: String

    val requestBodyJson: JsValue = Json.parse(
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
          (ACCEPT, s"application/vnd.hmrc.$version+json"),
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

  private class NonTysTest(val version: String) extends Test {
    def taxYear: String = "2020-21"

    def downstreamUri: String = s"/income-tax/accounts/self-assessment/collection/tax-code/$nino/2020-21"
  }

  private class TysTest(val version: String) extends Test {
    def taxYear: String = "2023-24"

    def downstreamUri: String = s"/income-tax/23-24/accounts/self-assessment/collection/tax-code/$nino"

    override def request: WSRequest =
      super.request.addHttpHeaders("suspend-temporal-validations" -> "true")

  }

  versions.foreach(version =>
    s"Calling the 'create or amend coding out' endpoint for version $version" should {
      behave like new EndpointBehaviour(version)
    })

  class EndpointBehaviour(version: String) {

    "return a 200 status code" when {
      "any valid request is made" in new NonTysTest(version) {
        override def setupStubs(): Unit =
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, JsObject.empty)

        val response: WSResponse = await(request.put(requestBodyJson))
        response.status shouldBe OK
        response.json shouldBe responseBody
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }

      "any valid request is made (TYS)" in new TysTest(version) {
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

        "an invalid NINO format is provided" in new NonTysTest(version) {
          override val nino: String = "INVALID_NINO"

          val response: WSResponse = await(request.put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe NinoFormatError.asJson
        }

        "an invalid taxYear format is provided" in new NonTysTest(version) {
          override val taxYear: String = "INVALID_TAXYEAR"

          val response: WSResponse = await(request.put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe TaxYearFormatError.asJson
        }

        "an unsupported taxYear is provided" in new NonTysTest(version) {
          override val taxYear: String = "2016-17"

          val response: WSResponse = await(request.put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe RuleTaxYearNotSupportedError.asJson
        }

        "a taxYear with an incorrect range is provided" in new NonTysTest(version) {
          override val taxYear: String = "2020-22"

          val response: WSResponse = await(request.put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe RuleTaxYearRangeInvalidError.asJson
        }

        "a taxYear which has not ended is provided" in new NonTysTest(version) {
          override val taxYear: String = "2098-99"

          val response: WSResponse = await(request.put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe RuleTaxYearNotEndedError.asJson
        }

        "an invalid value and id is submitted" in new NonTysTest(version) {
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

        "an empty body is submitted" in new NonTysTest(version) {
          override val requestBodyJson: JsValue = Json.parse("{}")

          val response: WSResponse = await(request.put(requestBodyJson))
          response.status shouldBe BAD_REQUEST
          response.json shouldBe RuleIncorrectOrEmptyBodyError.asJson
        }
      }

      "service error" when {
        def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
          s"downstream returns a $downstreamCode error and status $downstreamStatus" in new NonTysTest(version) {

            override def setupStubs(): Unit =
              DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, downstreamStatus, errorBody(downstreamCode))

            val response: WSResponse = await(request.put(requestBodyJson))
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
          }
        }

        val errors = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
          (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "INVALID_REQUEST_TAX_YEAR", BAD_REQUEST, RuleTaxYearNotEndedError),
          (UNPROCESSABLE_ENTITY, "DUPLICATE_ID_NOT_ALLOWED", BAD_REQUEST, RuleDuplicateIdError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError)
        )

        val extraTysErrors = Seq(
          (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
        )

        (errors ++ extraTysErrors).foreach(args => (serviceErrorTest _).tupled(args))

      }
    }

  }

}
