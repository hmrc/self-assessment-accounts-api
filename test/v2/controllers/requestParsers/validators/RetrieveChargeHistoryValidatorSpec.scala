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

import mocks.MockAppConfig
import support.UnitSpec
import api.models.errors._
import v2.models.request.retrieveChargeHistory.RetrieveChargeHistoryRawData

class RetrieveChargeHistoryValidatorSpec extends UnitSpec with MockAppConfig {

  private val validNino          = "AA123456A"
  private val validTransactionId = "717f3agW678f"

  private val validator = new RetrieveChargeHistoryValidator(mockAppConfig)

  "running a validation" should {
    "return no errors" when {
      "a valid request is supplied" in {
        validator.validate(RetrieveChargeHistoryRawData(validNino, validTransactionId)) shouldBe Nil
      }
    }
    "return a path parameter format error" when {
      "an invalid nino is supplied" in {
        validator.validate(RetrieveChargeHistoryRawData("Nino", validTransactionId)) shouldBe List(NinoFormatError)
      }
      "an invalid transactionId is supplied" in {
        validator.validate(RetrieveChargeHistoryRawData(validNino, "abcdefghijklmn")) shouldBe List(TransactionIdFormatError)
      }
      "multiple format errors are made" in {
        validator.validate(RetrieveChargeHistoryRawData("Nino", "abcdefghijklmn")) shouldBe
          List(NinoFormatError, TransactionIdFormatError)
      }
    }
  }

}
