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

import api.models.errors.{MissingPaymentLotError, PaymentLotFormatError}
import api.models.utils.JsonErrorValidators
import support.UnitSpec

class PaymentLotValidationSpec extends UnitSpec with JsonErrorValidators {

  val paymentLot     = "AA123456aa12"
  val paymentLotItem = "000001"

  "validateFormat" should {
    "return no errors" when {
      "when a valid paymentLot is supplied" in {
        PaymentLotValidation.validateFormat(paymentLot) shouldBe List()
      }
      "when a valid optional paymentLot is supplied" in {
        PaymentLotValidation.validateFormat(Some(paymentLot)) shouldBe List()
      }
      "when a no paymentLot value is supplied" in {
        PaymentLotValidation.validateFormat(None) shouldBe List()
      }
    }

    "return an error" when {
      "when an invalid paymentLot is supplied" in {
        val invalidPaymentLot = "AA123456aa12!*"

        PaymentLotValidation.validateFormat(invalidPaymentLot) shouldBe List(PaymentLotFormatError)
      }
    }
  }

  "validateMissing" should {
    "return no errors" when {
      "when a paymentLot and paymentLotItem are supplied" in {
        PaymentLotValidation.validateMissing(Some(paymentLot), Some(paymentLotItem)) shouldBe List()
      }
      "when a paymentLot is supplied without a paymentLotItem" in {
        PaymentLotValidation.validateMissing(Some(paymentLot), None) shouldBe List()
      }
      "when neither a paymentLot or paymentLotItem are supplied" in {
        PaymentLotValidation.validateMissing(None, None) shouldBe List()
      }
    }

    "return an error" when {
      "when a paymentLotItem is supplied without a paymentLot" in {
        PaymentLotValidation.validateMissing(None, Some(paymentLotItem)) shouldBe List(MissingPaymentLotError)
      }
    }
  }

}
