/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1.fixtures.RetrieveChargeHistoryFixture._
import v1.models.errors.ChargeIdFormatError
import v1.models.utils.JsonErrorValidators

class ChargeIdValidationSpec extends UnitSpec with JsonErrorValidators {

  "validate" should {
    "return no errors" when {
      "when a valid charge ID is supplied" in {
        val validationResult = ChargeIdValidation.validate(validChargeId)
        validationResult.isEmpty shouldBe true

      }
    }

    "return an error" when {
      "when an invalid charge ID is supplied" in {
        val validationResult = ChargeIdValidation.validate(invalidChargeId)
        validationResult.isEmpty shouldBe false
        validationResult.length shouldBe 1
        validationResult.head shouldBe ChargeIdFormatError

      }
    }

  }
}
