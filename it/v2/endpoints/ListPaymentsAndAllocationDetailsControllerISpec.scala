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

package v2.endpoints

import api.models.errors._
import api.stubs.{AuditStub, AuthStub, MtdIdLookupStub}
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v2.fixtures.listPaymentsAndAllocationDetails.ResponseFixtures._
import v2.stubs.DownstreamStub

class ListPaymentsAndAllocationDetailsControllerISpec extends IntegrationBaseSpec {

  private trait Test {
    val nino                           = "AA123456A"
    val fromDate: Option[String]       = None
    val toDate: Option[String]         = None
    val paymentLot: Option[String]     = None
    val paymentLotItem: Option[String] = None

    def downstreamUrl: String = s"/cross-regime/payment-allocation/NINO/$nino/ITSA"

    def setupStubs(): Unit = ()

    def request: WSRequest = {

      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()

      val queryParams = Seq("fromDate" -> fromDate, "toDate" -> toDate, "paymentLot" -> paymentLot, "paymentLotItem" -> paymentLotItem)
        .collect { case (k, Some(v)) => (k, v) }

      buildRequest(s"/$nino/payments-and-allocations")
        .addQueryStringParameters(queryParams: _*)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

  }

  "Calling the list payments endpoint" should {
    "return a valid response with status OK" when {
      "a valid request with no query parameters is made" in new Test {
        override def setupStubs(): Unit =
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUrl, OK, responseDownstreamJson)

        val response: WSResponse = await(request.get)

        response.status shouldBe OK
        response.json shouldBe mtdResponseJson
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "a valid request with all query parameters is made" in new Test {
        override val fromDate: Option[String]       = Some("2020-01-01")
        override val toDate: Option[String]         = Some("2020-02-01")
        override val paymentLot: Option[String]     = Some("SomeLot")
        override val paymentLotItem: Option[String] = Some("000001")

        override def setupStubs(): Unit = {
          val queryParams = Map("paymentLot" -> "SomeLot", "paymentLotItem" -> "000001", "dateFrom" -> "2020-01-01", "dateTo" -> "2020-02-01")
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUrl, queryParams, OK, responseDownstreamJson)
        }

        val response: WSResponse = await(request.get)

        response.status shouldBe OK
        response.json shouldBe mtdResponseJson
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      def validationErrorTest(requestNino: String,
                              requestFromDate: Option[String],
                              requestToDate: Option[String],
                              requestLot: Option[String],
                              requestLotItem: Option[String],
                              expectedStatus: Int,
                              expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {

          override val nino: String                   = requestNino
          override val fromDate: Option[String]       = requestFromDate
          override val toDate: Option[String]         = requestToDate
          override val paymentLot: Option[String]     = requestLot
          override val paymentLotItem: Option[String] = requestLotItem

          val response: WSResponse = await(request.get)
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        ("BAD_NINO", None, None, None, None, BAD_REQUEST, NinoFormatError),
        ("AA123456A", Some("BAD_DATE"), Some("2020-01-01"), None, None, BAD_REQUEST, FromDateFormatError),
        ("AA123456A", Some("2020-01-01"), Some("BAD_DATE"), None, None, BAD_REQUEST, ToDateFormatError),
        ("AA123456A", Some("2020-02-01"), Some("2020-01-01"), None, None, BAD_REQUEST, RangeToDateBeforeFromDateError),
        ("AA123456A", None, Some("2020-02-01"), None, None, BAD_REQUEST, MissingFromDateError),
        ("AA123456A", Some("2020-02-01"), None, None, None, BAD_REQUEST, RuleMissingToDateError),
        ("AA123456A", None, None, Some("BAD_LOT"), Some("000001"), BAD_REQUEST, PaymentLotFormatError),
        ("AA123456A", None, None, None, Some("BAD_LOT_ITEM"), BAD_REQUEST, PaymentLotItemFormatError),
        ("AA123456A", None, None, None, Some("000001"), BAD_REQUEST, MissingPaymentLotError),
        ("AA123456A", None, None, Some("AA123456aa1"), None, BAD_REQUEST, MissingPaymentLotItemError),
       // ("AA123456A", Some("2020-01-01"), Some("2021-01-01"), Some("AA123456aa1"), Some("000001"), BAD_REQUEST, RuleInconsistentQueryParamsMtdError)
      )
      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "a downstream service error occurs" when {

      def errorBody(code: String): String =
        s"""{
           |  "code": "$code",
           |  "reason": "message"
           |}""".stripMargin

      def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {

          override def setupStubs(): Unit = {
            DownstreamStub.onError(DownstreamStub.GET, downstreamUrl, downstreamStatus, errorBody(downstreamCode))
          }

          val response: WSResponse = await(request.get)
          response.json shouldBe Json.toJson(expectedBody)
          response.status shouldBe expectedStatus
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_IDVALUE", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_REGIME_TYPE", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_PAYMENT_LOT", BAD_REQUEST, PaymentLotFormatError),
        (BAD_REQUEST, "INVALID_PAYMENT_LOT_ITEM", BAD_REQUEST, PaymentLotItemFormatError),
        (BAD_REQUEST, "INVALID_CLEARING_DOC", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_DATE_FROM", BAD_REQUEST, FromDateFormatError),
        (BAD_REQUEST, "INVALID_DATE_TO", BAD_REQUEST, ToDateFormatError),
        (BAD_REQUEST, "INVALID_DATE_RANGE", BAD_REQUEST, RuleInvalidDateRangeError),
        (BAD_REQUEST, "INVALID_REQUEST", BAD_REQUEST, RuleInconsistentQueryParamsErrorListSA),
        (BAD_REQUEST, "REQUEST_NOT_PROCESSED", BAD_REQUEST, BadRequestError),
        (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
        (BAD_REQUEST, "PARTIALLY_MIGRATED", BAD_REQUEST, BadRequestError),
        (UNPROCESSABLE_ENTITY, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, InternalError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
      )

      input.foreach(args => (serviceErrorTest _).tupled(args))
    }
  }

}
