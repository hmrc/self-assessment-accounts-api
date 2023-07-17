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

import api.models.errors.RuleInconsistentQueryParamsError
import api.models.utils.JsonErrorValidators
import support.UnitSpec

class OnlyOpenItemsValidationSpec extends UnitSpec with JsonErrorValidators {

  "validate" should {
    "return no errors" when {
      "only docNumber is true" in {
        val validationResult =
          OnlyOpenItemsValidation.validate(onlyOpenItems = Some("false"), docNumber = Some("0123456"), fromDate = None, toDate = None)
        validationResult shouldBe Nil
      }

      "only a date range is provided" in {
        val validationResult =
          OnlyOpenItemsValidation.validate(
            onlyOpenItems = Some("false"),
            docNumber = None,
            fromDate = Some("2022-08-15"),
            toDate = Some("2022-09-15"))
        validationResult shouldBe Nil
      }

      "only onlyOpenItems is true" in {
        val validationResult =
          OnlyOpenItemsValidation.validate(onlyOpenItems = Some("true"), docNumber = None, fromDate = None, toDate = None)
        validationResult shouldBe Nil
      }
    }
    "return inconsistentQueryParams error" when {
      "both onlyOpenItems and docNumber are provided" in {
        val validationResult =
          OnlyOpenItemsValidation.validate(onlyOpenItems = Some("true"), docNumber = Some("0123456"), fromDate = None, toDate = None)
        validationResult shouldBe List(RuleInconsistentQueryParamsError)
      }
      "both onlyOpenItems and date range are provided" in {
        val validationResult =
          OnlyOpenItemsValidation.validate(onlyOpenItems = Some("true"), docNumber = None, fromDate = Some("2022-08-15"), toDate = Some("2022-09-15"))
        validationResult shouldBe List(RuleInconsistentQueryParamsError)
      }
      "everything is provided" in {
        val validationResult =
          OnlyOpenItemsValidation.validate(
            onlyOpenItems = Some("true"),
            docNumber = Some("0123456"),
            fromDate = Some("2022-08-15"),
            toDate = Some("2022-09-15"))
        validationResult shouldBe List(RuleInconsistentQueryParamsError)
      }
    }
  }

}
