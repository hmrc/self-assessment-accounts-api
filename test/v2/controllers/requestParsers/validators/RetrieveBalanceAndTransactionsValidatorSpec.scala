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

package v2.controllers.requestParsers.validators

import api.models.errors.{
  InvalidCalculateAccruedInterestError,
  InvalidCustomerPaymentInformationError,
  InvalidDateFromError,
  InvalidDateRangeError,
  InvalidDateToError,
  InvalidDocNumberError,
  InvalidIncludeLocksError,
  InvalidIncludeChargeEstimateError,
  InvalidOnlyOpenItemsError,
  InvalidRemovePaymentOnAccountError,
  NinoFormatError
}
import mocks.MockAppConfig
import support.UnitSpec
import v2.fixtures.retrieveBalanceAndTransactions.RequestFixture._

class RetrieveBalanceAndTransactionsValidatorSpec extends UnitSpec with MockAppConfig {

  private val validator = new RetrieveBalanceAndTransactionsValidator(mockAppConfig)

  "running a validation" should {
    "return no errors" when {
      "a valid request with doc number is supplied" in {
        validator.validate(inputDataDocNumber) shouldBe Nil
      }

      "a valid request with date range is supplied" in {
        validator.validate(inputDataDateRange) shouldBe Nil
      }

      "a valid request with everything true is supplied" in {
        validator.validate(inputDataEverythingTrue) shouldBe Nil
      }
    }

    "return a parameter error" when {
      "an invalid nino is supplied" in {
        validator.validate(inputDataDocNumber.copy(nino = "nino")) shouldBe List(NinoFormatError)
      }

      "an invalid doc number is supplied" in {
        validator.validate(inputDataDocNumber.copy(docNumber = Some("a" * 13))) shouldBe List(InvalidDocNumberError)
      }

      "an invalid date from is supplied" in {
        validator.validate(inputDataDocNumber.copy(dateFrom = Some("invalid"))) shouldBe List(InvalidDateFromError)
      }

      "an invalid date to is supplied" in {
        validator.validate(inputDataDocNumber.copy(dateTo = Some("invalid"))) shouldBe List(InvalidDateToError)
      }

      "an invalid only open items is supplied" in {
        validator.validate(inputDataDocNumber.copy(onlyOpenItems = Some("invalid"))) shouldBe List(InvalidOnlyOpenItemsError)
      }

      "an invalid include locks is supplied" in {
        validator.validate(inputDataDocNumber.copy(includeLocks = Some("invalid"))) shouldBe List(InvalidIncludeLocksError)
      }

      "an invalid calculate accrued interest is supplied" in {
        validator.validate(inputDataDocNumber.copy(calculateAccruedInterest = Some("invalid"))) shouldBe List(InvalidCalculateAccruedInterestError)
      }

      "an invalid remove POA is supplied" in {
        validator.validate(inputDataDocNumber.copy(removePOA = Some("invalid"))) shouldBe List(InvalidRemovePaymentOnAccountError)
      }

      "an invalid customer payment information is supplied" in {
        validator.validate(inputDataDocNumber.copy(customerPaymentInformation = Some("invalid"))) shouldBe List(
          InvalidCustomerPaymentInformationError)
      }

      "an invalid include charge estimate is supplied" in {
        validator.validate(inputDataDocNumber.copy(includeChargeEstimate = Some("invalid"))) shouldBe List(InvalidIncludeChargeEstimateError)
      }

      "multiple invalid values are supplied" in {
        val input          = inputDataDocNumber.copy(dateFrom = Some("invalid"), removePOA = Some("invalid"))
        val expectedErrors = List(InvalidDateFromError, InvalidRemovePaymentOnAccountError)

        validator.validate(input) shouldBe expectedErrors
      }

      "date from is later than date to" in {
        val input          = inputDataDateRange.copy(dateFrom = Some("2022-12-01"), dateTo = Some("2022-11-01"))
        val expectedErrors = List(InvalidDateRangeError)
        validator.validate(input) shouldBe expectedErrors
      }
    }
  }

}
