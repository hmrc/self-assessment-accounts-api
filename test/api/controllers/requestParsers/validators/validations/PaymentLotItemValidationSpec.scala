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

import api.models.errors.PaymentLotItemFormatError
import api.models.utils.JsonErrorValidators
import support.UnitSpec

class PaymentLotItemValidationSpec extends UnitSpec with JsonErrorValidators {

  "validateFormat" should {
    "return no errors" when {
      "when a valid paymentLotItem is supplied" in {
        val validPaymentLotItem = "000001"

        PaymentLotItemValidation.validateFormat(validPaymentLotItem) shouldBe List()
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

}
