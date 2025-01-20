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

package v3.createOrAmendCodingOut.def1

import config.MockSaAccountsConfig
import play.api.libs.json.{JsObject, JsPath, JsValue, Json}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.utils.UnitSpec
import v3.createOrAmendCodingOut.def1.model.request.{Def1_CreateOrAmendCodingOutRequestBody, Def1_CreateOrAmendCodingOutRequestData, TaxCodeComponents}

class Def1_CreateOrAmendCodingOutValidatorSpec extends UnitSpec with MockSaAccountsConfig {

  private implicit val correlationId: String = "1234"

  private val validNino      = "AA111111A"
  private val validTaxYear   = "2021-22"
  private val invalidNino    = "not a nino"
  private val invalidTaxYear = "not a tax year"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private val validJson = Json
    .parse(
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
        |      "amount": 123.45,
        |      "id": 12345
        |    }
        |  }
        |}
    """.stripMargin
    )
    .as[JsObject]

  private val invalidJson = Json.parse(
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

  private val parsedBody = validJson.as[Def1_CreateOrAmendCodingOutRequestBody]
  import parsedBody.{taxCodeComponents => parsedTaxCodeComponents}

  private def validJsonWithout(field: String): JsObject =
    (JsPath \ "taxCodeComponents" \ field)
      .prune(validJson)
      .getOrElse(fail(s"Field not in validJson: $field"))

  private def validator(nino: String, taxYear: String, body: JsValue) =
    new Def1_CreateOrAmendCodingOutValidator(nino, taxYear, body, temporalValidationEnabled = true, mockSaAccountsConfig)

  private def setupMocks(): Unit = (MockedSaAccountsConfig.minimumPermittedTaxYear returns 2022).anyNumberOfTimes()

  "running a validation" should {
    "return the parsed domain object" when {
      "given a valid request" in {
        setupMocks()
        val result = validator(validNino, validTaxYear, validJson).validateAndWrapResult()

        result shouldBe Right(
          Def1_CreateOrAmendCodingOutRequestData(parsedNino, parsedTaxYear, parsedBody)
        )
      }

      def testValidWithMissingField(field: String, taxCodeComponents: TaxCodeComponents): Unit = {
        setupMocks()
        val json     = validJsonWithout(field)
        val expected = parsedBody.copy(taxCodeComponents = taxCodeComponents)

        withClue(s"Should be missing: $field") {
          val result = validator(validNino, validTaxYear, json).validateAndWrapResult()
          result shouldBe Right(
            Def1_CreateOrAmendCodingOutRequestData(parsedNino, parsedTaxYear, expected)
          )
        }
      }

      "a valid request is supplied missing payeUnderpayment" in {
        testValidWithMissingField("payeUnderpayment", parsedTaxCodeComponents.copy(payeUnderpayment = None))
      }

      "a valid request is supplied missing selfAssessmentUnderpayment" in {
        testValidWithMissingField("selfAssessmentUnderpayment", parsedTaxCodeComponents.copy(selfAssessmentUnderpayment = None))
      }

      "a valid request is supplied missing debt" in {
        testValidWithMissingField("debt", parsedTaxCodeComponents.copy(debt = None))
      }

      "a valid request is supplied missing inYearAdjustment" in {
        testValidWithMissingField("inYearAdjustment", parsedTaxCodeComponents.copy(inYearAdjustment = None))
      }
    }

    "return NinoFormatError" when {
      "given an invalid nino" in {
        setupMocks()
        val result = validator(invalidNino, validTaxYear, validJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, NinoFormatError)
        )
      }
    }

    "return TaxYearFormatError" when {
      "given an invalid tax year" in {
        setupMocks()
        val result = validator(validNino, invalidTaxYear, validJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, TaxYearFormatError)
        )
      }
    }

    "return RuleIncorrectOrEmptyBodyError" when {
      "given an empty body" in {
        setupMocks()
        val result = validator(validNino, validTaxYear, JsObject.empty).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError)
        )
      }

      "an empty taxCodeComponents is supplied" in {
        setupMocks()
        val json   = Json.obj("taxCodeComponents" -> JsObject.empty)
        val result = validator(validNino, validTaxYear, json).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleIncorrectOrEmptyBodyError.withPath("/taxCodeComponents"))
        )
      }

      "Json arrays inside taxCodeComponents are empty" in {
        setupMocks()

        val json = Json.parse("""
            |{
            |  "taxCodeComponents": {
            |    "payeUnderpayment": [],
            |    "selfAssessmentUnderpayment": [],
            |    "debt": []
            |  }
            |}
            |""".stripMargin)

        val result = validator(validNino, validTaxYear, json).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            RuleIncorrectOrEmptyBodyError.withPaths(
              List(
                "/taxCodeComponents/payeUnderpayment",
                "/taxCodeComponents/selfAssessmentUnderpayment",
                "/taxCodeComponents/debt"
              ))
          )
        )
      }
    }

    "return RuleTaxYearRangeInvalidError" when {
      "given a tax year range of more than one year" in {
        setupMocks()

        val result = validator(validNino, "2021-25", validJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError)
        )
      }
    }

    "return RuleTaxYearNotSupportedError" when {
      "given a tax year that isn't supported" in {
        setupMocks()

        val result = validator(validNino, "2020-21", validJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearNotSupportedError)
        )
      }
    }

    "return RuleTaxYearNotEndedError" when {
      "given a tax year that hasn't ended" in {
        setupMocks()

        val result = validator(validNino, "2024-25", validJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearNotEndedError)
        )
      }
    }

    "return NinoFormatError and TaxYearFormatError" when {
      "the request has multiple errors" in {
        setupMocks()

        val result = validator(invalidNino, invalidTaxYear, validJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(NinoFormatError, TaxYearFormatError))
          )
        )
      }
    }

    "return ValueFormatError and IdFormatError" when {
      "given a request with invalid fields" in {
        setupMocks()

        val result = validator(validNino, validTaxYear, invalidJson).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(
              IdFormatError.withPaths(List(
                "/taxCodeComponents/payeUnderpayment/0/id",
                "/taxCodeComponents/selfAssessmentUnderpayment/0/id",
                "/taxCodeComponents/debt/0/id",
                "/taxCodeComponents/inYearAdjustment/id"
              )),
              ValueFormatError.withPaths(List(
                "/taxCodeComponents/payeUnderpayment/0/amount",
                "/taxCodeComponents/selfAssessmentUnderpayment/0/amount",
                "/taxCodeComponents/debt/0/amount",
                "/taxCodeComponents/inYearAdjustment/amount"
              ))
            ))
          )
        )
      }
    }
  }

}
