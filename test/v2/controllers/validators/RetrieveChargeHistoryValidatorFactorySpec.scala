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

package v2.controllers.validators

import api.models.domain.{ChargeReference, Nino, TransactionId}
import api.models.errors._
import support.UnitSpec
import v2.models.request.retrieveChargeHistory.RetrieveChargeHistoryRequestData

class RetrieveChargeHistoryValidatorFactorySpec extends UnitSpec {
  private implicit val correlationId: String = "1234"

  private val validNino            = "AA123456A"
  private val validTransactionId   = "717f3agW678f"
  private val validChargeReference = "testCharge23"
  private val chargeReference      = ChargeReference(validChargeReference)

  private val parsedNino          = Nino(validNino)
  private val parsedTransactionId = TransactionId(validTransactionId)

  private val validatorFactory = new RetrieveChargeHistoryValidatorFactory()

  private def validator(nino: String, transactionId: String, chargeReference: Option[String]) =
    validatorFactory.validator(nino, transactionId, chargeReference)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, RetrieveChargeHistoryRequestData] =
          validator(validNino, validTransactionId, Some(validChargeReference)).validateAndWrapResult()

        result shouldBe Right(RetrieveChargeHistoryRequestData(parsedNino, parsedTransactionId, Some(chargeReference)))
      }

      "passed a valid request with no charge reference" in {
        val result: Either[ErrorWrapper, RetrieveChargeHistoryRequestData] =
          validator(validNino, validTransactionId, None).validateAndWrapResult()

        result shouldBe Right(RetrieveChargeHistoryRequestData(parsedNino, parsedTransactionId, None))
      }
    }
    "should return a single error" when {
      "an invalid nino is supplied" in {
        val result: Either[ErrorWrapper, RetrieveChargeHistoryRequestData] =
          validator("invalidNino", validTransactionId, Some(validChargeReference)).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
      "an invalid transactionId is supplied" in {
        val result: Either[ErrorWrapper, RetrieveChargeHistoryRequestData] =
          validator(validNino, "abcdefghijklmn", Some(validChargeReference)).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, TransactionIdFormatError))
      }

      "an invalid charge reference is supplied" in {
        val result: Either[ErrorWrapper, RetrieveChargeHistoryRequestData] =
          validator(validNino, validTransactionId, Some("not_valid_c_r")).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ChargeReferenceFormatError))
      }
    }

    "return multiple errors" when {
      "multiple format errors are made" in {
        val result: Either[ErrorWrapper, RetrieveChargeHistoryRequestData] =
          validator("invalidNino", "abcdefghijklmn", Some("f8324rg231489g21+_2")).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(ChargeReferenceFormatError, NinoFormatError, TransactionIdFormatError))))
      }
    }
  }

}
