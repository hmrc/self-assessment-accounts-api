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

package api.controllers.requestParsers.validators.validations

import api.models.errors.DocNumberFormatError
import api.models.utils.JsonErrorValidators
import support.UnitSpec

class DocNumberValidationSpec extends UnitSpec with JsonErrorValidators {

  "validate" should {
    "return no errors" when {
      "when a valid doc number is supplied" in {
        val validDocNumber   = "1234"
        val validationResult = DocNumberValidation.validate(validDocNumber)
        validationResult shouldBe Nil
      }

      "when a valid optional doc number is supplied" in {
        val validDocNumber   = Some("1234")
        val validationResult = DocNumberValidation.validate(validDocNumber)
        validationResult shouldBe Nil
      }
    }

    "return an error" when {
      "when an invalid doc number is supplied" in {
        val invalidDocNumber = "a" * 13
        val validationResult = DocNumberValidation.validate(invalidDocNumber)
        validationResult shouldBe List(DocNumberFormatError)
      }

      "when an invalid optional doc number is supplied" in {
        val invalidDocNumber = Some("a" * 13)
        val validationResult = DocNumberValidation.validate(invalidDocNumber)
        validationResult shouldBe List(DocNumberFormatError)
      }
    }
  }

}
