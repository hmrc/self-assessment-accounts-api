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
import v1.fixtures.ListChargesFixture._
import v1.models.errors._
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class ListChargesControllerISpec extends IntegrationBaseSpec {

  private trait Test {
    val nino = "AA123456A"
    val correlationId = "X-123"
    val from: Option[String] = Some("2018-10-01")
    val to : Option[String]  = Some("2019-10-01")
    def uri: String = s"/$nino/charges"

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
  }

  "Calling the list charges endpoint" should {

    "return a valid response with status OK" when {

      "valid request is made" in new Test {

        val desQueryParams: Map[String, String] = Map("dateFrom" -> from.get, "dateTo" -> to.get, "type" -> "charge")

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, desQueryParams, OK, fullDesListChargesMultipleResponse)
        }

        val response: WSResponse = await(request.get)

        response.status shouldBe OK
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe mtdResponse(nino = "AA123456A")
      }
    }

    "return a 404 NO_CHARGES_FOUND error" when {
      "a success response with no charges is returned" in new Test {

        val desQueryParams: Map[String, String] = Map("dateFrom" -> from.get, "dateTo" -> to.get)

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, desQueryParams, OK, minimalDesListChargesResponse)
        }

        val response: WSResponse = await(request.get)

        response.status shouldBe NOT_FOUND
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe Json.toJson(NoChargesFoundError)
      }
    }

    "return error according to spec" when {

      def validationErrorTest(requestNino: String, fromDate: Option[String],
                              toDate: Option[String], expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {

          override val nino: String = requestNino
          override val from: Option[String] = fromDate
          override val to: Option[String] = toDate

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
        ("AA1123A", Some("2018-10-01"), Some("2019-10-01"), BAD_REQUEST, NinoFormatError),
        ("AA123456A", Some("2018-100-01"), Some("2019-10-01"), BAD_REQUEST, FromDateFormatError),
        ("AA123456A", Some("2018-10-01"), Some("2019-100-01"), BAD_REQUEST, ToDateFormatError),
        ("AA123456A", None, Some("2019-10-01"), BAD_REQUEST, MissingFromDateError),
        ("AA123456A", Some("2018-10-01"), None, BAD_REQUEST, MissingToDateError),
        ("AA123456A", Some("2018-10-01"), Some("2018-06-01"), BAD_REQUEST, RangeToDateBeforeFromDateError),
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
            DesStub.onError(DesStub.GET, desUrl, desQueryParams, desStatus, errorBody(desCode))
          }

          val response: WSResponse = await(request.get)
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        (BAD_REQUEST, "INVALID_IDVALUE", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_REGIME_TYPE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_DATE_FROM", BAD_REQUEST, FromDateFormatError),
        (BAD_REQUEST, "INVALID_DATE_TO", BAD_REQUEST, ToDateFormatError),
        (BAD_REQUEST, "INVALID_DATE_RANGE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_PAYMENT_LOT", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_PAYMENT_LOT_ITEM", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_CLEARING_DOC", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "REQUEST_NOT_PROCESSED", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "PARTIALLY_MIGRATED", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "NO_DATA_FOUND", NOT_FOUND, NotFoundError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError),
        (UNPROCESSABLE_ENTITY, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, DownstreamError)
      )

      input.foreach(args => (serviceErrorTest _).tupled(args))
    }
  }
}