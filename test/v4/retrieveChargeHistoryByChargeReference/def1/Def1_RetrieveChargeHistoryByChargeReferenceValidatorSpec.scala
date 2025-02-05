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

package v4.retrieveChargeHistoryByChargeReference.def1

import common.errors.ChargeReferenceFormatError
import common.models.ChargeReference
import shared.models.domain.Nino
import shared.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError}
import shared.utils.UnitSpec
import v3.retrieveChargeHistoryByChargeReference.def1.model.request.Def1_RetrieveChargeHistoryByChargeReferenceRequestData
import v3.retrieveChargeHistoryByChargeReference.model.request.RetrieveChargeHistoryByChargeReferenceRequestData

class Def1_RetrieveChargeHistoryByChargeReferenceValidatorSpec extends UnitSpec {
  private implicit val correlationId: String = "1234"

  private val validNino            = "AA123456A"
  private val validChargeReference = "XD000024425799"

  private val parsedNino            = Nino(validNino)
  private val parsedChargeReference = ChargeReference(validChargeReference)


  private def validator(nino: String, chargeReference: String) =
    new Def1_RetrieveChargeHistoryByChargeReferenceValidator(nino, chargeReference)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, RetrieveChargeHistoryByChargeReferenceRequestData] =
          validator(validNino, validChargeReference).validateAndWrapResult()

        result shouldBe Right(Def1_RetrieveChargeHistoryByChargeReferenceRequestData(parsedNino, parsedChargeReference))
      }

    }
    "should return a single error" when {
      "an invalid nino is supplied" in {
        val result: Either[ErrorWrapper, RetrieveChargeHistoryByChargeReferenceRequestData] =
          validator("invalidNino", validChargeReference).validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
      "an invalid chargeReference is supplied" in {
        val result: Either[ErrorWrapper, RetrieveChargeHistoryByChargeReferenceRequestData] =
          validator(validNino, "invalidChargeReference").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, ChargeReferenceFormatError))
      }
    }

    "return multiple errors" when {
      "multiple format errors are made" in {
        val result: Either[ErrorWrapper, RetrieveChargeHistoryByChargeReferenceRequestData] =
          validator("invalidNino", "invalidChargeReference").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(List(ChargeReferenceFormatError, NinoFormatError))))
      }
    }
  }

}
