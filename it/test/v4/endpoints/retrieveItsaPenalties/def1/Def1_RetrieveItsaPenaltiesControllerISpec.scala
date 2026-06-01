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

package v4.endpoints.retrieveItsaPenalties.def1

import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.*
import shared.models.errors.*
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import shared.support.IntegrationBaseSpec
import v4.retrieveItsaPenalties.def1.model.response.RetrieveItsaPenaltiesFixture.*

class Def1_RetrieveItsaPenaltiesControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    protected val nino = "AA123456A"

    def hipQueryParams: Map[String, String] =
      Map(
        "taxRegime" -> "ITSA",
        "idType"    -> "NINO",
        "idNumber"  -> nino
      )

    def downstreamUrl: String = s"/etmp/RESTAdapter/cross-regime/taxpayer/penalties"

    def setupStubs(): Unit = ()

    def request: WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(s"/$nino/penalties")
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.4.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String =
      s"""
         |{
         |  "errors": {
         |    "processingDate": "2025-07-15T09:45:17Z",
         |    "code": "$code",
         |    "text": "downstream message"
         |  }
         |}
      """.stripMargin

  }

  "Calling the 'retrieve itsa penalties' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {
        override def setupStubs(): Unit = DownstreamStub.onSuccess(
          method = DownstreamStub.GET,
          uri = downstreamUrl,
          hipQueryParams,
          status = OK,
          body = downstreamResponse
        )

        val response: WSResponse = await(request.get())
        response.status shouldBe OK
        response.json shouldBe mtdJsonResponse
        response.header("Content-Type") shouldBe Some("application/json")
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

      val input = ("AA1123A", BAD_REQUEST, NinoFormatError)
      validationErrorTest.tupled(input)
    }

    "downstream service error" when {
      def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {
          override def setupStubs(): Unit = DownstreamStub.onError(
            method = DownstreamStub.GET,
            uri = downstreamUrl,
            errorStatus = downstreamStatus,
            errorBody = errorBody(downstreamCode)
          )

          val response: WSResponse = await(request.get())
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = List(
        (UNPROCESSABLE_ENTITY, "002", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "003", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "015", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "135", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "UNMATCHED_STUB_ERROR", BAD_REQUEST, RuleIncorrectGovTestScenarioError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError)
      )
      input.foreach(args => serviceErrorTest.tupled(args))
    }
  }

}
