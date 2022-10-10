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

    val nino: String               = "AA123456A"
    val docNumber: Option[String] = Some("1234")
    val dateFrom: Option[String]  = Some("2022-08-15")
    val dateTo: Option[String]    = Some("2022-09-15")
    val onlyOpenItems: Option[String] = Some("true")
    val includeLocks: Option[String] = Some("true")
    val calculateAccruedInterest: Option[String] = Some("true")
    val removePOA: Option[String] = Some("true")
    val customerPaymentInformation: Option[String] = Some("true")
    val includeChargeEstimate: Option[String] = Some("true")

    def uri: String           = s"/$nino/balance-and-transactions"
    def downstreamUrl: String = s"/enterprise/02.00.00/financial-data/NINO/$nino/ITSA"

    def queryParams: Seq[(String, String)] =
      Seq("docNumber" -> docNumber,
          "dateFrom"  -> dateFrom,
          "dateTo"    -> dateTo,
          "onlyOpenItems" -> onlyOpenItems,
          "includeLocks" -> includeLocks,
          "calculateAccruedInterest" -> calculateAccruedInterest,
          "removePOA" -> removePOA,
          "customerPaymentInformation" -> customerPaymentInformation,
          "includeChargeEstimate" -> includeChargeEstimate
          )
        .collect { case (k, Some(v)) =>
          (k, v)
        }

    def setupStubs(): StubMapping

    def request(queryParams: Seq[(String, String)]): WSRequest = {
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
      "dateFrom"                   -> dateFrom,
      "dateTo"                     -> dateTo,
      "onlyOpenItems"              -> onlyOpenItems,
      "includeLocks"               -> includeLocks,
      "calculateAccruedInterest"   -> calculateAccruedInterest,
      "removePOA"                  -> removePOA,
      "customerPaymentInformation" -> customerPaymentInformation,
      "includeChargeEstimate"      -> includeChargeEstimate
    ).collect { case (k, Some(v)) =>
      (k, v)
    }

  }

  "Calling the 'retrieve a charge history' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made with doc number, dateFrom, dateTo and all flag params as false" in new Test {

        override val onlyOpenItems: Option[String] = Some("false")
        override val includeLocks: Option[String] = Some("false")
        override val calculateAccruedInterest: Option[String] = Some("false")
        override val removePOA: Option[String] = Some("false")
        override val customerPaymentInformation: Option[String] = Some("false")
        override val includeChargeEstimate: Option[String] = Some("false")

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUrl, ifsQueryParams, OK, downstreamResponseJson)
        }

        val response: WSResponse = await(request(queryParams).get)
        response.status shouldBe OK
        response.json shouldBe mtdResponseJson
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made with only doc number and all flag params as true" in new Test {

        override val dateFrom: Option[String] = None
        override val dateTo: Option[String] = None

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUrl, ifsQueryParams, OK, downstreamResponseJson)
        }

        val response: WSResponse = await(request(queryParams).get)
        response.status shouldBe OK
        response.json shouldBe mtdResponseJson
        response.header("Content-Type") shouldBe Some("application/json")
      }

      "any valid request is made with no doc number, but with dateFrom, dateTo and all flag params as true" in new Test {

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUrl, ifsQueryParams, OK, downstreamResponseJson)
        }

        val response: WSResponse = await(request(queryParams).get)
        response.status shouldBe OK
        response.json shouldBe mtdResponseJson
        response.header("Content-Type") shouldBe Some("application/json")
      }
    }

    "return error according to spec" when {

      def validationErrorTest(requestNino: String,
                              requestDocNumber: Option[String],
                              requestDateFrom: Option[String],
                              requestDateTo: Option[String],
                              requestOnlyOpenItems: Option[String],
                              requestIncludeLocks: Option[String],
                              requestCalculateAccruedInterest: Option[String],
                              requestRemovePOA: Option[String],
                              requestCustomerPaymentInformation: Option[String],
                              requestIncludeChargeEstimate: Option[String],
                              expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {

          override val nino: String = requestNino
          override val docNumber: Option[String] = requestDocNumber
          override val dateFrom: Option[String]  = requestDateFrom
          override val dateTo: Option[String]    = requestDateTo
          override val onlyOpenItems: Option[String] = requestOnlyOpenItems
          override val includeLocks: Option[String] = requestIncludeLocks
          override val calculateAccruedInterest: Option[String] = requestCalculateAccruedInterest
          override val removePOA: Option[String] = requestRemovePOA
          override val customerPaymentInformation: Option[String] = requestCustomerPaymentInformation
          override val includeChargeEstimate: Option[String] = requestIncludeChargeEstimate

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request(queryParams).get)
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        ("AA1123A",Some(validDocNumber), Some(validDateFrom),Some(validDateTo),None,None,None,None,None,None, BAD_REQUEST, NinoFormatError),
        ("AA123456A",Some("a" * 13), Some(validDateFrom),Some(validDateTo),None,None,None,None,None,None, BAD_REQUEST, InvalidDocNumberError),
        ("AA123456A",Some(validDocNumber), Some("invalid"),Some(validDateTo),None,None,None,None,None,None, BAD_REQUEST, InvalidDateFromError),
        ("AA123456A",Some(validDocNumber), Some(validDateFrom),Some("invalid"),None,None,None,None,None,None, BAD_REQUEST, InvalidDateToError),
        ("AA123456A",Some(validDocNumber), Some(validDateFrom),Some(validDateTo),Some("invalid"),None,None,None,None,None, BAD_REQUEST, InvalidOnlyOpenItemsError),
        ("AA123456A",Some(validDocNumber), Some(validDateFrom),Some(validDateTo),None,Some("invalid"),None,None,None,None, BAD_REQUEST, InvalidIncludeLocksError),
        ("AA123456A",Some(validDocNumber), Some(validDateFrom),Some(validDateTo),None,None,Some("invalid"),None,None,None, BAD_REQUEST, InvalidCalculateAccruedInterestError),
        ("AA123456A",Some(validDocNumber), Some(validDateTo),Some(validDateFrom),None,None,None,Some("invalid"),None,None, BAD_REQUEST, InvalidRemovePaymentOnAccountError),
        ("AA123456A",Some(validDocNumber), Some(validDateFrom),Some(validDateTo),None,None,None,None,Some("invalid"),None, BAD_REQUEST, InvalidCustomerPaymentInformationError),
        ("AA123456A",Some(validDocNumber), Some(validDateFrom),Some(validDateTo),None,None,None,None,None,Some("invalid"), BAD_REQUEST, InvalidIncludeChargeEstimateError),
        ("AA123456A",Some(validDocNumber), Some(validDateTo),Some(validDateFrom),None,None,None,None,None,None, BAD_REQUEST, InvalidDateRangeError),
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

          val response: WSResponse = await(request(queryParams).get)
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_IDNUMBER", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_REGIME_TYPE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_DOC_NUMBER", BAD_REQUEST, InvalidDocNumberError),
        (BAD_REQUEST, "INVALID_DATE_FROM", BAD_REQUEST, InvalidDateFromError),
        (BAD_REQUEST, "INVALID_DATE_TO", BAD_REQUEST, InvalidDateToError),
        (BAD_REQUEST, "INVALID_DATE_RANGE", BAD_REQUEST, InvalidDateRangeError),
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
