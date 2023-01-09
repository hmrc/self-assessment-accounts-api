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

package v1Andv2.endpoints

import api.models.errors._
import api.stubs.{AuditStub, AuthStub, MtdIdLookupStub}
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status
import play.api.http.Status.{BAD_REQUEST, INTERNAL_SERVER_ERROR, UNPROCESSABLE_ENTITY}
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.stubs.DownstreamStub

import scala.collection.Seq

class DeleteCodingOutControllerISpec extends IntegrationBaseSpec {

  val versions: Seq[String] = Seq("1.0", "2.0")

  "Calling the delete endpoint" should {
    "return a 204 status code for a non-TYS request" when {
      def makeAValidRequest(version: String): Unit = {
        s"for version $version" in new NonTysTest {

          override def setupStubs(): Unit = {
            DownstreamStub.onSuccess(DownstreamStub.DELETE, downstreamUri, Status.NO_CONTENT, JsObject.empty)
          }

          val response: WSResponse = await(request(version).delete())
          response.status shouldBe Status.NO_CONTENT
          response.header("X-CorrelationId").nonEmpty shouldBe true
        }
      }

      versions.foreach(arg => makeAValidRequest(arg))
    }

    "return a 204 status code for a TYS request" when {
      def makeAValidRequest(version: String): Unit = {
        s"for version $version" in new TysIfsTest {

          override def setupStubs(): Unit = {
            DownstreamStub.onSuccess(DownstreamStub.DELETE, downstreamUri, Status.NO_CONTENT, JsObject.empty)
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
        s"validation fails with ${expectedBody.code} error " in new NonTysTest {

          override val nino: String = requestNino
          override val taxYear: String = requestId

          override def setupStubs(): Unit = {
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

    "downstream service error" when {
      def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError, version: String): Unit = {
        s"downstream returns an $downstreamCode error and status $downstreamStatus " in new NonTysTest {

          override def setupStubs(): Unit = {
            DownstreamStub.onError(DownstreamStub.DELETE, downstreamUri, downstreamStatus, errorBody(downstreamCode))
          }

          val response: WSResponse = await(request(version).delete())
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val errors = Seq(
        (Status.BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", Status.BAD_REQUEST, NinoFormatError),
        (Status.BAD_REQUEST, "INVALID_TAX_YEAR", Status.BAD_REQUEST, TaxYearFormatError),
        (Status.BAD_REQUEST, "INVALID_CORRELATIONID", Status.INTERNAL_SERVER_ERROR, InternalError),
        (Status.NOT_FOUND, "NO_DATA_FOUND", Status.NOT_FOUND, CodingOutNotFoundError),
        (Status.INTERNAL_SERVER_ERROR, "SERVER_ERROR", Status.INTERNAL_SERVER_ERROR, InternalError),
        (Status.SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", Status.INTERNAL_SERVER_ERROR, InternalError)
      )

      val extraTysErrors = Seq(
        (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
      )

      versions.foreach(version => {
        s"for version ${version}" when {
          val parameters = (errors ++ extraTysErrors).map(c => (c._1, c._2, c._3, c._4, version))
          parameters.foreach(args => (serviceErrorTest _).tupled(args))
        }
      })
    }
  }

  private trait Test {
    val nino = "AA123456A"

    def taxYear: String

    def downstreamUri: String

    def uri: String = s"/$nino/$taxYear/collection/tax-code"

    def setupStubs(): Unit

    def request(version: String): WSRequest = {
      setupStubs()
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, s"application/vnd.hmrc.$version+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String =
      s"""
         |      {
         |        "code": "$code",
         |        "reason": "downstream message"
         |      }
    """.stripMargin

  }

  private trait NonTysTest extends Test {
    def taxYear: String = "2019-20"

    def downstreamUri: String = s"/income-tax/accounts/self-assessment/collection/tax-code/$nino/2019-20"
  }

  private trait TysIfsTest extends Test {
    def taxYear: String = "2023-24"

    def downstreamUri: String = s"/income-tax/23-24/accounts/self-assessment/collection/tax-code/$nino"
  }
}
