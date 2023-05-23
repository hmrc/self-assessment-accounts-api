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

package v1.endpoints

import api.models.errors._
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.fixtures.ListChargesFixture._
import api.stubs.{AuditStub, AuthStub, MtdIdLookupStub}
import v1.stubs.DownstreamStub

class ListChargesControllerISpec extends IntegrationBaseSpec {

  private trait Test {
    val nino                 = "AA123456A"
    val from: Option[String] = Some("2018-10-01")
    val to: Option[String]   = Some("2019-10-01")

    def uri: String = s"/$nino/charges"

    def desUrl: String = s"/enterprise/02.00.00/financial-data/NINO/$nino/ITSA"

    def setupStubs(): StubMapping

    def request: WSRequest = {

      val queryParams = Seq("from" -> from, "to" -> to)
        .collect { case (k, Some(v)) =>
          (k, v)
        }
      setupStubs()
      buildRequest(uri)
        .addQueryStringParameters(queryParams: _*)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.1.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

  }

  "Calling the list charges endpoint" should {
    "return a valid response with status OK" when {
      "valid request is made" in new Test {

        val desQueryParams: Map[String, String] = Map(
          "dateFrom"                   -> from.get,
          "dateTo"                     -> to.get,
          "onlyOpenItems"              -> "false",
          "includeLocks"               -> "true",
          "calculateAccruedInterest"   -> "true",
          "removePOA"                  -> "true",
          "customerPaymentInformation" -> "true",
          "includeStatistical"         -> "false"
        )

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, desUrl, desQueryParams, OK, fullDesListChargesMultipleResponse)
        }

        val response: WSResponse = await(request.get())

        response.status shouldBe OK
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe ListChargesMtdResponseWithHateoas
      }
    }

    "return error according to spec" when {

      def validationErrorTest(requestNino: String,
                              fromDate: Option[String],
                              toDate: Option[String],
                              expectedStatus: Int,
                              expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {

          override val nino: String         = requestNino
          override val from: Option[String] = fromDate
          override val to: Option[String]   = toDate

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
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
        ("AA1123A", Some("2018-10-01"), Some("2019-10-01"), BAD_REQUEST, NinoFormatError),
        ("AA123456A", Some("2018-100-01"), Some("2019-10-01"), BAD_REQUEST, V1_FromDateFormatError),
        ("AA123456A", Some("2018-10-01"), Some("2019-100-01"), BAD_REQUEST, V1_ToDateFormatError),
        ("AA123456A", None, Some("2019-10-01"), BAD_REQUEST, V1_MissingFromDateError),
        ("AA123456A", Some("2018-10-01"), None, BAD_REQUEST, V1_MissingToDateError),
        ("AA123456A", Some("2018-10-01"), Some("2018-06-01"), BAD_REQUEST, V1_RangeToDateBeforeFromDateError),
        ("AA123456A", Some("2018-10-01"), Some("2021-10-01"), BAD_REQUEST, RuleDateRangeInvalidError),
        ("AA123456A", Some("2018-03-01"), Some("2019-10-01"), BAD_REQUEST, RuleFromDateNotSupportedError)
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
            DownstreamStub.onError(DownstreamStub.GET, desUrl, desQueryParams, desStatus, errorBody(desCode))
          }

          val response: WSResponse = await(request.get())
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        (BAD_REQUEST, "INVALID_IDNUMBER", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_REGIME_TYPE", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_DATE_FROM", BAD_REQUEST, V1_FromDateFormatError),
        (BAD_REQUEST, "INVALID_DATE_TO", BAD_REQUEST, V1_ToDateFormatError),
        (FORBIDDEN, "REQUEST_NOT_PROCESSED", INTERNAL_SERVER_ERROR, InternalError),
        (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
        (BAD_REQUEST, "INVALID_DOC_NUMBER", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_ONLY_OPEN_ITEMS", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_INCLUDE_LOCKS", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_CALCULATE_ACCRUED_INTEREST", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_CUSTOMER_PAYMENT_INFORMATION", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_DATE_RANGE", BAD_REQUEST, RuleDateRangeInvalidError),
        (BAD_REQUEST, "INVALID_REQUEST", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_REMOVE_PAYMENT_ON_ACCOUNT", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_INCLUDE_STATISTICAL", INTERNAL_SERVER_ERROR, InternalError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
      )
      input.foreach(args => (serviceErrorTest _).tupled(args))
    }
  }

}
