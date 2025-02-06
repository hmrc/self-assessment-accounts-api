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

package v4.retrieveCodingOutStatus.def1

import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.utils.UnitSpec
import v4.retrieveCodingOutStatus.def1.model.request.Def1_RetrieveCodingOutStatusRequestData
import v4.retrieveCodingOutStatus.model.request.RetrieveCodingOutStatusRequestData

class Def1_RetrieveCodingOutStatusValidatorSpec extends UnitSpec {
  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2023-24"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private def validator(nino: String, taxYear: String) = new Def1_RetrieveCodingOutStatusValidator(nino, taxYear)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {

        val result: Either[ErrorWrapper, RetrieveCodingOutStatusRequestData] =
          validator(validNino, validTaxYear).validateAndWrapResult()

        result shouldBe Right(Def1_RetrieveCodingOutStatusRequestData(parsedNino, parsedTaxYear))
      }
    }

    "should return a single error" when {
      "an invalid nino is supplied" in {

        val result: Either[ErrorWrapper, RetrieveCodingOutStatusRequestData] =
          validator("invalidNino", validTaxYear).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "an incorrectly formatted taxYear is supplied" in {

        val result: Either[ErrorWrapper, RetrieveCodingOutStatusRequestData] =
          validator(validNino, "202324").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "an invalid tax year range is supplied" in {

        val result: Either[ErrorWrapper, RetrieveCodingOutStatusRequestData] =
          validator(validNino, "2022-24").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }

    }

    "return multiple errors" when {
      "request supplied has multiple errors" in {

        val result: Either[ErrorWrapper, RetrieveCodingOutStatusRequestData] =
          validator("invalidNino", "invalidTaxYear").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
