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
      "no value is submitted" in {
        DocNumberValidation.validate(None) shouldBe empty
      }

      "when a value of minimum length is supplied" in {
        DocNumberValidation.validate(Some("a")) shouldBe empty
      }

      "when a value of maximum length is supplied" in {
        DocNumberValidation.validate(Some("a" * 12)) shouldBe empty
      }
    }

    "return an error" when {
      "when a empty value is supplied" in {
        DocNumberValidation.validate(Some("")) shouldBe List(DocNumberFormatError)
      }

      "when a value that is too long is supplied" in {
        DocNumberValidation.validate(Some("a" * 13)) shouldBe List(DocNumberFormatError)
      }
    }
  }

}
