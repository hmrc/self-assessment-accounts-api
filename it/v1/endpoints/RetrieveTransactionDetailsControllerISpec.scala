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

import api.models.errors._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.fixture.RetrieveTransactionDetailsFixture
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class RetrieveTransactionDetailsControllerISpec extends IntegrationBaseSpec with RetrieveTransactionDetailsFixture {

  private trait Test {

    val nino          = "AA123456A"
    val correlationId = "X-123"
    val transactionId = "1111111111"

    val desQueryParams: Seq[(String, String)] = Seq(
      "docNumber"                  -> transactionId,
      "onlyOpenItems"              -> "false",
      "includeLocks"               -> "true",
      "calculateAccruedInterest"   -> "true",
      "removePOA"                  -> "false",
      "customerPaymentInformation" -> "true",
      "includeStatistical"         -> "false"
    )

    def desUrl: String = s"/enterprise/02.00.00/financial-data/NINO/$nino/ITSA"

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withQueryStringParameters(desQueryParams: _*)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def uri: String = s"/$nino/transactions/$transactionId"
  }

  "Calling the retrieve transaction details endpoint" should {
    "return a valid response with status OK" when {
      "valid request is made for a charge" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, OK, desJsonCharge)
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe OK
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe mtdJsonCharge
      }

      "valid request is made for a payment" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, OK, desJsonPayment)
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe OK
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe mtdJsonPayment
      }
    }

    "return a 404 NO_DETAILS_FOUND error" when {
      "a success response with no data is returned" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, OK, desJsonNoTransactions)
        }

        val response: WSResponse = await(request.get)

        response.status shouldBe NOT_FOUND
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe Json.toJson(NoTransactionDetailsFoundError)
      }

      "a success response with no relevant data is returned" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, OK, desJsonNoRelevantTransactions)
        }

        val response: WSResponse = await(request.get)

        response.status shouldBe NOT_FOUND
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe Json.toJson(NoTransactionDetailsFoundError)
      }
    }

    "return error according to spec" when {

      def validationErrorTest(requestNino: String, requestTransactionId: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {

          override val nino: String          = requestNino
          override val transactionId: String = requestTransactionId

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
        ("a", "11111", BAD_REQUEST, NinoFormatError),
        ("AA123456A", "1111111111111111111111111111", BAD_REQUEST, TransactionIdFormatError)
      )
      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "des service error" when {

      def errorBody(code: String): String =
        s"""{
           |  "code": "$code",
           |  "reason": "des message"
           |}""".stripMargin

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
        (BAD_REQUEST, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_IDNUMBER", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_REGIME_TYPE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_DOC_NUMBER", BAD_REQUEST, TransactionIdFormatError),
        (BAD_REQUEST, "INVALID_ONLY_OPEN_ITEMS", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_INCLUDE_LOCKS", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_CALCULATE_ACCRUED_INTEREST", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_CUSTOMER_PAYMENT_INFORMATION", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_DATE_FROM", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_DATE_TO", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_DATE_RANGE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_REQUEST", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_INCLUDE_STATISTICAL", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_REMOVE_PAYMENT_ON_ACCOUNT", INTERNAL_SERVER_ERROR, DownstreamError),
        (FORBIDDEN, "REQUEST_NOT_PROCESSED", INTERNAL_SERVER_ERROR, DownstreamError),
        (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError)
      )
      input.foreach(args => (serviceErrorTest _).tupled(args))
    }
  }

}
