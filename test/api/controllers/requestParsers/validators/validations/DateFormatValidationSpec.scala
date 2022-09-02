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

import api.models.errors.FromDateFormatError
import support.UnitSpec

class DateFormatValidationSpec extends UnitSpec {

  "validate" should {
    "return an empty list" when {
      "passed a valid date" in {
        DateFormatValidation.validate("2019-02-02", FromDateFormatError) shouldBe List()
      }
    }
    "return a list containing an error" when {
      "passed a date with an invalid month" in {
        DateFormatValidation.validate("2019-13-02", FromDateFormatError) shouldBe List(FromDateFormatError)
      }
      "passed a date with an invalid day" in {
        DateFormatValidation.validate("2019-02-32", FromDateFormatError) shouldBe List(FromDateFormatError)
      }
      "passed a date with an invalid year" in {
        DateFormatValidation.validate("201-02-02", FromDateFormatError) shouldBe List(FromDateFormatError)
      }
      "passed a date with an invalid separator" in {
        DateFormatValidation.validate("2012.02-02", FromDateFormatError) shouldBe List(FromDateFormatError)
      }
      "passed a date written as text" in {
        DateFormatValidation.validate("2nd Feb 2012", FromDateFormatError) shouldBe List(FromDateFormatError)
      }
    }
  }

}