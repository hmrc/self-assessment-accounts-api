/*
 * Copyright 2020 HM Revenue & Customs
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
import v1.models.request.retrieveAllocations.RetrieveAllocationsRawRequest

class RetrieveAllocationsValidatorSpec extends UnitSpec {

  private val validNino = "AA123456A"
  private val validPaymentId = "AF234F-12DFA"


  val validator = new RetrieveAllocationsValidator()

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(RetrieveAllocationsRawRequest(validNino, validPaymentId)) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in {
        validator.validate(RetrieveAllocationsRawRequest("A12344A", validPaymentId)) shouldBe
          List(NinoFormatError)
      }
    }

    "return PaymentIdFormatError error" when {
      "an invalid paymentId is supplied" in {
        validator.validate(RetrieveAllocationsRawRequest(validNino, "not a valid payment ID")) shouldBe
          List(PaymentIdFormatError)
      }
    }

    "return multiple errors" when {
      "request supplied has multiple errors" in {
        validator.validate(RetrieveAllocationsRawRequest("A12344A", "not a valid payment ID")) shouldBe
          List(NinoFormatError, PaymentIdFormatError)
      }
    }
  }
}
