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

package v4.endpoints.retrieveChargeHistoryByChargeReference.def1

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import common.errors.ChargeReferenceFormatError
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import shared.models.errors.*
import shared.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import shared.support.IntegrationBaseSpec
import v4.retrieveChargeHistoryByChargeReference.def1.model.response.RetrieveChargeHistoryFixture.{downstreamResponseMultiple, mtdMultipleResponse}

class Def1_RetrieveChargeHistoryByChargeReferenceHipISpec extends IntegrationBaseSpec {

  private trait Test {

    protected val chargeReference = "XD000024425799"
    protected val nino            = "AA123456A"

    def downstreamUrl: String = s"/etmp/RESTAdapter/itsa/taxpayer/GetChargeHistory"

    def setupStubs(): StubMapping

    def request: WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.4.0+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def uri: String = s"/$nino/charges/chargeReference/$chargeReference"

    def errorBody(code: String): String =
      s"""
         |{
         |   "code": "$code",
         |   "text": "downstream message"
         |}
           """.stripMargin

  }

  "Calling the 'retrieve a charge history' endpoint" should {
    "return a 200 status code" when {
      "any valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUrl, OK, downstreamResponseMultiple)
        }

        val response: WSResponse = await(request.get())
        response.status shouldBe OK
        response.json shouldBe mtdMultipleResponse
        response.header("Content-Type") shouldBe Some("application/json")
      }

    }

    "return error according to spec" when {

      def validationErrorTest(requestNino: String, requestChargeReference: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {

          override val nino: String            = requestNino
          override val chargeReference: String = requestChargeReference

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

      val input = Seq(
        ("AA1123A", "XD000024425799", BAD_REQUEST, NinoFormatError),
        ("AA123456A", "veryBadChargeRef", BAD_REQUEST, ChargeReferenceFormatError)
      )
      input.foreach(args => validationErrorTest.tupled(args))
    }

    "downstream service error" when {
      def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {

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
        (UNPROCESSABLE_ENTITY, "014", NOT_FOUND, NotFoundError),
        (UNPROCESSABLE_ENTITY, "002", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "003", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "005", NOT_FOUND, NotFoundError),
        (UNPROCESSABLE_ENTITY, "015", INTERNAL_SERVER_ERROR, InternalError)
      )
      input.foreach(args => serviceErrorTest.tupled(args))
    }
  }

}
