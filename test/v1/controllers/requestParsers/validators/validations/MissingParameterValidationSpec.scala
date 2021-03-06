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

package v1.controllers.requestParsers.validators.validations

import support.UnitSpec
import v1.models.errors.MissingFromDateError

class MissingParameterValidationSpec extends UnitSpec {
  "validate" should {
    "return an empty list" when {
      "passed a non-empty Option" in {
        MissingParameterValidation.validate(Some("from date"), MissingFromDateError) shouldBe List()
      }
    }
    "return a list containing an error" when {
      "passed an empty Option" in {
        MissingParameterValidation.validate(None, MissingFromDateError) shouldBe List(MissingFromDateError)
      }
    }
  }
}
