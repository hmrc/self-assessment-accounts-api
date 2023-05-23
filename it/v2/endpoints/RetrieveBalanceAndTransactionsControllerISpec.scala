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
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v2.fixtures.retrieveBalanceAndTransactions.RequestFixture._
import v2.fixtures.retrieveBalanceAndTransactions.ResponseFixture._
import v2.stubs.DownstreamStub

class RetrieveBalanceAndTransactionsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String                               = "AA123456A"
    val docNumber: Option[String]                  = Some("1234")
    val fromDate: Option[String]                   = Some("2022-08-15")
    val toDate: Option[String]                     = Some("2022-09-15")
    val onlyOpenItems: Option[String]              = Some("true")
    val includeLocks: Option[String]               = Some("true")
    val calculateAccruedInterest: Option[String]   = Some("true")
    val removePOA: Option[String]                  = Some("true")
    val customerPaymentInformation: Option[String] = Some("true")
    val includeEstimatedCharges: Option[String]    = Some("true")

    def uri: String           = s"/$nino/balance-and-transactions"
    def downstreamUrl: String = s"/enterprise/02.00.00/financial-data/NINO/$nino/ITSA"

    def setupStubs(): StubMapping

    def request: WSRequest = {
      val queryParams =
        Seq(
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
          .collect { case (k, Some(v)) =>
            (k, v)
          }

      setupStubs()
      buildRequest(uri)
        .addQueryStringParameters(queryParams: _*)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

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
    ).collect { case (k, Some(v)) =>
      (k, v)
    }

  }

  "Calling the 'retrieve a charge history' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made with doc number, fromDate, toDate and all flag params as false" in new Test {

        override val onlyOpenItems: Option[String]              = Some("false")
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
        override val onlyOpenItems: Option[String]              = Some("false")

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

        override val onlyOpenItems: Option[String]              = Some("false")

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

          override val nino: String                               = requestNino
          override val docNumber: Option[String]                  = requestDocNumber
          override val fromDate: Option[String]                   = requestFromDate
          override val toDate: Option[String]                     = requestToDate
          override val onlyOpenItems: Option[String]              = requestOnlyOpenItems
          override val includeLocks: Option[String]               = requestIncludeLocks
          override val calculateAccruedInterest: Option[String]   = requestCalculateAccruedInterest
          override val removePOA: Option[String]                  = requestRemovePOA
          override val customerPaymentInformation: Option[String] = requestCustomerPaymentInformation
          override val includeEstimatedCharges: Option[String]    = requestIncludeEstimatedCharges

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
      val input = Seq(
        ("AA1123A", Some(validDocNumber), Some(validFromDate), Some(validToDate), None, None, None, None, None, None, BAD_REQUEST, NinoFormatError),
        ("AA123456A", Some("a" * 13), Some(validFromDate), Some(validToDate), None, None, None, None, None, None, BAD_REQUEST, DocNumberFormatError),
        ("AA123456A", Some(validDocNumber), Some(validFromDate), Some(validToDate), Some("invalid"), None, None, None, None, None, BAD_REQUEST, OnlyOpenItemsFormatError),
        ("AA123456A", Some(validDocNumber), Some(validFromDate), Some(validToDate), None, Some("invalid"), None, None, None, None, BAD_REQUEST, IncludeLocksFormatError),
        ("AA123456A", Some(validDocNumber), Some(validFromDate), Some(validToDate), None, None, Some("invalid"), None, None, None, BAD_REQUEST, CalculateAccruedInterestFormatError),
        ("AA123456A", Some(validDocNumber), Some(validFromDate), Some(validToDate), None, None, None, None, Some("invalid"), None, BAD_REQUEST, CustomerPaymentInformationFormatError),
        ("AA123456A", Some(validDocNumber), Some("invalid"), Some(validToDate), None, None, None, None, None, None, BAD_REQUEST, FromDateFormatError),
        ("AA123456A", Some(validDocNumber), Some(validFromDate), Some("invalid"), None, None, None, None, None, None, BAD_REQUEST, ToDateFormatError),
        ("AA123456A", Some(validDocNumber), Some(validToDate), Some(validFromDate), None, None, None, Some("invalid"), None, None, BAD_REQUEST, RemovePaymentOnAccountFormatError),
        ("AA123456A", Some(validDocNumber), Some(validFromDate), Some(validToDate), None, None, None, None, None, Some("invalid"), BAD_REQUEST, IncludeEstimatedChargesFormatError),
        ("AA123456A", Some(validDocNumber), Some(validToDate), Some(validFromDate), None, None, None, None, None, None, BAD_REQUEST, RangeToDateBeforeFromDateError),
        ("AA123456A", Some(validDocNumber), Some(validToDate), None, None, None, None, None, None, None, BAD_REQUEST, RuleMissingToDateError),
        ("AA123456A", Some(validDocNumber), None, Some(validFromDate), None, None, None, None, None, None, BAD_REQUEST, MissingFromDateError),
        ("AA123456A", Some(validDocNumber), Some(validFromDate), Some(validToDate), Some("true"), None, None, None, None, None, BAD_REQUEST, RuleInconsistentQueryParamsError)

      )
      // format: on

      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "des service error" when {
      def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {

          override val onlyOpenItems: Option[String]              = Some("false")

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
  }

}

//Return inconsistent query params if onlyOPenItems = true and toDate/fromDate is provided
