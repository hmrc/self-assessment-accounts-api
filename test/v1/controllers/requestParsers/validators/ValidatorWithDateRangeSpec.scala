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

package v1.controllers.requestParsers.validators

import support.UnitSpec
import v1.models.errors._
import v1.models.request.RawDataWithDateRange

class ValidatorWithDateRangeSpec extends UnitSpec {

  case class TestRawDataWithDateRange(nino: String, from: Option[String], to: Option[String]) extends RawDataWithDateRange
  class TestValidatorWithDateRange extends ValidatorWithDateRange[TestRawDataWithDateRange]
  val validator = new TestValidatorWithDateRange

  "validation" should {
    "return no errors" when {
      "passed valid raw data" in {
        val input = TestRawDataWithDateRange("AA999999A", Some("2019-02-01"), Some("2019-02-02"))
        validator.validate(input) shouldBe List()
      }
      "passed a to date which is the same as the from date" in {
        val input = TestRawDataWithDateRange("AA999999A", Some("2019-02-02"), Some("2019-02-02"))
        validator.validate(input) shouldBe List()
      }
    }
    "return a single error" when {
      "passed an invalid nino" in {
        val input = TestRawDataWithDateRange("AA999999AA", Some("2019-02-01"), Some("2019-02-02"))
        validator.validate(input) shouldBe List(NinoFormatError)
      }
      "passed an invalid from date format" in {
        val input = TestRawDataWithDateRange("AA999999A", Some("2019-022-01"), Some("2019-02-02"))
        validator.validate(input) shouldBe List(FromDateFormatError)
      }
      "passed an invalid to date format" in {
        val input = TestRawDataWithDateRange("AA999999A", Some("2019-02-01"), Some("2019-022-02"))
        validator.validate(input) shouldBe List(ToDateFormatError)
      }
      "passed a missing from date" in {
        val input = TestRawDataWithDateRange("AA999999A", None, Some("2019-02-02"))
        validator.validate(input) shouldBe List(MissingFromDateError)
      }
      "passed a missing to date" in {
        val input = TestRawDataWithDateRange("AA999999A", Some("2019-02-01"), None)
        validator.validate(input) shouldBe List(MissingToDateError)
      }
      "passed a to date before the from date" in {
        val input = TestRawDataWithDateRange("AA999999A", Some("2019-02-02"), Some("2019-02-01"))
        validator.validate(input) shouldBe List(RangeToDateBeforeFromDateError)
      }
      "passed a date range which is too large" in {
        val input = TestRawDataWithDateRange("AA999999A", Some("2019-02-02"), Some("2021-02-03"))
        validator.validate(input) shouldBe List(RuleDateRangeInvalidError)
      }
      "passed a from date which is too early" in {
        val input = TestRawDataWithDateRange("AA999999A", Some("2017-02-02"), Some("2018-02-03"))
        validator.validate(input) shouldBe List(RuleFromDateNotSupportedError)
      }
    }
    "return multiple errors" when {
      "multiple parameters are missing" in {
        val input = TestRawDataWithDateRange("AA999999A", None, None)
        validator.validate(input) shouldBe List(MissingFromDateError, MissingToDateError)
      }
      "multiple invalid parameters are provided" in {
        val input = TestRawDataWithDateRange("AA999999A", Some("2017-02-02"), Some("2021-02-03"))
        validator.validate(input) shouldBe List(RuleFromDateNotSupportedError, RuleDateRangeInvalidError)
      }
    }
  }
}
