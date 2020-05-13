/*
 * Copyright 2020 HM Revenue & Customs
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
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v1.fixtures.ListTransactionsFixture._
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class ListTransactionsControllerISpec extends IntegrationBaseSpec {

  private val desJsonNoTransactions = Json.parse(
    """
      |{
      |  "financialDetails" : [
      |  ]
      |}
    """.stripMargin
  )

  private val desJson = Json.parse(
    """
      |{
      |   "financialDetails":[
      |      {
      |         "taxYear":"2020",
      |         "documentId":"X123456790A",
      |         "documentDate":"2020-01-01",
      |         "documentDescription":"Balancing Charge Debit",
      |         "totalAmount":12.34,
      |         "documentOutstandingAmount":10.33,
      |         "lastClearingDate":"2020-01-02",
      |         "lastClearingReason":"Refund",
      |         "lastClearedAmount":2.01
      |      },
      |      {
      |         "taxYear":"2020",
      |         "documentId":"X123456790B",
      |         "paymentLot":"081203010024",
      |         "paymentLotItem" : "000001",
      |         "documentDate":"2020-01-01",
      |         "documentDescription":"Payment On Account",
      |         "totalAmount":12.34,
      |         "documentOutstandingAmount":10.33,
      |         "lastClearingDate":"2020-01-02",
      |         "lastClearingReason":"Payment Allocation",
      |         "lastClearedAmount":2.01
      |      }
      |   ]
      |}
    """.stripMargin
  )

  private trait Test {
    val nino = "AA123456A"
    val correlationId = "X-123"
    val from: Option[String] = Some("2018-10-01")
    val to: Option[String] = Some("2019-10-01")

    def desUrl: String = s"/cross-regime/transactions-placeholder/NINO/$nino/ITSA"

    def setupStubs(): StubMapping

    def request: WSRequest = {

      val queryParams = Seq("from" -> from, "to" -> to)
        .collect {
          case (k, Some(v)) => (k, v)
        }
      setupStubs()
      buildRequest(uri)
        .addQueryStringParameters(queryParams: _*)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }

    def uri: String = s"/$nino/transactions"
  }

  "Calling the list transactions endpoint" should {
    "return a valid response with status OK" when {
      "valid request is made" in new Test {

        val desQueryParams: Map[String, String] = Map("dateFrom" -> from.get, "dateTo" -> to.get)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, desQueryParams, OK, desJson)
        }

        val response: WSResponse = await(request.get)

        response.status shouldBe OK
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe mtdJson
      }
    }

    "return a 500 status code" when {
      "des returns errors that map to DownstreamError" in new Test {

        val desQueryParams: Map[String, String] = Map("dateFrom" -> from.get, "dateTo" -> to.get)

        val multipleErrors: String =
          """
            |{
            |   "failures": [
            |        {
            |            "code": "INVALID_IDTYPE",
            |            "reason": "The provided id type is invalid"
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
          DesStub.onError(DesStub.GET, desUrl, desQueryParams, BAD_REQUEST, multipleErrors)
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe INTERNAL_SERVER_ERROR
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe Json.toJson(DownstreamError)
      }
    }

    "return a 404 NO_TRANSACTIONS_FOUND error" when {
      "a success response with no payments is returned" in new Test {

        val desQueryParams: Map[String, String] = Map("dateFrom" -> from.get, "dateTo" -> to.get)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, desQueryParams, OK, desJsonNoTransactions)
        }

        val response: WSResponse = await(request.get)

        response.status shouldBe NOT_FOUND
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe Json.toJson(NoTransactionsFoundError)
      }
    }

    "return error according to spec" when {

      def validationErrorTest(requestNino: String, fromDate: String,
                              toDate: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {

          override val nino: String = requestNino
          override val from: Option[String] = Some(fromDate)
          override val to: Option[String] = Some(toDate)

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
        ("AA1123A", "2018-10-01", "2019-10-01", BAD_REQUEST, NinoFormatError),
        ("AA123456A", "2018-100-01", "2019-10-01", BAD_REQUEST, FromDateFormatError),
        ("AA123456A", "2018-10-01", "2019-100-01", BAD_REQUEST, ToDateFormatError),
        ("AA123456A", "2018-03-01", "2019-10-01", BAD_REQUEST, RuleFromDateNotSupportedError),
        ("AA123456A", "2018-10-01", "2021-10-01", BAD_REQUEST, RuleDateRangeInvalidError),
        ("AA123456A", "2018-10-01", "2018-06-01", BAD_REQUEST, RangeToDateBeforeFromDateError)
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

          val desQueryParams: Map[String, String] = Map("dateFrom" -> from.get, "dateTo" -> to.get)

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onError(DesStub.GET, desUrl, desQueryParams, desStatus, errorBody(desCode))
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
        (BAD_REQUEST, "INVALID_DOC_NUMBER", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_ONLY_OPEN_ITEMS", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_INCLUDE_LOCKS", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_CALCULATE_ACCRUED_INTEREST", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_CUSTOMER_PAYMENT_INFORMATION", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_DATE_FROM", BAD_REQUEST, FromDateFormatError),
        (BAD_REQUEST, "INVALID_DATE_TO", BAD_REQUEST, ToDateFormatError),
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
