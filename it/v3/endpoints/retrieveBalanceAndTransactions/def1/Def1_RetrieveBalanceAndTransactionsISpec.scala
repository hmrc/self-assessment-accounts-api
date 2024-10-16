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

package v3.endpoints.retrieveBalanceAndTransactions.def1

import api.models.errors._
import api.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v3.retrieveBalanceAndTransactions.def1.model.RequestFixture._
import v3.retrieveBalanceAndTransactions.def1.model.ResponseFixture._

class Def1_RetrieveBalanceAndTransactionsISpec extends IntegrationBaseSpec {

  "Calling the 'retrieve a charge history' endpoint" when {
    "any valid request is made with doc number, fromDate, toDate and all flag params as false" should {
      "return a 200 status code" in new Test {
        override val includeLocks: Option[String]               = Some("false")
        override val calculateAccruedInterest: Option[String]   = Some("false")
        override val removePOA: Option[String]                  = Some("false")
        override val customerPaymentInformation: Option[String] = Some("false")
        override val includeEstimatedCharges: Option[String]    = Some("false")

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUrl, ifsQueryParams, OK, downstreamResponseJson)
        }

        val response: WSResponse = await(request.get())
        response.status shouldBe OK
        response.json shouldBe mtdResponseWithoutLocksJson
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made with only doc number and all flag params (except onlyOpenItems) as true" in new Test {
        override val fromDate: Option[String] = None
        override val toDate: Option[String]   = None

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUrl, ifsQueryParams, OK, downstreamResponseJson)
        }

        val response: WSResponse = await(request.get())
        response.status shouldBe OK
        response.json shouldBe mtdResponseJson
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made with no doc number, but with fromDate, toDate and all flag params (except onlyOpenItems) as true" in new Test {

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUrl, ifsQueryParams, OK, downstreamResponseJson)
        }

        val response: WSResponse = await(request.get())
        response.status shouldBe OK
        response.json shouldBe mtdResponseJson
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }
  }

  "return error according to spec" when {

    def validationErrorTest(requestNino: String,
                            requestDocNumber: Option[String],
                            requestFromDate: Option[String],
                            requestToDate: Option[String],
                            requestOnlyOpenItems: Option[String],
                            requestIncludeLocks: Option[String],
                            requestCalculateAccruedInterest: Option[String],
                            requestRemovePOA: Option[String],
                            requestCustomerPaymentInformation: Option[String],
                            requestIncludeEstimatedCharges: Option[String],
                            expectedStatus: Int,
                            expectedBody: MtdError): Unit = {

      s"validation fails with ${expectedBody.code} error" in new Test {

        override protected val nino: String = requestNino

        override protected val docNumber: Option[String]                  = requestDocNumber
        override protected val fromDate: Option[String]                   = requestFromDate
        override protected val toDate: Option[String]                     = requestToDate
        override protected val onlyOpenItems: Option[String]              = requestOnlyOpenItems
        override protected val includeLocks: Option[String]               = requestIncludeLocks
        override protected val calculateAccruedInterest: Option[String]   = requestCalculateAccruedInterest
        override protected val removePOA: Option[String]                  = requestRemovePOA
        override protected val customerPaymentInformation: Option[String] = requestCustomerPaymentInformation
        override protected val includeEstimatedCharges: Option[String]    = requestIncludeEstimatedCharges

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

      // format: off
      val input = List(
        ("AA1123A", Some(validDocNumber), Some(validFromDate), Some(validToDate), None, None, None, None, None, None, BAD_REQUEST, NinoFormatError),
        ("AA123456A", Some("a" * 13), Some(validFromDate), Some(validToDate), None, None, None, None, None, None, BAD_REQUEST, DocNumberFormatError),
        ("AA123456A", Some(validDocNumber), Some(validFromDate), Some(validToDate), Some("invalid"), None, None, None, None, None, BAD_REQUEST, OnlyOpenItemsFormatError),
        ("AA123456A", Some(validDocNumber), Some(validFromDate), Some(validToDate), None, Some("invalid"), None, None, None, None, BAD_REQUEST, IncludeLocksFormatError),
        ("AA123456A", Some(validDocNumber), Some(validFromDate), Some(validToDate), None, None, Some("invalid"), None, None, None, BAD_REQUEST, CalculateAccruedInterestFormatError),
        ("AA123456A", Some(validDocNumber), Some(validFromDate), Some(validToDate), None, None, None, None, Some("invalid"), None, BAD_REQUEST, CustomerPaymentInformationFormatError),
        ("AA123456A", Some(validDocNumber), Some("invalid"), Some(validToDate), None, None, None, None, None, None, BAD_REQUEST, FromDateFormatError),
        ("AA123456A", Some(validDocNumber), Some(validFromDate), Some("invalid"), None, None, None, None, None, None, BAD_REQUEST, ToDateFormatError),
        ("AA123456A", Some(validDocNumber), Some(validFromDate), Some(validToDate), None, None, None, Some("invalid"), None, None, BAD_REQUEST, RemovePaymentOnAccountFormatError),
        ("AA123456A", Some(validDocNumber), Some(validFromDate), Some(validToDate), None, None, None, None, None, Some("invalid"), BAD_REQUEST, IncludeEstimatedChargesFormatError),
        ("AA123456A", Some(validDocNumber), Some(validToDate), Some(validFromDate), None, None, None, None, None, None, BAD_REQUEST, RangeToDateBeforeFromDateError),
        ("AA123456A", Some(validDocNumber), Some(validToDate), None, None, None, None, None, None, None, BAD_REQUEST, RuleMissingToDateError),
        ("AA123456A", Some(validDocNumber), None, Some(validFromDate), None, None, None, None, None, None, BAD_REQUEST, MissingFromDateError),
        ("AA123456A", Some(validDocNumber), Some(validFromDate), Some(validToDate), Some("true"), None, None, None, None, None, BAD_REQUEST, RuleInconsistentQueryParamsError)

      )
      // format: on

    input.foreach((validationErrorTest _).tupled)
  }

  "downstream service error" should {
    def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
      s"handle $downstreamCode with status $downstreamStatus" in new Test {

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

    val input = List(
      (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
      (BAD_REQUEST, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, InternalError),
      (BAD_REQUEST, "INVALID_IDNUMBER", BAD_REQUEST, NinoFormatError),
      (BAD_REQUEST, "INVALID_REGIME_TYPE", INTERNAL_SERVER_ERROR, InternalError),
      (BAD_REQUEST, "INVALID_DOC_NUMBER", BAD_REQUEST, DocNumberFormatError),
      (BAD_REQUEST, "INVALID_ONLY_OPEN_ITEMS", BAD_REQUEST, OnlyOpenItemsFormatError),
      (BAD_REQUEST, "INVALID_INCLUDE_LOCKS", BAD_REQUEST, IncludeLocksFormatError),
      (BAD_REQUEST, "INVALID_CALCULATE_ACCRUED_INTEREST", BAD_REQUEST, CalculateAccruedInterestFormatError),
      (BAD_REQUEST, "INVALID_CUSTOMER_PAYMENT_INFORMATION", BAD_REQUEST, CustomerPaymentInformationFormatError),
      (BAD_REQUEST, "INVALID_DATE_FROM", BAD_REQUEST, FromDateFormatError),
      (BAD_REQUEST, "INVALID_DATE_TO", BAD_REQUEST, ToDateFormatError),
      (BAD_REQUEST, "INVALID_DATE_RANGE", BAD_REQUEST, RuleInvalidDateRangeError),
      (BAD_REQUEST, "INVALID_REQUEST", BAD_REQUEST, RuleInconsistentQueryParamsError),
      (BAD_REQUEST, "INVALID_REMOVE_PAYMENT_ON_ACCOUNT", BAD_REQUEST, RemovePaymentOnAccountFormatError),
      (BAD_REQUEST, "INVALID_INCLUDE_STATISTICAL", BAD_REQUEST, IncludeEstimatedChargesFormatError),
      (FORBIDDEN, "REQUEST_NOT_PROCESSED", INTERNAL_SERVER_ERROR, InternalError),
      (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
      (UNPROCESSABLE_ENTITY, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, InternalError),
      (UNPROCESSABLE_ENTITY, "INVALID_REGIME_TYPE", INTERNAL_SERVER_ERROR, InternalError),
      (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
      (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
    )
    input.foreach(args => (serviceErrorTest _).tupled(args))
  }

  private trait Test {

    protected val nino = "AA123456A"

    protected val docNumber: Option[String]                  = Some("1234")
    protected val fromDate: Option[String]                   = Some("2022-08-15")
    protected val toDate: Option[String]                     = Some("2022-09-15")
    protected val onlyOpenItems: Option[String]              = Some("false")
    protected val includeLocks: Option[String]               = Some("true")
    protected val calculateAccruedInterest: Option[String]   = Some("true")
    protected val removePOA: Option[String]                  = Some("true")
    protected val customerPaymentInformation: Option[String] = Some("true")
    protected val includeEstimatedCharges: Option[String]    = Some("true")

    def downstreamUrl: String = s"/enterprise/02.00.00/financial-data/NINO/$nino/ITSA"

    def setupStubs(): StubMapping

    def request: WSRequest = {
      val queryParams =
        List(
          "docNumber"                  -> docNumber,
          "fromDate"                   -> fromDate,
          "toDate"                     -> toDate,
          "onlyOpenItems"              -> onlyOpenItems,
          "includeLocks"               -> includeLocks,
          "calculateAccruedInterest"   -> calculateAccruedInterest,
          "removePOA"                  -> removePOA,
          "customerPaymentInformation" -> customerPaymentInformation,
          "includeEstimatedCharges"    -> includeEstimatedCharges
        )
          .collect { case (k, Some(v)) => (k, v) }

      setupStubs()
      buildRequest(uri)
        .addQueryStringParameters(queryParams: _*)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.3.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def uri: String = s"/$nino/balance-and-transactions"

    def errorBody(code: String): String =
      s"""
         |{
         |   "code": "$code",
         |   "reason": "downstream message"
         |}
          """.stripMargin

    def ifsQueryParams: Map[String, String] = Map(
      "docNumber"                  -> docNumber,
      "dateFrom"                   -> fromDate,
      "dateTo"                     -> toDate,
      "onlyOpenItems"              -> onlyOpenItems,
      "includeLocks"               -> includeLocks,
      "calculateAccruedInterest"   -> calculateAccruedInterest,
      "removePOA"                  -> removePOA,
      "customerPaymentInformation" -> customerPaymentInformation,
      "includeStatistical"         -> includeEstimatedCharges
    ).collect { case (k, Some(v)) => (k, v) }

  }

}
