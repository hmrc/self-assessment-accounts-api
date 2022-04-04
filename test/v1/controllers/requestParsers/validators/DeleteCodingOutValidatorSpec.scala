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

package v1.controllers.requestParsers.validators

import config.AppConfig
import mocks.MockAppConfig
import support.UnitSpec
import v1.models.errors._
import v1.models.request.deleteCodingOut.DeleteCodingOutRawRequest

class DeleteCodingOutValidatorSpec extends UnitSpec {

  private val validTaxYear = "2021-22"
  private val validNino    = "AA123456B"

  class Test extends MockAppConfig {

    implicit val appConfig: AppConfig = mockAppConfig

    val validator = new DeleteCodingOutValidator()

    MockAppConfig.minimumPermittedTaxYear
      .returns(2022)
      .anyNumberOfTimes()

  }

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in new Test {
        validator.validate(DeleteCodingOutRawRequest(validNino, validTaxYear)) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "invalid nino is supplied" in new Test {
        validator.validate(DeleteCodingOutRawRequest("badNino", validTaxYear)) shouldBe
          List(NinoFormatError)
      }
    }

    "return TaxYearFormatError error" when {
      "invalid taxYear is supplied" in new Test {
        validator.validate(DeleteCodingOutRawRequest(validNino, "badTaxYear")) shouldBe
          List(TaxYearFormatError)
      }
    }

    "return RuleTaxYearNotSupportedError error" when {
      "a taxYear supplied is not supported" in new Test {
        validator.validate(DeleteCodingOutRawRequest(validNino, "2020-21")) shouldBe
          List(RuleTaxYearNotSupportedError)
      }
    }

    "return RuleTaxYearRangeInvalid error" when {
      "an invalid tax year range is supplied" in new Test {
        validator.validate(DeleteCodingOutRawRequest(validNino, "2020-22")) shouldBe
          List(RuleTaxYearRangeInvalidError)
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in new Test {
        validator.validate(DeleteCodingOutRawRequest("badNino", "badTaxYear")) shouldBe
          List(NinoFormatError, TaxYearFormatError)
      }
    }
  }

}
