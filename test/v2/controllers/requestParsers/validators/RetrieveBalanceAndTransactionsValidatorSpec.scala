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

import api.models.errors._
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
        validator.validate(inputDataDocNumber.copy(docNumber = Some("a" * 13))) shouldBe List(DocNumberFormatError)
      }

      "an invalid only open items is supplied" in {
        validator.validate(inputDataDocNumber.copy(onlyOpenItems = Some("invalid"))) shouldBe List(OnlyOpenItemsFormatError)
      }

      "an invalid include locks is supplied" in {
        validator.validate(inputDataDocNumber.copy(includeLocks = Some("invalid"))) shouldBe List(IncludeLocksFormatError)
      }

      "an invalid calculate accrued interest is supplied" in {
        validator.validate(inputDataDocNumber.copy(calculateAccruedInterest = Some("invalid"))) shouldBe List(CalculateAccruedInterestFormatError)
      }

      "an invalid customer payment information is supplied" in {
        validator.validate(inputDataDocNumber.copy(customerPaymentInformation = Some("invalid"))) shouldBe
          List(CustomerPaymentInformationFormatError)
      }

      "an invalid from date is supplied" in {
        validator.validate(inputDataDocNumber.copy(fromDate = Some("invalid"))) shouldBe List(V2_FromDateFormatError)
      }
      "an invalid to date is supplied" in {
        validator.validate(inputDataDocNumber.copy(toDate = Some("invalid"))) shouldBe List(V2_ToDateFormatError)
      }

      "an invalid remove POA is supplied" in {
        validator.validate(inputDataDocNumber.copy(removePOA = Some("invalid"))) shouldBe List(RemovePaymentOnAccountFormatError)
      }

      "an invalid include charge estimate is supplied" in {
        validator.validate(inputDataDocNumber.copy(includeEstimatedCharges = Some("invalid"))) shouldBe List(IncludeEstimatedChargesFormatError)
      }

      "to date is supplied but from date is not" in {
        validator.validate(inputDataDateRange.copy(fromDate = None, toDate = Some("2022-11-01"))) shouldBe List(V2_MissingFromDateError)
      }

      "from date is supplied but to date is not" in {
        validator.validate(inputDataDateRange.copy(fromDate = Some("2022-12-01"), toDate = None)) shouldBe List(V2_MissingToDateError)
      }

      "from date is later than to date" in {
        validator.validate(inputDataDateRange.copy(fromDate = Some("2022-12-01"), toDate = Some("2022-11-01"))) shouldBe
          List(V2_RangeToDateBeforeFromDateError)
      }

      "multiple invalid values are supplied" in {
        validator.validate(inputDataDocNumber.copy(fromDate = Some("invalid"), removePOA = Some("invalid"))) shouldBe
          List(V2_FromDateFormatError, RemovePaymentOnAccountFormatError)
      }
    }
  }

}
