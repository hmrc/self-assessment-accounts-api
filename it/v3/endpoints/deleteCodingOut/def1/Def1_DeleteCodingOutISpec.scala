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

package v3.endpoints.deleteCodingOut.def1

import common.errors.CodingOutNotFoundError
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors._
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import support.IntegrationBaseSpec

class Def1_DeleteCodingOutISpec extends IntegrationBaseSpec {

  "Calling the delete endpoint" should {
    "return a 204 status code for a non-TYS request" when {
      "sent a valid request" in new NonTysTest {
        override def setupStubs(): Unit = {
          DownstreamStub.onSuccess(DownstreamStub.DELETE, downstreamUri, NO_CONTENT, JsObject.empty)
        }
        val response: WSResponse = await(newRequest.delete())
        response.status shouldBe NO_CONTENT
        response.header("X-CorrelationId") shouldBe defined
      }
    }

    "return a 204 status code for a TYS request" when {
      "sent a valid request" in new TysIfsTest {
        DownstreamStub.onSuccess(DownstreamStub.DELETE, downstreamUri, NO_CONTENT, JsObject.empty)

        val response: WSResponse = await(newRequest.delete())
        withClue(s"Response message: ${response.body}") {
          response.status shouldBe NO_CONTENT
          response.header("X-CorrelationId") shouldBe defined
        }
      }
    }

    "return error according to spec" when {
      def validationErrorTest(requestNino: String, requestId: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error " in new NonTysTest {

          override val nino: String    = requestNino
          override val taxYear: String = requestId

          MtdIdLookupStub.ninoFound(nino)

          val response: WSResponse = await(newRequest.delete())
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = List(
        ("invalidNino", "2021-22", BAD_REQUEST, NinoFormatError),
        ("AA123456A", "203100", BAD_REQUEST, TaxYearFormatError),
        ("AA123456A", "2018-19", BAD_REQUEST, RuleTaxYearNotSupportedError),
        ("AA123456A", "2018-20", BAD_REQUEST, RuleTaxYearRangeInvalidError)
      )

      input.map(c => (c._1, c._2, c._3, c._4)).foreach((validationErrorTest _).tupled)
    }

    "downstream service error" when {
      def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"downstream returns an $downstreamCode error and status $downstreamStatus " in new NonTysTest {
          DownstreamStub.onError(DownstreamStub.DELETE, downstreamUri, downstreamStatus, errorBody(downstreamCode))

          val response: WSResponse = await(newRequest.delete())
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val errors = List(
        (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
        (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
        (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, CodingOutNotFoundError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
      )

      val extraTysErrors = List(
        (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
      )

      (errors ++ extraTysErrors).map(c => (c._1, c._2, c._3, c._4)).foreach((serviceErrorTest _).tupled)
    }
  }

  private trait Test {
    protected val nino = "AA123456A"

    protected def taxYear: String

    protected def downstreamUri: String

    def setupStubs(): Unit = ()

    protected def mtdUri: String = s"/$nino/$taxYear/collection/tax-code"

    protected def newRequest: WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(mtdUri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    protected def errorBody(code: String): String =
      s"""
         |      {
         |        "code": "$code",
         |        "reason": "downstream message"
         |      }
    """.stripMargin

  }

  private trait NonTysTest extends Test {
    def taxYear: String = "2021-22"

    def downstreamUri: String = s"/income-tax/accounts/self-assessment/collection/tax-code/$nino/$taxYear"
  }

  private trait TysIfsTest extends Test {
    def taxYear: String = "2023-24"

    def downstreamUri: String = s"/income-tax/23-24/accounts/self-assessment/collection/tax-code/$nino"
  }

}
