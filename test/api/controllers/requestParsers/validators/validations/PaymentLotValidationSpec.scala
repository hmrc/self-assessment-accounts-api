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

import api.models.errors.PaymentLotFormatError
import api.models.utils.JsonErrorValidators
import support.UnitSpec

class PaymentLotValidationSpec extends UnitSpec with JsonErrorValidators {

  "validate" should {
    "return no errors" when {
      "when a valid paymentLot is supplied" in {
        val validPaymentLot = "AA123456aa12"

        PaymentLotValidation.validate(validPaymentLot) shouldBe List()
      }
      "when a valid optional paymentLot is supplied" in {
        val validPaymentLot = Some("AA123456aa12")

        PaymentLotValidation.validate(validPaymentLot) shouldBe List()
      }
      "when a no paymentLot value is supplied" in {
        PaymentLotValidation.validate(None) shouldBe List()
      }
    }

    "return an error" when {
      "when an invalid paymentLot is supplied" in {
        val invalidPaymentLot = "AA123456aa12!*"

        PaymentLotValidation.validate(invalidPaymentLot) shouldBe List(PaymentLotFormatError)
      }
    }
  }

}
