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

import config.AppConfig
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.models.errors.{NinoFormatError, RuleIncorrectOrEmptyBodyError, RuleTaxYearRangeInvalidError, TaxYearFormatError, ValueFormatError}
import v1.models.request.createOrAmendCodingOut.CreateOrAmendCodingOutRawRequest

class CreateOrAmendCodingOutValidatorSpec extends UnitSpec{

  private val validNino = "AA111111A"
  private val validTaxYear = "2019-20"
  private val invalidNino = "not a nino"
  private val invalidTaxYear = "not a tax year"
  private val invalidTaxYearRange = "2021-25"

  private val validJson = Json.parse(
    """
      |{
      |   "payeUnderpayments": 2000.99,
      |   "selfAssessmentUnderPayments": 2000.99,
      |   "debts": 2000.99,
      |   "inYearAdjustments": 5000.99
      |}
      |""".stripMargin)

  private val validPartiallyEmptyJson = Json.parse(
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

  private val emptyJson = Json.parse(
    """
      |{}
      |""".stripMargin)

  class Test extends MockAppConfig {
    implicit val appConfig: AppConfig = mockAppConfig
    val validator = new CreateOrAmendCodingOutValidator()
    MockAppConfig.minimumPermittedTaxYear returns 2020
    val emptyRequestBodyJson: JsValue = Json.parse("""{}""")
  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, validTaxYear, validJson)) shouldBe Nil
      }

      "a valid request is supplied missing optional fields" in new Test {
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, validTaxYear, validPartiallyEmptyJson)) shouldBe Nil
      }
    }

    "return NinoFormatError" when {
      "an invalid nino is supplied" in new Test {
        validator.validate(CreateOrAmendCodingOutRawRequest(invalidNino, validTaxYear, validJson)) shouldBe List(NinoFormatError)
      }
    }

    "return a TaxYearFormatError" when {
      "an invalid tax year is supplied" in new Test {
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, invalidTaxYear, validJson)) shouldBe List(TaxYearFormatError)
      }
    }

    "return a RuleIncorrectOrEmptyBodyError" when {
      "an empty body is supplied" in new Test {
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, validTaxYear, emptyJson)) shouldBe List(RuleIncorrectOrEmptyBodyError)
      }
    }

    "return a RuleTaxYearRangeInvalidError" when {
      "the tax year range is invalid" in new Test{
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, invalidTaxYearRange, validJson)) shouldBe List(
          RuleTaxYearRangeInvalidError
        )
      }
    }

    "return NinoFormatError and TaxYearFormatError" when {
      "an invalid tax year and an invalid nino is supplied" in new Test {
        validator.validate(CreateOrAmendCodingOutRawRequest(invalidNino, invalidTaxYear, validJson)) shouldBe List(
          NinoFormatError,
          TaxYearFormatError
        )
      }
    }

    "return ValueFormatError" when {
      "a request with invalid fields is supplied" in new Test {
        validator.validate(CreateOrAmendCodingOutRawRequest(validNino, validTaxYear, invalidJson)) shouldBe List(ValueFormatError.copy(paths =
          Some(Seq(
            "/payeUnderpayments",
            "/selfAssessmentUnderPayments",
            "/debts",
            "/inYearAdjustments"
          )))
        )
      }
    }
  }

}
