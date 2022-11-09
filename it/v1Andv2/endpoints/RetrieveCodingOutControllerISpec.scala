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
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.fixtures.RetrieveCodingOutFixture._
import v1.stubs.DownstreamStub

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneOffset}

class RetrieveCodingOutControllerISpec extends IntegrationBaseSpec {
  val versions: Seq[String] = Seq("1.0", "2.0")

  private trait Test {

    val nino: String         = "AA123456A"
    lazy val taxYear: String = "2021-22"

    def desParamSource: String = "HMRC-HELD"

    def mtdParamSource: String = "hmrcHeld"

    def desBodySource: String = "HMRC HELD"

    def mtdBodySource: String = "hmrcHeld"

    val desResponse: JsValue = Json.parse(
      s"""
         |{
         |   "taxCodeComponents": {
         |       "selfAssessmentUnderpayment": [
         |           {
         |               "amount": 0,
         |               "relatedTaxYear": "$taxYear",
         |               "submittedOn": "2021-08-24T14:15:22Z",
         |               "source": "$desBodySource",
         |               "componentIdentifier": "12345678910"
         |           }
         |       ],
         |       "payeUnderpayment": [
         |           {
         |               "amount": 0,
         |               "relatedTaxYear": "$taxYear",
         |               "submittedOn": "2021-08-24T14:15:22Z",
         |               "source": "$desBodySource",
         |               "componentIdentifier": "12345678910"
         |           }
         |       ],
         |       "debt": [
         |           {
         |               "amount": 0,
         |               "relatedTaxYear": "$taxYear",
         |               "submittedOn": "2021-08-24T14:15:22Z",
         |               "source": "$desBodySource",
         |               "componentIdentifier": "12345678910"
         |           }
         |       ],
         |       "inYearAdjustment": {
         |           "amount": 0,
         |           "relatedTaxYear": "$taxYear",
         |           "submittedOn": "2021-08-24T14:15:22Z",
         |           "source": "$desBodySource",
         |           "componentIdentifier": "12345678910"
         |       }
         |   },
         |   "unmatchedCustomerSubmissions": {
         |       "selfAssessmentUnderpayment": [
         |           {
         |               "amount": 0,
         |               "submittedOn": "2021-08-24T14:15:22Z",
         |               "componentIdentifier": "12345678910"
         |           }
         |       ],
         |       "payeUnderpayment": [
         |           {
         |               "amount": 0,
         |               "submittedOn": "2021-08-24T14:15:22Z",
         |               "componentIdentifier": "12345678910"
         |           }
         |       ],
         |       "debt": [
         |           {
         |               "amount": 0,
         |               "submittedOn": "2021-08-24T14:15:22Z",
         |               "componentIdentifier": "12345678910"
         |           }
         |       ],
         |       "inYearAdjustment": {
         |           "amount": 0,
         |           "submittedOn": "2021-08-24T14:15:22Z",
         |           "componentIdentifier": "12345678910"
         |       }
         |   }
         |}
       """.stripMargin
    )

    val desResponseNoId: JsValue = Json.parse(
      s"""
         |{
         |   "taxCodeComponents": {
         |       "selfAssessmentUnderpayment": [
         |           {
         |               "amount": 0,
         |               "relatedTaxYear": "$taxYear",
         |               "submittedOn": "2021-08-24T14:15:22Z",
         |               "source": "$desBodySource"
         |           }
         |       ],
         |       "payeUnderpayment": [
         |           {
         |               "amount": 0,
         |               "relatedTaxYear": "$taxYear",
         |               "submittedOn": "2021-08-24T14:15:22Z",
         |               "source": "$desBodySource"
         |           }
         |       ],
         |       "debt": [
         |           {
         |               "amount": 0,
         |               "relatedTaxYear": "$taxYear",
         |               "submittedOn": "2021-08-24T14:15:22Z",
         |               "source": "$desBodySource"
         |           }
         |       ],
         |       "inYearAdjustment": {
         |           "amount": 0,
         |           "relatedTaxYear": "$taxYear",
         |           "submittedOn": "2021-08-24T14:15:22Z",
         |           "source": "$desBodySource"
         |       }
         |   },
         |   "unmatchedCustomerSubmissions": {
         |       "selfAssessmentUnderpayment": [
         |           {
         |               "amount": 0,
         |               "submittedOn": "2021-08-24T14:15:22Z"
         |           }
         |       ],
         |       "payeUnderpayment": [
         |           {
         |               "amount": 0,
         |               "submittedOn": "2021-08-24T14:15:22Z"
         |           }
         |       ],
         |       "debt": [
         |           {
         |               "amount": 0,
         |               "submittedOn": "2021-08-24T14:15:22Z"
         |           }
         |       ],
         |       "inYearAdjustment": {
         |           "amount": 0,
         |           "submittedOn": "2021-08-24T14:15:22Z"
         |       }
         |   }
         |}
       """.stripMargin
    )

