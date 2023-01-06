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
import play.api.http.Status._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v1.stubs.DownstreamStub

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneOffset}

class CreateOrAmendCodingOutControllerISpec extends IntegrationBaseSpec {

  val versions = Seq("1.0", "2.0")

  private trait Test {

    val nino: String = "AA123456A"
    def taxYear: String

    val requestBodyJson: JsValue = Json.parse(
      """
        |{
        |  "taxCodeComponents": {
        |    "payeUnderpayment": [
        |      {
        |        "amount": 123.45,
        |        "id": 1
        |      }
        |    ],
        |    "selfAssessmentUnderpayment": [
        |      {
        |        "amount": 123.45,
        |        "id": 2
        |      }
        |    ],
        |    "debt": [
        |      {
        |        "amount": 123.45,
        |        "id": 3
        |      }
        |    ],
        |    "inYearAdjustment": {
        |      "amount": 123.45,
        |      "id": 4
        |    }
        |  }
        |}
      """.stripMargin
    )

    val responseBody: JsValue = Json.parse(
      s"""
         |{
         |  "links": [
         |    {
         |      "href": "/accounts/self-assessment/$nino/$taxYear/collection/tax-code",
         |      "method": "PUT",
         |      "rel": "create-or-amend-coding-out-underpayments"
         |    },
         |    {
         |      "href": "/accounts/self-assessment/$nino/$taxYear/collection/tax-code",
         |      "method": "GET",
         |      "rel": "self"
         |    },
         |    {
         |      "href": "/accounts/self-assessment/$nino/$taxYear/collection/tax-code",
         |      "method": "DELETE",
         |      "rel": "delete-coding-out-underpayments"
         |    }
         |  ]
         |}
       """.stripMargin
    )

    def setupStubs(): Unit = ()

    def request(version: String): WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      setupStubs()
      buildRequest(s"/$nino/$taxYear/collection/tax-code")
        .withHttpHeaders(
          (ACCEPT, s"application/vnd.hmrc.$version+json"),
          (AUTHORIZATION, "Bearer 123")
        )
    }

