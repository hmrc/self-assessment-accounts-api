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

package api.controllers.requestParsers.validators.validations

import api.models.errors.{V1_FromDateFormatError, V1_ToDateFormatError}
import support.UnitSpec

class DateFormatValidationSpec extends UnitSpec {

  private val dateFormatValidation: DateFormatValidation = new DateFormatValidation(minYear = 1900, maxYear = 2100)

  "validate" should {
    "return an empty list" when {
      "passed a valid date" in {
        dateFormatValidation.validate("2019-02-02", isFromDate = true, V1_FromDateFormatError) shouldBe List()
      }

      "passed an optional valid date" in {
        dateFormatValidation.validate(Some("2019-02-02"), isFromDate = true, V1_FromDateFormatError) shouldBe List()
      }
    }

    "return a list containing an error" when {
      "passed a date with an invalid month" in {
        dateFormatValidation.validate("2019-13-02", isFromDate = true, V1_FromDateFormatError) shouldBe List(V1_FromDateFormatError)
      }
      "passed a date with an invalid day" in {
        dateFormatValidation.validate("2019-02-32", isFromDate = true, V1_FromDateFormatError) shouldBe List(V1_FromDateFormatError)
      }
      "passed a date with an invalid year" in {
        dateFormatValidation.validate("201-02-02", isFromDate = true, V1_FromDateFormatError) shouldBe List(V1_FromDateFormatError)
      }
      "passed a date with an invalid separator" in {
        dateFormatValidation.validate("2012.02-02", isFromDate = true, V1_FromDateFormatError) shouldBe List(V1_FromDateFormatError)
      }
      "passed a date written as text" in {
        dateFormatValidation.validate("2nd Feb 2012", isFromDate = true, V1_FromDateFormatError) shouldBe List(V1_FromDateFormatError)
      }
      "passed a optional invalid date" in {
        dateFormatValidation.validate(Some("2nd Feb 2012"), isFromDate = true, V1_FromDateFormatError) shouldBe List(V1_FromDateFormatError)
      }
      "passed a from date before 1900" in {
        dateFormatValidation.validate(Some("1899-11-02"), isFromDate = true, V1_FromDateFormatError) shouldBe List(V1_FromDateFormatError)
      }
      "passed a to date after 2100" in {
        dateFormatValidation.validate(Some("2100-12-31"), isFromDate = false, V1_ToDateFormatError) shouldBe List(V1_ToDateFormatError)
      }
    }
  }

}
