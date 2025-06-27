/*
 * Copyright 2025 HM Revenue & Customs
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

package v3.endpoints.optOutOfCodingOut.def1

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.errors.{RuleAlreadyOptedOutError, RuleBusinessPartnerNotExistError, RuleItsaContractObjectNotExistError}
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.{EmptyBody, WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors.{InternalError, MtdError, NinoFormatError, TaxYearFormatError}
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import shared.support.IntegrationBaseSpec
import v3.retrieveCodingOutStatus.model.responses.ResponseFixture.downstreamOptOutOfCodingOutResponseJson

class Def1_OptOutOfCodingOutISpec extends IntegrationBaseSpec {

  "Calling the 'opt out of coding out' endpoint" when {
    "any valid request is made with nino and tax year" should {
      "return a 204 No Content status code" in new Test {

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUrl, OK, downstreamOptOutOfCodingOutResponseJson)
        }

        val response: WSResponse = await(request.post(EmptyBody))
        response.status shouldBe NO_CONTENT
        response.header("Content-Type") shouldBe None
      }

    }
  }

  "return error according to spec" when {
    def validationErrorTest(requestNino: String, requestTaxYear: String, expectedStatus: Int, expectedBody: MtdError): Unit = {

      s"validation fails with ${expectedBody.code} error" in new Test {
        override protected val nino: String    = requestNino
        override protected val taxYear: String = requestTaxYear

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
        }

        val response: WSResponse = await(request.post(EmptyBody))
        response.status shouldBe expectedStatus
        response.json shouldBe Json.toJson(expectedBody)
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    val input = List(
      ("AA1123A", "2021-22", BAD_REQUEST, NinoFormatError),
      ("AA123456A", "20199", BAD_REQUEST, TaxYearFormatError)
    )
    input foreach (validationErrorTest _).tupled

    "downstream service error" should {
      def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"handle $downstreamCode with status $downstreamStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onError(DownstreamStub.PUT, downstreamUrl, downstreamStatus, errorBody(downstreamCode))
          }

          val response: WSResponse = await(request.post(EmptyBody))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = List(
        (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_TAX_YEAR", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_REGIME", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "BUSINESS_PARTNER_NOT_EXIST", BAD_REQUEST, RuleBusinessPartnerNotExistError),
        (UNPROCESSABLE_ENTITY, "ITSA_CONTRACT_OBJECT_NOT_EXIST", BAD_REQUEST, RuleItsaContractObjectNotExistError),
        (UNPROCESSABLE_ENTITY, "REQUEST_NOT_PROCESSED", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "DUPLICATE_ACKNOWLEDGEMENT_REF", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "OPT_OUT_IND_ALREADY_SET", BAD_REQUEST, RuleAlreadyOptedOutError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_GATEWAY, "BAD_GATEWAY", INTERNAL_SERVER_ERROR, InternalError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
      )
      input.foreach(args => (serviceErrorTest _).tupled(args))
    }
  }

  private trait Test {
    protected val nino    = "AA123456A"
    protected val taxYear = "2023-24"

    val downstreamUrl: String = s"/income-tax/accounts/self-assessment/tax-code/opt-out/ITSA/$nino/2024"

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(s"/$nino/$taxYear/collection/tax-code/coding-out/opt-out")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String =
      s"""
       |{
       |  "failures": [
       |    {
       |      "code": "$code",
       |      "reason": "Some reason"
       |    }
       |  ]
       |}
          """.stripMargin

  }

}
