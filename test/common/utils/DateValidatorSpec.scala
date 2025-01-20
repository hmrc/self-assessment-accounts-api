/*
 * Copyright 2025 HM Revenue & Customs
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

package common.utils

import cats.data.Validated.{invalid, valid}
import shared.models.domain.DateRange
import shared.models.errors.RangeToDateBeforeFromDateError
import shared.utils.UnitSpec

import java.time.LocalDate

class DateValidatorSpec extends UnitSpec {

  private val fromDate = LocalDate.parse("2025-03-29")
  private val toDate = LocalDate.parse("2025-12-29")

  "DateValidator.validateSameDates" should {
    "return valid" when {
      "different dates are supplied" in {
        DateValidator.validateSameDates(DateRange(fromDate, toDate)) shouldBe valid(DateRange(fromDate, toDate))
      }
    }

    "return RangeToDateBeforeFromDateError" when {
      "same dates are supplied" in {
        DateValidator.validateSameDates(DateRange(fromDate, fromDate)) shouldBe invalid(List(RangeToDateBeforeFromDateError))
      }
    }
  }
}