    def errorBody(code: String): String =
      s"""
         |{
         |   "code": "$code",
         |   "reason": "A message from downstream"
         |}
        """.stripMargin

  }

  private trait NonTysTest extends Test {
    def taxYear: String = "2020-21"

    def downstreamUri: String = s"/income-tax/accounts/self-assessment/collection/tax-code/$nino/2020-21"
  }

  private trait TysTest extends Test {
    def taxYear: String = "2023-24"

    def downstreamUri: String = s"/income-tax/23-24/accounts/self-assessment/collection/tax-code/$nino"

    override def request(version : String): WSRequest =
      super.request(version).addHttpHeaders("suspend-temporal-validations" -> "true")
  }

  "Calling the 'create or amend coding out' endpoint" should {

    "return a 200 status code" when {

      def validRequest(version: String): Unit = {
        "any valid request is made" in new NonTysTest  {
          override def setupStubs(): Unit =
            DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, JsObject.empty)

          val response: WSResponse = await(request(version).put(requestBodyJson))
          response.status shouldBe OK
          response.json shouldBe responseBody
          response.header("X-CorrelationId").nonEmpty shouldBe true
        }

        "any valid request is made (TYS)" in new TysTest {
          override def setupStubs(): Unit =
            DownstreamStub.onSuccess(DownstreamStub.PUT, downstreamUri, NO_CONTENT, JsObject.empty)

          val response: WSResponse = await(request(version).put(requestBodyJson))
          response.status shouldBe OK
          response.json shouldBe responseBody
          response.header("X-CorrelationId").nonEmpty shouldBe true
        }
      }

      versions.foreach(version => {
        s"for version $version" when {
          validRequest(version)
        }
      })
    }

    "return an error according to the spec" when {

      "the nino validation error is because" when {
        def invalidNino(version: String): Unit = {
          "an invalid NINO format is provided" in new NonTysTest {
            override val nino: String = "INVALID_NINO"

            val response: WSResponse = await(request(version).put(requestBodyJson))
            response.status shouldBe BAD_REQUEST
            response.json shouldBe NinoFormatError.asJson
          }

        }
        versions.foreach(version => {
          s"for version $version" when {
            invalidNino(version)
          }
        })
      }

      "the taxYear validation error because" when {
        def invalidTaxYear(version: String): Unit = {
          "an invalid taxYear format is provided" in new NonTysTest {

            override val taxYear: String = "INVALID_TAXYEAR"

            val response: WSResponse = await(request(version).put(requestBodyJson))
            response.status shouldBe BAD_REQUEST
            response.json shouldBe TaxYearFormatError.asJson
          }
        }
        versions.foreach(version => {
          s"for version $version" when {
            invalidTaxYear(version)
          }
        })
      }

      "the unsupported taxYear validation error because" when {
        def unsupportedTaxYear(version: String): Unit = {
          "an unsupported taxYear is provided" in new NonTysTest {
            override val taxYear: String = "2016-17"

            val response: WSResponse = await(request(version).put(requestBodyJson))
            response.status shouldBe BAD_REQUEST
            response.json shouldBe RuleTaxYearNotSupportedError.asJson
          }
        }
        versions.foreach(version => {
          s"for version $version" when {
            unsupportedTaxYear(version)
          }
        })
      }

      "the incorrect range taxYear validation error because" when {
        def incorrectRange(version: String): Unit = {
          "a taxYear with an incorrect range is provided" in new NonTysTest {
            override val taxYear: String = "2020-22"

            val response: WSResponse = await(request(version).put(requestBodyJson))
            response.status shouldBe BAD_REQUEST
            response.json shouldBe RuleTaxYearRangeInvalidError.asJson
          }

        }

        versions.foreach(version => {
          s"for version $version" when {
            incorrectRange(version)
          }
        })
      }

      "the taxYear not ended validation error because" when {
        def taxYearNotEnded(version: String): Unit = {
          "a taxYear which has not ended is provided" in new NonTysTest {
            def getCurrentTaxYear: String = {
              val currentDate = LocalDate.now(ZoneOffset.UTC)

              val taxYearStartDate: LocalDate = LocalDate.parse(
                s"${currentDate.getYear}-04-06",
                DateTimeFormatter.ofPattern("yyyy-MM-dd")
              )

              def fromDesIntToString(taxYear: Int): String =
                s"${taxYear - 1}-${taxYear.toString.drop(2)}"

              if (currentDate.isBefore(taxYearStartDate)) fromDesIntToString(currentDate.getYear) else fromDesIntToString(currentDate.getYear + 1)
            }

            override val taxYear: String = getCurrentTaxYear

            val response: WSResponse = await(request(version).put(requestBodyJson))
            response.status shouldBe BAD_REQUEST
            response.json shouldBe RuleTaxYearNotEndedError.asJson
          }

        }
        versions.foreach(version => {
          s"for version $version" when {
            taxYearNotEnded(version)
          }
        })
      }

      "the invalid payeUnderpayment validation error because" when {
        def invalidPayeUnderpayment(version: String): Unit = {
          "an invalid payeUnderpayment is submitted" in new NonTysTest {
            override val requestBodyJson: JsValue = Json.parse(
              """
              |{
              |  "taxCodeComponents": {
              |    "payeUnderpayment": [
              |      {
              |        "amount": 123498394893843.4,
              |        "id": 12345.35
              |      }
              |    ],
              |    "selfAssessmentUnderpayment": [
              |      {
              |        "amount": 123.45,
              |        "id": 12345
              |      }
              |    ],
              |    "debt": [
              |      {
              |        "amount": 123.45,
              |        "id": 12345
              |      }
              |    ],
              |    "inYearAdjustment": {
              |      "amount": 123.45,
              |      "id": 12345
              |    }
              |  }
              |}
            """.stripMargin
            )

            val response: WSResponse = await(request(version).put(requestBodyJson))
            response.status shouldBe BAD_REQUEST
            response.json shouldBe Json.toJson(
              ErrorWrapper(
                correlationId = "",
                error = BadRequestError,
                errors = Some(Seq(
                  ValueFormatError.copy(
                    paths = Some(List(
                      "/taxCodeComponents/payeUnderpayment/0/amount"
                    ))
                  ),
                  IdFormatError.copy(
                    paths = Some(List(
                      "/taxCodeComponents/payeUnderpayment/0/id"
                    ))
                  )
                ))
              )
            )
          }
        }
        versions.foreach(version => {
          s"for version $version" when {
            invalidPayeUnderpayment(version)
          }
        })
      }

      "the invalid selfAssessmentUnderpayment validation error because" when {
        def invalidSelfAssessmentUnderpayment(version: String): Unit = {
          "an invalid selfAssessmentUnderpayment is submitted" in new NonTysTest {
            override val requestBodyJson: JsValue = Json.parse(
              """
              |{
              |  "taxCodeComponents": {
              |    "payeUnderpayment": [
              |      {
              |        "amount": 123.45,
              |        "id": 12345
              |      }
              |    ],
              |    "selfAssessmentUnderpayment": [
              |      {
              |        "amount": 123498394893843.4,
              |        "id": 12345.35
              |      }
              |    ],
              |    "debt": [
              |      {
              |        "amount": 123.45,
              |        "id": 12345
              |      }
              |    ],
              |    "inYearAdjustment": {
              |      "amount": 123.45,
              |      "id": 12345
              |    }
              |  }
              |}
            """.stripMargin
            )

            val response: WSResponse = await(request(version).put(requestBodyJson))
            response.status shouldBe BAD_REQUEST
            response.json shouldBe Json.toJson(ErrorWrapper(
              correlationId = "",
              error = BadRequestError,
              errors = Some(Seq(
                ValueFormatError.copy(
                  paths = Some(List(
                    "/taxCodeComponents/selfAssessmentUnderpayment/0/amount"
                  ))
                ),
                IdFormatError.copy(
                  paths = Some(List(
                    "/taxCodeComponents/selfAssessmentUnderpayment/0/id"
                  ))
                )
              ))
            ))
          }
        }
        versions.foreach(version => {
          s"for version $version" when {
            invalidSelfAssessmentUnderpayment(version)
          }
        })
      }

      "the invalid debt validation error because" when {
        def invalidDebt(version: String): Unit = {
          "an invalid debt is submitted" in new NonTysTest {
            override val requestBodyJson: JsValue = Json.parse(
              """
              |{
              |  "taxCodeComponents": {
              |    "payeUnderpayment": [
              |      {
              |        "amount": 123.45,
              |        "id": 12345
              |      }
              |    ],
              |    "selfAssessmentUnderpayment": [
              |      {
              |        "amount": 123.45,
              |        "id": 12345
              |      }
              |    ],
              |    "debt": [
              |      {
              |        "amount": 123498394893843.4,
              |        "id": 12345.35
              |      }
              |    ],
              |    "inYearAdjustment": {
              |      "amount": 123.45,
              |      "id": 12345
              |    }
              |  }
              |}
            """.stripMargin
            )

            val response: WSResponse = await(request(version).put(requestBodyJson))
            response.status shouldBe BAD_REQUEST
            response.json shouldBe Json.toJson(ErrorWrapper(
              correlationId = "",
              error = BadRequestError,
              errors = Some(
                Seq(
                  ValueFormatError.copy(
                    paths = Some(List(
                      "/taxCodeComponents/debt/0/amount"
                    ))
                  ),
                  IdFormatError.copy(
                    paths = Some(List(
                      "/taxCodeComponents/debt/0/id"
                    ))
                  )
                ))
            ))
          }
        }
        versions.foreach(version => {
          s"for version $version" when {
            invalidDebt(version)
          }
        })
      }

      "the invalid inYearAdjustment validation error because" when {
        def invalidInYearAdjustment(version: String): Unit = {
          "an invalid inYearAdjustment is submitted" in new NonTysTest {
            override val requestBodyJson: JsValue = Json.parse(
              """
              |{
              |  "taxCodeComponents": {
              |    "payeUnderpayment": [
              |      {
              |        "amount": 123.45,
              |        "id": 12345
              |      }
              |    ],
              |    "selfAssessmentUnderpayment": [
              |      {
              |        "amount": 123.45,
              |        "id": 12345
              |      }
              |    ],
              |    "debt": [
              |      {
              |        "amount": 123.45,
              |        "id": 12345
              |      }
              |    ],
              |    "inYearAdjustment": {
              |      "amount": 123498394893843.4,
              |      "id": 12345.35
              |    }
              |  }
              |}
            """.stripMargin
            )

            val response: WSResponse = await(request(version).put(requestBodyJson))
            response.status shouldBe BAD_REQUEST
            response.json shouldBe Json.toJson(ErrorWrapper(
              correlationId = "",
              error = BadRequestError,
              errors = Some(Seq(
                ValueFormatError.copy(
                  paths = Some(List(
                    "/taxCodeComponents/inYearAdjustment/amount"
                  ))
                ),
                IdFormatError.copy(
                  paths = Some(List(
                    "/taxCodeComponents/inYearAdjustment/id"
                  ))
                )
              ))
            ))
          }
        }
        versions.foreach(version => {
          s"for version $version" when {
            invalidInYearAdjustment(version)
          }
        })
      }

      "the invalid (all) values validation error because" when {
        def allInvalidValues(version: String): Unit = {
          "all values submitted are invalid" in new NonTysTest {
            override val requestBodyJson: JsValue = Json.parse(
              """
              |{
              |  "taxCodeComponents": {
              |    "payeUnderpayment": [
              |      {
              |        "amount": 123.455,
              |        "id": -12345
              |      }
              |    ],
              |    "selfAssessmentUnderpayment": [
              |      {
              |        "amount": 123498394893843.4,
              |        "id": 12345.35
              |      }
              |    ],
              |    "debt": [
              |      {
              |        "amount": -123.45,
              |        "id": 123453456789098765434567897654567890987654
              |      }
              |    ],
              |    "inYearAdjustment": {
              |      "amount": 11111111111111111111111111111123.45,
              |      "id": -12345
              |    }
              |  }
              |}
            """.stripMargin
            )

            val response: WSResponse = await(request(version).put(requestBodyJson))
            response.status shouldBe BAD_REQUEST
            response.json shouldBe Json.toJson(ErrorWrapper(
              correlationId = "",
              error = BadRequestError,
              errors = Some(Seq(
                ValueFormatError.copy(
                  paths = Some(List(
                    "/taxCodeComponents/payeUnderpayment/0/amount",
                    "/taxCodeComponents/selfAssessmentUnderpayment/0/amount",
                    "/taxCodeComponents/debt/0/amount",
                    "/taxCodeComponents/inYearAdjustment/amount"
                  ))
                ),
                IdFormatError.copy(
                  paths = Some(List(
                    "/taxCodeComponents/payeUnderpayment/0/id",
                    "/taxCodeComponents/selfAssessmentUnderpayment/0/id",
                    "/taxCodeComponents/debt/0/id",
                    "/taxCodeComponents/inYearAdjustment/id"
                  ))
                )
              ))
            ))
          }
        }

        versions.foreach(version => {
          s"for version $version" when {
            allInvalidValues(version)
          }
        })
      }

      "the empty body validation error because" when {
        def emptyBodySubmitted(version: String): Unit = {
          "an empty body is submitted" in new NonTysTest {
            override val requestBodyJson: JsValue = Json.parse("{}")

            val response: WSResponse = await(request(version).put(requestBodyJson))
            response.status shouldBe BAD_REQUEST
            response.json shouldBe RuleIncorrectOrEmptyBodyError.asJson
          }
        }
        versions.foreach(version => {
          s"for version $version" when {
            emptyBodySubmitted(version)
          }
        })
      }

    }

    "the error response from downstream is" when {
      def serviceErrorTest(version: String)(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"$downstreamCode and status $downstreamStatus " in new NonTysTest {

          override def setupStubs(): Unit =
            DownstreamStub.onError(DownstreamStub.PUT, downstreamUri, downstreamStatus, errorBody(downstreamCode))

          val response: WSResponse = await(request(version).put(requestBodyJson))
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
        }
      }

      val errors = Seq(
        (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
        (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_PAYLOAD", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "INVALID_REQUEST_TAX_YEAR", BAD_REQUEST, RuleTaxYearNotEndedError),
        (UNPROCESSABLE_ENTITY, "DUPLICATE_ID_NOT_ALLOWED", BAD_REQUEST, RuleDuplicateIdError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError)
      )

      val extraTysErrors = Seq(
        (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
      )

      versions.foreach(version => {
        s"for version $version " when {
          (errors ++ extraTysErrors).foreach(args => (serviceErrorTest(version) _).tupled(args))
        }
      })

    }
  }

}
