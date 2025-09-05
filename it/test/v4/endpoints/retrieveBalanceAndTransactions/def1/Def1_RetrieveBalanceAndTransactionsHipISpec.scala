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

package v4.endpoints.retrieveBalanceAndTransactions.def1

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.errors.*
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors.*
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import shared.support.IntegrationBaseSpec
import v4.retrieveBalanceAndTransactions.def1.model.RequestFixture.*
import v4.retrieveBalanceAndTransactions.def1.model.ResponseFixture.*

class Def1_RetrieveBalanceAndTransactionsHipISpec extends IntegrationBaseSpec {

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
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUrl, hipQueryParams, OK, downstreamResponseHipJson)
        }

        val response: WSResponse = await(request.get())
        response.status shouldBe OK
        response.json shouldBe mtdResponseWithoutLocksJsonHip
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made with only doc number and all flag params (except onlyOpenItems) as true" in new Test {
        override val fromDate: Option[String] = None
        override val toDate: Option[String]   = None

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUrl, hipQueryParams, OK, downstreamResponseHipJson)
        }

        val response: WSResponse = await(request.get())
        response.status shouldBe OK
        response.json shouldBe mtdResponseJsonHip
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made with no doc number, but with fromDate, toDate and all flag params (except onlyOpenItems) as true" in new Test {

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUrl, hipQueryParams, OK, downstreamResponseHipJson)
        }

        val response: WSResponse = await(request.get())
        response.status shouldBe OK
        response.json shouldBe mtdResponseJsonHip
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
        ("AA123456A", Some(validDocNumber), Some(validFromDate), Some(validToDate), Some("true"), None, None, None, None, None, BAD_REQUEST, RuleInconsistentQueryParamsError),
        ("AA123456A", Some(validDocNumber), Some(validFromDate), Some(outOfRangeEndDate), None, None, None, None, None, None, BAD_REQUEST, RuleInvalidDateRangeError)

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
      (UNPROCESSABLE_ENTITY, "002", INTERNAL_SERVER_ERROR, InternalError),
      (UNPROCESSABLE_ENTITY, "003", INTERNAL_SERVER_ERROR, InternalError),
      (UNPROCESSABLE_ENTITY, "005", NOT_FOUND, NotFoundError),
      (UNPROCESSABLE_ENTITY, "015", INTERNAL_SERVER_ERROR, InternalError)
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

    def downstreamUrl: String = s"/etmp/RESTAdapter/itsa/taxpayer/financial-details"

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
          (ACCEPT, "application/vnd.hmrc.4.0+json"),
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

    def baseHipQueryParams: Map[String, String] = Map(
      "sapDocumentNumber"          -> docNumber,
      "dateFrom"                   -> fromDate,
      "dateTo"                     -> toDate,
      "onlyOpenItems"              -> onlyOpenItems,
      "includeLocks"               -> includeLocks,
      "calculateAccruedInterest"   -> calculateAccruedInterest,
      "removePaymentonAccount"     -> removePOA,
      "customerPaymentInformation" -> customerPaymentInformation,
      "includeStatistical"         -> includeEstimatedCharges
    ).collect { case (k, Some(v)) => (k, v) }

    def extraHipQueryParams: Map[String, String] = Map(
      "idNumber"   -> nino,
      "idType"     -> "NINO",
      "regimeType" -> "ITSA"
    )

    def hipQueryParams: Map[String, String] = baseHipQueryParams ++ extraHipQueryParams
  }

}
