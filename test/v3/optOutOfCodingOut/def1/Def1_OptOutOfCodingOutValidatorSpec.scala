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

package v3.optOutOfCodingOut.def1

import support.UnitSpec
import v3.optOutOfCodingOut.def1.model.request.Def1_OptOutOfCodingOutRequestData

class Def1_OptOutOfCodingOutValidatorSpec extends UnitSpec  {
  private implicit val correlationId: String = "1234"

  private val validNino    = "AA123456A"
  private val validTaxYear = "2023-24"

  private val parsedNino    = Nino(validNino)
  private val parsedTaxYear = TaxYear.fromMtd(validTaxYear)

  private def validator(nino: String, taxYear: String) = new Def1_OptOutOfCodingOutValidator(nino, taxYear)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        validator(validNino, validTaxYear).validateAndWrapResult() shouldBe
          Right(Def1_OptOutOfCodingOutRequestData(parsedNino, parsedTaxYear))
      }
    }

    "should return a single error" when {
      "an invalid nino is supplied" in {
        validator("invalidNino", validTaxYear).validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "an incorrectly formatted taxYear is supplied" in {
        validator(validNino, "202324").validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, TaxYearFormatError))
      }

      "an invalid tax year range is supplied" in {
        validator(validNino, "2022-24").validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, RuleTaxYearRangeInvalidError))
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        validator("invalidNino", "invalidTaxYear").validateAndWrapResult() shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
