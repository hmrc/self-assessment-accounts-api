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

package v1.endpoints

import api.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.fixtures.RetrieveChargeHistoryFixture

class RetrieveChargeHistoryControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String          = "AA111111A"
    val transactionId: String = "anId"
    val correlationId         = "X-123"

    val desResponse: JsValue = RetrieveChargeHistoryFixture.desResponseWithMultipleHHistory
    val mtdResponse: JsValue = RetrieveChargeHistoryFixture.mtdResponseMultipleWithHateoas(nino, transactionId)

    def uri: String    = s"/$nino/charges/$transactionId"
    def desUrl: String = s"/cross-regime/charges/NINO/$nino/ITSA"

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withQueryStringParameters(("docNumber", transactionId))
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def errorBody(code: String): String =
      s"""
           |{
           |   "code": "$code",
           |   "reason": "des message"
           |}
          """.stripMargin

  }

  "Calling the 'retrieve a self assessment charge's history' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, OK, desResponse)
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe OK
        response.json shouldBe mtdResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }

    }
    "return a 500 status code" when {
      "des returns errors that map to DownstreamError" in new Test {

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
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onError(DesStub.GET, desUrl, BAD_REQUEST, multipleErrors)
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe INTERNAL_SERVER_ERROR
        response.json shouldBe Json.toJson(DownstreamError)
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      def validationErrorTest(requestNino: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {

          override val nino: String = requestNino

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request.get)
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        ("AA1123A", BAD_REQUEST, NinoFormatError)
      )
      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "des service error" when {
      def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"des returns an $desCode error and status $desStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onError(DesStub.GET, desUrl, desStatus, errorBody(desCode))
          }

          val response: WSResponse = await(request.get)
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_IDVALUE", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_REGIME_TYPE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_DOC_NUMBER", BAD_REQUEST, TransactionIdFormatError),
        (BAD_REQUEST, "INVALID_DATE_FROM", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_DATE_TO", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_DATE_RANGE", INTERNAL_SERVER_ERROR, DownstreamError),
        (FORBIDDEN, "REQUEST_NOT_PROCESSED", INTERNAL_SERVER_ERROR, DownstreamError),
        (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
        (UNPROCESSABLE_ENTITY, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, DownstreamError),
        (UNPROCESSABLE_ENTITY, "INVALID_REGIME_TYPE", INTERNAL_SERVER_ERROR, DownstreamError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError)
      )
      input.foreach(args => (serviceErrorTest _).tupled(args))
    }
  }

}
