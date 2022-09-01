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

package v1.controllers.requestParsers.validators

import api.models.errors.{NinoFormatError, TransactionIdFormatError}
import support.UnitSpec
import v1.models.request.retrieveTransactionDetails.RetrieveTransactionDetailsRawRequest

class RetrieveTransactionDetailsValidatorSpec extends UnitSpec {

  val validator = new RetrieveTransactionDetailsValidator()

  private val validNino            = "AA111111A"
  private val invalidNino          = "thisIsNotANino"
  private val validTransactionId   = "AA23GG4F34FG"
  private val invalidTransactionId = "thisStringIsTooLongToBeATransactionID"

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied with a transactionId" in {
        validator.validate(RetrieveTransactionDetailsRawRequest(validNino, validTransactionId)) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in {
        validator.validate(RetrieveTransactionDetailsRawRequest(invalidNino, validTransactionId)) shouldBe List(NinoFormatError)
      }
    }

    "return TransactionIdFormatError error" when {
      "an invalid transaction ID is supplied" in {
        validator.validate(RetrieveTransactionDetailsRawRequest(validNino, invalidTransactionId)) shouldBe List(TransactionIdFormatError)
      }
    }

    "return NinoFormatError and TransactionIdFormatError error" when {
      "an invalid nino and invalid transaction ID are supplied" in {
        validator.validate(RetrieveTransactionDetailsRawRequest(invalidNino, invalidTransactionId)) shouldBe List(
          NinoFormatError,
          TransactionIdFormatError)
      }
    }
  }

}
