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

package v3.retrieveCodingOut.def1

import api.models.domain.{MtdSource, Nino, TaxYear}
import api.models.errors._
import config.MockAppConfig
import support.UnitSpec
import v3.retrieveCodingOut.RetrieveCodingOutValidatorFactory
import v3.retrieveCodingOut.def1.model.request.Def1_RetrieveCodingOutRequestData
import v3.retrieveCodingOut.model.request.RetrieveCodingOutRequestData

class RetrieveCodingOutValidatorFactorySpec extends UnitSpec with MockAppConfig {
  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2021-22"
  private val validSource  = Some("hmrcHeld")

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)
  private val parsedSource  = Some(MtdSource.parser("hmrcHeld"))

  private val validatorFactory = new RetrieveCodingOutValidatorFactory

  private def validator(nino: String, taxYear: String, source: Option[String]) = validatorFactory.validator(nino, taxYear, source, mockAppConfig)

  private def setupMocks(): Unit = (MockAppConfig.minimumPermittedTaxYear returns 2022).anyNumberOfTimes()

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveCodingOutRequestData] =
          validator(validNino, validTaxYear, validSource).validateAndWrapResult()

        result shouldBe Right(Def1_RetrieveCodingOutRequestData(parsedNino, parsedTaxYear, parsedSource))
      }
    }

    "should return a single error" when {
      "an invalid nino is supplied" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveCodingOutRequestData] =
          validator("invalidNino", validTaxYear, validSource).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "an incorrectly formatted taxYear is supplied" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveCodingOutRequestData] =
          validator(validNino, "202122", validSource).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "an invalid tax year range is supplied" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveCodingOutRequestData] =
          validator(validNino, "2020-22", validSource).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

      "an invalid tax year, before the minimum, is supplied" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveCodingOutRequestData] =
          validator(validNino, "2020-21", validSource).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearNotSupportedError))
      }

      "invalid source is supplied" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveCodingOutRequestData] =
          validator(validNino, validTaxYear, Some("badSource")).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, SourceFormatError))
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        setupMocks()

        val result: Either[ErrorWrapper, RetrieveCodingOutRequestData] =
          validator("invalidNino", "invalidTaxYear", Some("badSource")).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, SourceFormatError, TaxYearFormatError))))
      }
    }
  }

}