    val mtdResponse: JsValue     = mtdResponseWithHateoas(nino, taxYear, mtdBodySource)
    val mtdResponseNoId: JsValue = mtdResponseWithHateoasNoId(nino, taxYear, mtdBodySource)

    def uri: String = s"/$nino/$taxYear/collection/tax-code"

    def desUri: String = s"/income-tax/accounts/self-assessment/collection/tax-code/$nino/$taxYear"

    def setupStubs(): StubMapping

    def request(version: String, source: Option[String]): WSRequest = {
      def queryParams: Seq[(String, String)] = Seq("source" -> source).collect { case (k, Some(v)) =>
        (k, v)
      }

      setupStubs()
      buildRequest(uri)
        .addQueryStringParameters(queryParams: _*)
        .withHttpHeaders(
          (ACCEPT, s"application/vnd.hmrc.$version+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

  }

  "Calling the 'retrieve coding out' endpoint" should {
    "return a 200 status code for default" when {
      def makeAValidRequest(version: String): Unit = {
        s"any valid request is made to retrieve latest view when no query parameter is specified for version $version" in new Test {

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.GET, desUri, OK, desResponse)
          }

          val response: WSResponse = await(request(version, None).get())
          response.status shouldBe OK
          response.json shouldBe mtdResponse
          response.header("X-CorrelationId").nonEmpty shouldBe true
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }
      versions.foreach(arg => makeAValidRequest(arg))
    }

    "return a 200 status code for latest" when {
      def latest(version: String): Unit = {
        s"any valid request is made to retrieve the latest view for version $version" in new Test {

          override def desParamSource: String = "LATEST"

          override def mtdParamSource: String = "latest"

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.GET, desUri, Map("view" -> desParamSource), OK, desResponse)
          }

          val response: WSResponse = await(request(version, Some(mtdParamSource)).get())
          response.status shouldBe OK
          response.json shouldBe mtdResponse
          response.header("X-CorrelationId").nonEmpty shouldBe true
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      versions.foreach(arg => latest(arg))
    }

    "return a 200 status code for hmrcHeld data" should {
      def hmrcHeld(version: String): Unit = {
        s"any valid request is made to retrieve hmrcHeld for version $version" in new Test {

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.GET, desUri, Map("view" -> desParamSource), OK, desResponse)
          }

          val response: WSResponse = await(request(version, Some(mtdParamSource)).get())
          response.status shouldBe OK
          response.json shouldBe mtdResponse
          response.header("X-CorrelationId").nonEmpty shouldBe true
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }
      versions.foreach(arg => hmrcHeld(arg))
    }

    "return a 200 status code for user submitted data" should {
      def userSubmitted(version: String): Unit = {
        s"any valid request is made to retrieve user submitted data for version $version" in new Test {

          override def desParamSource: String = "CUSTOMER"

          override def mtdParamSource: String = "user"

          override def desBodySource: String = "CUSTOMER"

          override def mtdBodySource: String = "user"

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.GET, desUri, Map("view" -> desParamSource), OK, desResponse)
          }

          val response: WSResponse = await(request(version, Some(mtdParamSource)).get())
          response.status shouldBe OK
          response.json shouldBe mtdResponse
          response.header("X-CorrelationId").nonEmpty shouldBe true
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }
      versions.foreach(arg => userSubmitted(arg))
    }

    "return a 200 status code for an taxYear that has ended" should {
      def endedTaxYear(version: String): Unit = {
        s"any valid request is made that returns a body with the id present while the taxYear has ended for version $version" in new Test {

          override lazy val taxYear: String = "2020-21"

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.GET, desUri, OK, desResponse)
          }

          val response: WSResponse = await(request(version, None).get())
          response.status shouldBe OK
          response.json shouldBe mtdResponse
          response.header("X-CorrelationId").nonEmpty shouldBe true
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }
      versions.foreach(arg => endedTaxYear(arg))
    }

