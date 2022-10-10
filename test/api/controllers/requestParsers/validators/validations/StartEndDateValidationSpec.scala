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

import api.models.errors.{EndDateFormatError, MissingFromDateError, MissingToDateError, RuleDateRangeInvalidError, RuleEndBeforeStartError, StartDateFormatError}
import support.UnitSpec

class StartEndDateValidationSpec extends UnitSpec {

  val date2020: Option[String] = Some("2020-01-01")
  val date2021: Option[String] = Some("2021-01-01")
  val date2022: Option[String] = Some("2022-01-01")

  "validate" should {
    "return an empty list" when {
      "passed valid start and end dates" in {
        StartEndDateValidation.validate(date2020, date2021) shouldBe List()
      }
      "passed no dates" in {
        StartEndDateValidation.validate(None, None) shouldBe List()
      }
    }
    "return a list containing an error" when {
      "passed a start date which is after the end date" in {
        StartEndDateValidation.validate(date2021, date2020) shouldBe List(RuleEndBeforeStartError)
      }
      "passed a start date but not a end date" in {
        StartEndDateValidation.validate(date2020, None) shouldBe List(MissingToDateError)
      }
      "passed a end date but not a start date" in {
        StartEndDateValidation.validate(None, date2021) shouldBe List(MissingFromDateError)
      }
      "passed a valid start date and an invalid end date" in {
        StartEndDateValidation.validate(date2021, Some("invalid")) shouldBe List(EndDateFormatError)
      }
      "passed a invalid start date and an valid end date" in {
        StartEndDateValidation.validate(Some("invalid"), date2021) shouldBe List(StartDateFormatError)
      }
      "passed a start date and end date with invalid date range" in {
        StartEndDateValidation.validate(date2020, date2022) shouldBe List(RuleDateRangeInvalidError)
      }
    }
  }

}
