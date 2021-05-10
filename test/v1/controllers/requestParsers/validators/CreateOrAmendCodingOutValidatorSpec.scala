/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators

import play.api.libs.json.Json
import play.api.mvc.AnyContentAsJson
import support.UnitSpec
import v1.models.errors.{NinoFormatError, TaxYearFormatError, ValueFormatError}
import v1.controllers.requestParsers.validators.CreateOrAmendCodingOutValidator

class CreateOrAmendCodingOutValidatorSpec extends UnitSpec{

  val validator = CreateOrAmendCodingOutValidator()

  private val validNino = "AA111111A"
  private val validTaxYear = "2021-22"
  private val invalidNino = "not a nino"
  private val invalidTaxYear = "not a tax year"

  private val validJson = Json.parse(
    """
      |{
      |   "payeUnderpayments": 2000.99,
      |   "selfAssessmentUnderPayments": 2000.99,
      |   "debts": 2000.99,
      |   "inYearAdjustments": 5000.99
      |}
      |""".stripMargin)

  private val validEmptyJson = Json.parse(
    """
      |{
      |   "payeUnderpayments": 2000.99,
      |   "selfAssessmentUnderPayments": 2000.99,
      |   "debts": 2000.99
      |}
      |""".stripMargin)

  private val invalidJson = Json.parse(
    """
      |{
      |   "payeUnderpayments": -2000.99,
      |   "selfAssessmentUnderPayments": 2000.999,
      |   "debts": 199999999999.99,
      |   "inYearAdjustments": -5000.99
      |}
      |""".stripMargin)



  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, validTaxYear, AnyContentAsJson(validJson))) shouldBe Nil
      }

      "a valid request is supplied missing optional fields" in {
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, validTaxYear, AnyContentAsJson(validEmptyJson))) shouldBe Nil
      }
    }

    "return NinoFormatError" when {
      "an invalid nino is supplied" in {
        validator.validate(CreateOrAmendCodingOutRawRequest(invalidNino, validTaxYear, AnyContentAsJson(validJson))) shouldBe List(NinoFormatError)
      }
    }

    "return TaxYearFormatError" when {
      "an invalid tax year is supplied" in {
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, invalidTaxYear, AnyContentAsJson(validJson))) shouldBe List(TaxYearFormatError)
      }
    }

    "return NinoFormatError and TaxYearFormatError" when {
      "an invalid tax year and an invalid nino is supplied" in {
        validator.validate(CreateOrAmendCodingOutRawRequest(invalidNino, invalidTaxYear, AnyContentAsJson(validJson))) shouldBe List(TaxYearFormatError, NinoFormatError)
      }
    }

    "return ValueFormatError" when {
      "a request with invalid values is supplied" in {
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, validTaxYear, AnyContentAsJson(invalidJson))) shouldBe List(ValueFormatError,
          Some(List(
            "/payeUnderpayments",
            "/selfAssessmentUnderPayments",
            "/debts",
            "/inYearAdjustments"
          ))
        )
      }
    }
  }

}
