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

package v4.deleteCodingOut.def1.models

import config.MockSaAccountsConfig
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.*
import shared.utils.UnitSpec
import v4.deleteCodingOut.def1.Def1_DeleteCodingOutValidator
import v4.deleteCodingOut.def1.model.request.Def1_DeleteCodingOutRequestData

class Def1_DeleteCodingOutValidatorSpec extends UnitSpec with MockSaAccountsConfig {

  private implicit val correlationId: String = "1234"

  private val validNino      = "AA111111A"
  private val validTaxYear   = "2021-22"
  private val invalidNino    = "not a nino"
  private val invalidTaxYear = "not a tax year"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private def validator(nino: String, taxYear: String) =
    new Def1_DeleteCodingOutValidator(nino, taxYear, mockSaAccountsConfig)

  private def setupMocks(): Unit = (MockedSaAccountsConfig.minimumPermittedTaxYear returns 2022).anyNumberOfTimes()

  "running a validation" should {
    "return the parsed domain object" when {
      "given a valid request" in {
        setupMocks()
        val result = validator(validNino, validTaxYear).validateAndWrapResult()

        result shouldBe Right(
          Def1_DeleteCodingOutRequestData(parsedNino, parsedTaxYear)
        )
      }
    }

    "return NinoFormatError" when {
      "given an invalid nino" in {
        setupMocks()
        val result = validator(invalidNino, validTaxYear).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, NinoFormatError)
        )
      }
    }

    "return TaxYearFormatError" when {
      "given an invalid tax year" in {
        setupMocks()
        val result = validator(validNino, invalidTaxYear).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, TaxYearFormatError)
        )
      }
    }

    "return RuleTaxYearRangeInvalidError" when {
      "given a tax year range of more than one year" in {
        setupMocks()

        val result = validator(validNino, "2021-25").validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError)
        )
      }
    }

    "return RuleTaxYearNotSupportedError" when {
      "given a tax year that isn't supported" in {
        setupMocks()

        val result = validator(validNino, "2020-21").validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(correlationId, RuleTaxYearNotSupportedError)
        )
      }
    }

    "return NinoFormatError and TaxYearFormatError" when {
      "the request has multiple errors" in {
        setupMocks()

        val result = validator(invalidNino, invalidTaxYear).validateAndWrapResult()
        result shouldBe Left(
          ErrorWrapper(
            correlationId,
            BadRequestError,
            Some(List(NinoFormatError, TaxYearFormatError))
          )
        )
      }
    }

  }

}
