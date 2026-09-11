/*
 * Copyright 2026 HM Revenue & Customs
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

package v4.endpoints.listPaymentsAndAllocationDetails

import api.models.errors.*
import api.services.*
import api.support.IntegrationBaseSpec
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*
import v4.listPaymentsAndAllocationDetails.def1.model.response.ResponseFixtures.*

class Def1_ListPaymentsAndAllocationDetailsControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino: String           = "AA123456A"
    private val dateFrom       = "2025-08-13"
    private val dateTo         = "2025-09-13"
    private val paymentLot     = "081203010024"
    private val paymentLotItem = "000001"

    val downstreamQueryParams: Map[String, String] =
      Map(
        "dateFrom"       -> dateFrom,
        "dateTo"         -> dateTo,
        "paymentLot"     -> paymentLot,
        "paymentLotItem" -> paymentLotItem
      )

    val downstreamUrl: String = s"/etmp/RESTAdapter/payment-allocation/NINO/$nino/ITSA"

    def setupStubs(): Unit = ()

    def request: WSRequest = {
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(s"/$nino/payments-and-allocations")
        .addQueryStringParameters("fromDate" -> dateFrom, "toDate" -> dateTo, "paymentLot" -> paymentLot, "paymentLotItem" -> paymentLotItem)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.4.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String =
      s"""
         |{
         |  "errors": {
         |    "processingDate": "2026-07-15T09:45:17Z",
         |    "code": "$code",
         |    "text": "downstream message"
         |  }
         |}
      """.stripMargin

  }

  "Calling the 'List Self Assessment Payments & Allocation Details' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {
        override def setupStubs(): Unit = DownstreamStub.onSuccess(
          method = DownstreamStub.GET,
          uri = downstreamUrl,
          queryParams = downstreamQueryParams,
          status = OK,
          body = responseHipDownstreamJson
        )

        val response: WSResponse = await(request.get())
        response.status shouldBe OK
        response.json shouldBe mtdResponseJson
        response.header("Content-Type") shouldBe Some("application/json")
        response.header("X-CorrelationId").nonEmpty shouldBe true
      }

    }

    "return error according to spec" when {

      def validationErrorTest(requestNino: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {

          override val nino: String = requestNino

          val response: WSResponse = await(request.get())
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      validationErrorTest("AA1123A", BAD_REQUEST, NinoFormatError)

      def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"downstream returns a code $downstreamCode error and status $downstreamStatus" in new Test {
          override def setupStubs(): Unit = DownstreamStub.onError(
            method = DownstreamStub.GET,
            uri = downstreamUrl,
            queryParams = downstreamQueryParams,
            errorStatus = downstreamStatus,
            errorBody = errorBody(downstreamCode)
          )

          val response: WSResponse = await(request.get())
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
          response.header("X-CorrelationId").nonEmpty shouldBe true
        }
      }

      val input: Seq[(Int, String, Int, MtdError)] = List(
        (UNPROCESSABLE_ENTITY, "003", BAD_REQUEST, BadRequestError),
        (UNPROCESSABLE_ENTITY, "005", NOT_FOUND, NotFoundError),
        (UNPROCESSABLE_ENTITY, "015", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "600", BAD_REQUEST, BadRequestError)
      )

      input.foreach(serviceErrorTest.tupled)
    }
  }

}
