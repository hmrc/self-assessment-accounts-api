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

package v3.endpoints.retrieveChargeHistoryByTransactionId.def1

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors._
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import support.IntegrationBaseSpec
import v3.retrieveChargeHistoryByChargeReference.def1.model.response.RetrieveChargeHistoryFixture.{downstreamResponseMultiple, mtdMultipleResponseWithHateoas}

class Def1_RetrieveChargeHistoryByTransactionIdSpec extends IntegrationBaseSpec {

  private trait Test {

    protected val transactionId = "23456789"
    protected val nino          = "AA123456A"

    protected val mtdResponseWithHateoas: JsObject = mtdMultipleResponseWithHateoas(nino, transactionId)

    def downstreamUrl: String = s"/cross-regime/charges/NINO/$nino/ITSA"

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def uri: String = s"/$nino/charges/transactionId/$transactionId"

    def errorBody(code: String): String =
      s"""
         |{
         |   "code": "$code",
         |   "reason": "downstream message"
         |}
          """.stripMargin

  }

  "Calling the 'retrieve a charge history' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUrl, OK, downstreamResponseMultiple)
        }

        val response: WSResponse = await(request.get())
        response.status shouldBe OK
        response.json shouldBe mtdResponseWithHateoas
        response.header("Content-Type") shouldBe Some("application/json")
      }

    }
    "return a 500 status code" when {
      "downstream returns errors that map to DownstreamError" in new Test {

        val multipleErrors: String =
          """
                |{
                |   "failures": [
                |        {
                |            "code": "INVALID_IDTYPE",
                |            "reason": "The provided id type is invalid
                |        },
                |        {
                |            "code": "INVALID_REGIME_TYPE",
                |            "reason": "The provided regime type is invalid"
                |        }
                |    ]
                |}
          """.stripMargin

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onError(DownstreamStub.GET, downstreamUrl, BAD_REQUEST, multipleErrors)
        }

        val response: WSResponse = await(request.get())
        response.status shouldBe INTERNAL_SERVER_ERROR
        response.json shouldBe Json.toJson(InternalError)
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {
      def validationErrorTest(requestNino: String, requestTransactionId: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {

          override val nino: String          = requestNino
          override val transactionId: String = requestTransactionId

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request.get())
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        ("AA1123A", "transactionId", BAD_REQUEST, NinoFormatError),
        ("AA123456A", "invalidTransactionId", BAD_REQUEST, TransactionIdFormatError)
      )
      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "des service error" when {
      def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onError(DownstreamStub.GET, downstreamUrl, downstreamStatus, errorBody(downstreamCode))
          }

          val response: WSResponse = await(request.get())
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_IDVALUE", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_REGIME_TYPE", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_DOC_NUMBER", BAD_REQUEST, TransactionIdFormatError),
        (BAD_REQUEST, "INVALID_DATE_FROM", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_DATE_TO", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_DATE_RANGE", INTERNAL_SERVER_ERROR, InternalError),
        (FORBIDDEN, "REQUEST_NOT_PROCESSED", INTERNAL_SERVER_ERROR, InternalError),
        (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
        (UNPROCESSABLE_ENTITY, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "INVALID_REGIME_TYPE", INTERNAL_SERVER_ERROR, InternalError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
      )
      input.foreach(args => (serviceErrorTest _).tupled(args))
    }
  }

}
