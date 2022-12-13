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

package v1Andv2.endpoints

import api.models.errors._
import api.stubs.{AuthStub, MtdIdLookupStub}
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.stubs.DownstreamStub

class DeleteCodingOutControllerISpec extends IntegrationBaseSpec {

  val versions = Seq("1.0", "2.0")

  private trait Test {

    val nino    = "AA123456A"
    val taxYear = "2021-22"

    def uri: String    = s"/$nino/$taxYear/collection/tax-code"
    def desUri: String = s"/income-tax/accounts/self-assessment/collection/tax-code/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(version: String): WSRequest = {
      setupStubs()
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, s"application/vnd.hmrc.$version+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def errorBody(code: String): String =
      s"""
         |      {
         |        "code": "$code",
         |        "reason": "des message"
         |      }
    """.stripMargin

  }

  "Calling the delete endpoint" should {

    "return a 204 status code" when {

      def makeAValidRequest(version: String): Unit = {
        s"for version $version" in new Test {

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.DELETE, desUri, Status.NO_CONTENT, JsObject.empty)
          }

          val response: WSResponse = await(request(version).delete())
          response.status shouldBe Status.NO_CONTENT
          response.header("X-CorrelationId").nonEmpty shouldBe true

        }
      }

      versions.foreach(arg => makeAValidRequest(arg))

    }

    "return error according to spec" when {

      def validationErrorTest(requestNino: String, requestId: String, expectedStatus: Int, expectedBody: MtdError, version: String): Unit = {
        s"validation fails with ${expectedBody.code} error " in new Test {

          override val nino: String    = requestNino
          override val taxYear: String = requestId

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request(version).delete())
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        ("AA123456", "2021-22", Status.BAD_REQUEST, NinoFormatError),
        ("AA123456A", "203100", Status.BAD_REQUEST, TaxYearFormatError),
        ("AA123456A", "2018-19", Status.BAD_REQUEST, RuleTaxYearNotSupportedError),
        ("AA123456A", "2018-20", Status.BAD_REQUEST, RuleTaxYearRangeInvalidError)
      )
      versions.foreach(version => {
        s"for version ${version}" when {
          val parameters = input.map(c => (c._1, c._2, c._3, c._4, version))
          parameters.foreach(args => (validationErrorTest _).tupled(args))
        }
      })
    }

    "des service error" when {

      def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError, version: String): Unit = {
        s"des returns an $desCode error and status $desStatus " in new Test {

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onError(DownstreamStub.DELETE, desUri, desStatus, errorBody(desCode))
          }

          val response: WSResponse = await(request(version).delete())
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }
      val input = Seq(
        (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError),
        (Status.BAD_REQUEST, "INVALID_TAX_YEAR", Status.BAD_REQUEST, TaxYearFormatError),
        (Status.BAD_REQUEST, "INVALID_CORRELATIONID", Status.INTERNAL_SERVER_ERROR, InternalError),
        (Status.NOT_FOUND, "NO_DATA_FOUND", Status.NOT_FOUND, CodingOutNotFoundError),
        (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, InternalError),
        (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, InternalError)
      )

      versions.foreach(version => {
        s"for version ${version}" when {
          val parameters = input.map(c => (c._1, c._2, c._3, c._4, version))
          parameters.foreach(args => (serviceErrorTest _).tupled(args))
        }
      })
    }
  }

}
