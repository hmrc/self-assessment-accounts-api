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

import api.models.errors.{MissingFromDateError, RangeToDateBeforeFromDateError, RuleMissingToDateError}
import support.UnitSpec

class DateRangeValidationV2Spec extends UnitSpec {

  val date2020           = Some("2020-01-01")
  val date2020PlusOneDay = Some("2020-01-02")
  val date2021           = Some("2021-01-01")

  "validate" should {
    "return an empty list" when {
      "passed valid from and to dates" in {
        DateRangeValidationV2.validate(date2020, date2021) shouldBe List()
      }

      "passed no dates" in {
        DateRangeValidationV2.validate(None, None) shouldBe List()
      }

      "both dates are the same" in {
        DateRangeValidationV2.validate(date2020, date2020) shouldBe List()
      }
    }

    "return a list containing a RangeToDateBeforeFromDateError" when {
      "passed a from date which is after the to date" in {
        DateRangeValidationV2.validate(date2021, date2020) shouldBe List(RangeToDateBeforeFromDateError)
      }

      "from date is one day after to date" in {
        DateRangeValidationV2.validate(date2020PlusOneDay, date2020) shouldBe List(RangeToDateBeforeFromDateError)
      }
    }

    "return a list containing a MissingToDateError" when {
      "passed a from date but not a to date" in {
        DateRangeValidationV2.validate(date2020, None) shouldBe List(RuleMissingToDateError)
      }
    }

    "return a list containing a MissingFromDateError" when {
      "passed a to date but not a from date" in {
        DateRangeValidationV2.validate(None, date2021) shouldBe List(MissingFromDateError)
      }
    }
  }

}