    "return a 200 status code for a taxYear that has not ended" should {
      def notEndedTaxYear(version: String): Unit = {
        s"any valid request is made that returns a body with the id present while the taxYear hasn't ended for version $version" in new Test {

          def getCurrentTaxYear: String = {
            val currentDate = LocalDate.now(ZoneOffset.UTC)

            val taxYearStartDate: LocalDate = LocalDate.parse(
              currentDate.getYear + "-04-06",
              DateTimeFormatter.ofPattern("yyyy-MM-dd")
            )

            def fromDesIntToString(taxYear: Int): String =
              (taxYear - 1) + "-" + taxYear.toString.drop(2)

            if (currentDate.isBefore(taxYearStartDate)) fromDesIntToString(currentDate.getYear) else fromDesIntToString(currentDate.getYear + 1)
          }
          override lazy val taxYear: String = getCurrentTaxYear

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.GET, desUri, OK, desResponse)
          }

          val response: WSResponse = await(request(version, None).get())
          response.status shouldBe OK
          response.json shouldBe mtdResponse
          response.header("X-CorrelationId").nonEmpty shouldBe true
          response.header("Content-Type") shouldBe Some("application/json")

        }

      }
      versions.foreach(arg => notEndedTaxYear(arg))
    }

    "return a 200 status code for a taxYear that has not ended and no id" should {
      def notEndedTaxYearWithNoId(version: String): Unit = {
        s"any valid request is made that returns a body without the id present while the taxYear hasn't ended for version $version" in new Test {

          def getCurrentTaxYear: String = {
            val currentDate = LocalDate.now(ZoneOffset.UTC)

            val taxYearStartDate: LocalDate = LocalDate.parse(
              currentDate.getYear + "-04-06",
              DateTimeFormatter.ofPattern("yyyy-MM-dd")
            )

            def fromDesIntToString(taxYear: Int): String =
              (taxYear - 1) + "-" + taxYear.toString.drop(2)

            if (currentDate.isBefore(taxYearStartDate)) fromDesIntToString(currentDate.getYear) else fromDesIntToString(currentDate.getYear + 1)
          }
          override lazy val taxYear: String = getCurrentTaxYear

          override def setupStubs(): StubMapping = {
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DownstreamStub.onSuccess(DownstreamStub.GET, desUri, OK, desResponseNoId)
          }

          val response: WSResponse = await(request(version, None).get())
          response.status shouldBe OK
          response.json shouldBe mtdResponseNoId
          response.header("X-CorrelationId").nonEmpty shouldBe true
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }
      versions.foreach(arg => notEndedTaxYearWithNoId(arg))
    }

    "return error according to spec" when {

      "validation error" when {

        def validationErrorTest(requestNino: String,
                                requestTaxYear: String,
                                requestSource: String,
                                expectedStatus: Int,
                                expectedBody: MtdError,
                                version: String): Unit = {
          s"validation fails with ${expectedBody.code} error " in new Test {

            override val nino: String         = requestNino
            override lazy val taxYear: String = requestTaxYear

            override def setupStubs(): StubMapping = {
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
            }

            val response: WSResponse = await(request(version, Some(requestSource)).get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        val input: Seq[(String, String, String, Int, MtdError)] = Seq(
          ("AA1123A", "2021-22", "latest", BAD_REQUEST, NinoFormatError),
          ("AA123456A", "20199", "latest", BAD_REQUEST, TaxYearFormatError),
          ("AA123456A", "2021-22", "source", BAD_REQUEST, SourceFormatError),
          ("AA123456A", "2018-19", "latest", BAD_REQUEST, RuleTaxYearNotSupportedError),
          ("AA123456A", "2021-23", "latest", BAD_REQUEST, RuleTaxYearRangeInvalidError)
        )

        versions.foreach(version => {
          s"for version $version " when {
            val parameters = input.map(c => (c._1, c._2, c._3, c._4, c._5, version))
            parameters.foreach(args => (validationErrorTest _).tupled(args))
          }
        })

      }

      "des service error" when {
        def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError, version: String): Unit = {
          s"des returns an $desCode error and status $desStatus for version $version" in new Test {

            override def setupStubs(): StubMapping = {
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onError(DownstreamStub.GET, desUri, desStatus, errorBody(desCode))
            }

            val response: WSResponse = await(request(version, None).get())
            response.status shouldBe expectedStatus
            response.json shouldBe Json.toJson(expectedBody)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }

        def errorBody(code: String): String =
          s"""
             |{
             |  "code": "$code",
             |  "reason": "des message"
             |}
            """.stripMargin

        val input: Seq[(Int, String, Int, MtdError)] = Seq(
          (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
          (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
          (BAD_REQUEST, "INVALID_VIEW", BAD_REQUEST, SourceFormatError),
          (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, DownstreamError),
          (NOT_FOUND, "NO_DATA_FOUND", NOT_FOUND, CodingOutNotFoundError),
          (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError),
          (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
          (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError)
        )
        versions.foreach(version => {
          s"for version $version " when {
            val parameters = input.map(c => (c._1, c._2, c._3, c._4, version))
            parameters.foreach(args => (serviceErrorTest _).tupled(args))
          }
        })
      }

      "service error" when {
        def requestForBodyWithoutIdForEndedTaxYear(version: String): Unit = {
          s"any valid request is made that returns a body without the id present while the taxYear has ended " in new Test {

            override lazy val taxYear: String = "2020-21"

            override def setupStubs(): StubMapping = {
              AuthStub.authorised()
              MtdIdLookupStub.ninoFound(nino)
              DownstreamStub.onSuccess(DownstreamStub.GET, desUri, OK, desResponseNoId)
            }

            val response: WSResponse = await(request(version, None).get())
            response.status shouldBe INTERNAL_SERVER_ERROR
            response.json shouldBe Json.toJson(DownstreamError)
            response.header("Content-Type") shouldBe Some("application/json")
          }
        }
        versions.foreach(version => {
          s"for version $version" when {
            requestForBodyWithoutIdForEndedTaxYear(version)
          }
        })

      }
    }
  }

}
