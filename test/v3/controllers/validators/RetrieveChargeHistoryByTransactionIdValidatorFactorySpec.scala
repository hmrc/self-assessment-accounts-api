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

package v3.controllers.validators

import api.models.domain.{Nino, TransactionId}
import api.models.errors._
import support.UnitSpec
import v3.models.request.retrieveChargeHistory.RetrieveChargeHistoryByTransactionIdRequestData

class RetrieveChargeHistoryByTransactionIdValidatorFactorySpec extends UnitSpec {
  private implicit val correlationId: String = "1234"

  private val validNino = "AA123456A"
  private val validTransactionId = "717f3agW678f"

  private val parsedNino = Nino(validNino)
  private val parsedTransactionId = TransactionId(validTransactionId)

  private val validatorFactory = new RetrieveChargeHistoryByTransactionIdValidatorFactory()

  private def validator(nino: String, transactionId: String) =
    validatorFactory.validator(nino, transactionId)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, RetrieveChargeHistoryByTransactionIdRequestData] =
          validator(validNino, validTransactionId).validateAndWrapResult()

        result shouldBe Right(RetrieveChargeHistoryByTransactionIdRequestData(parsedNino, parsedTransactionId))
      }

    }
    "should return a single error" when {
      "an invalid nino is supplied" in {
        val result: Either[ErrorWrapper, RetrieveChargeHistoryByTransactionIdRequestData] =
          validator("invalidNino", validTransactionId).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
      "an invalid transactionId is supplied" in {
        val result: Either[ErrorWrapper, RetrieveChargeHistoryByTransactionIdRequestData] =
          validator(validNino, "invalidTransactionId").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TransactionIdFormatError))
      }
    }

    "return multiple errors" when {
      "multiple format errors are made" in {
        val result: Either[ErrorWrapper, RetrieveChargeHistoryByTransactionIdRequestData] =
          validator("invalidNino", "invalidTransactionId").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, TransactionIdFormatError))))
      }
    }
  }

}
