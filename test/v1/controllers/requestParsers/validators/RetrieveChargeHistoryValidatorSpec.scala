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

package v1.controllers.requestParsers.validators

import support.UnitSpec
import v1.fixtures.RetrieveChargeHistoryFixture._
import api.models.errors.{NinoFormatError, TransactionIdFormatError}

class RetrieveChargeHistoryValidatorSpec extends UnitSpec {

  val validator = new RetrieveChargeHistoryValidator()

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(validRetrieveChargeHistoryRawRequest) shouldBe Nil
      }
    }

    "return NinoFormatError error" when {
      "an invalid nino is supplied" in {
        validator.validate(invalidRetrieveChargeHistoryRawRequestInvalidNino) shouldBe List(NinoFormatError)
      }
    }

    "return TransactionIdFormatError error" when {
      "an invalid charge ID is supplied" in {
        validator.validate(invalidRetrieveChargeHistoryRawRequestInvalidTransactionId) shouldBe List(TransactionIdFormatError)
      }
    }

    "return NinoFormatError and TransactionIdFormatError error" when {
      "an invalid nino and invalid charge ID are supplied" in {
        validator.validate(invalidRetrieveChargeHistoryRawRequestInvalidNinoAndTransactionId) shouldBe List(NinoFormatError, TransactionIdFormatError)
      }
    }
  }

}
