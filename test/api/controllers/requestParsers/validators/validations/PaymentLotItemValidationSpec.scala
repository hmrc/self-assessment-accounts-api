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

import api.models.errors.{MissingPaymentLotItemError, PaymentLotItemFormatError}
import api.models.utils.JsonErrorValidators
import support.UnitSpec

class PaymentLotItemValidationSpec extends UnitSpec with JsonErrorValidators {

  val paymentLot = "AA123456aa12"
  val paymentLotItem = "000001"

  "validateFormat" should {
    "return no errors" when {
      "when a valid paymentLotItem is supplied" in {

        PaymentLotItemValidation.validateFormat(paymentLotItem) shouldBe List()
      }
      "when a valid optional paymentLotItem is supplied" in {
        val validPaymentLotItem = Some("000001")

        PaymentLotItemValidation.validateFormat(validPaymentLotItem) shouldBe List()
      }
      "when a no paymentLotItem value is supplied" in {
        PaymentLotItemValidation.validateFormat(None) shouldBe List()
      }
    }

    "return an error" when {
      "when an invalid paymentLotItem is supplied" in {
        val invalidPaymentLotItem = "AA12a!*"

        PaymentLotItemValidation.validateFormat(invalidPaymentLotItem) shouldBe List(PaymentLotItemFormatError)
      }
    }
  }

  "validateMissing" should {
    "return no errors" when {
      "when a paymentLot and paymentLotItem are supplied" in {
        PaymentLotItemValidation.validateMissing(Some(paymentLot), Some(paymentLotItem)) shouldBe List()
      }
      "when a paymentLotItem is supplied without a paymentLot" in {
        PaymentLotItemValidation.validateMissing(Some(paymentLotItem), None) shouldBe List()
      }
      "when neither a paymentLot or paymentLotItem are supplied" in {
        PaymentLotItemValidation.validateMissing(None, None) shouldBe List()
      }
    }

    "return an error" when {
      "when a paymentLot is supplied without a paymentLotItem" in {
        PaymentLotItemValidation.validateMissing(None, Some(paymentLot)) shouldBe List(MissingPaymentLotItemError)
      }
    }

  }
}
