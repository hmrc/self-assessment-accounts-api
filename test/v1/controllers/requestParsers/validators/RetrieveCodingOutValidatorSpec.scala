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
import support.UnitSpec
import v1.models.errors._
import v1.models.request.retrieveCodingOut.RetrieveCodingOutRawRequest

class RetrieveCodingOutValidatorSpec extends UnitSpec {

  private val validNino = "AA111111A"
  private val validTaxYear = "2021-22"
  private val validSource = Some("hmrcHeld")

  class Test extends MockAppConfig {

    implicit val appConfig: AppConfig = mockAppConfig

    val validator = new RetrieveCodingOutValidator()

    MockedAppConfig.minimumPermittedTaxYear
      .returns(2022)
      .anyNumberOfTimes()
  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test{
        validator.validate(RetrieveCodingOutRawRequest(validNino, validTaxYear, validSource)) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in new Test{
        validator.validate(RetrieveCodingOutRawRequest("badNino", validTaxYear, validSource)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "invalid taxYear is supplied" in new Test  {
        validator.validate(RetrieveCodingOutRawRequest(validNino, "badTaxYear", validSource)) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return SourceFormatError error" when {
      "invalid source is supplied" in new Test  {
        validator.validate(RetrieveCodingOutRawRequest(validNino, validTaxYear, Some("badSource"))) shouldBe
          List(SourceFormatError)
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "a taxYear supplied is not supported" in new Test  {
        validator.validate(RetrieveCodingOutRawRequest(validNino, "2020-21", validSource)) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleTaxYearRangeInvalid error" when {
      "an invalid tax year range is supplied" in new Test  {
        validator.validate(RetrieveCodingOutRawRequest(validNino, "2020-22", validSource)) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in new Test  {
        validator.validate(RetrieveCodingOutRawRequest("badNino", "badTaxYear", Some("badSource"))) shouldBe
          List(NinoFormatError, TaxYearFormatError, SourceFormatError)
      }
    }

  }
}
