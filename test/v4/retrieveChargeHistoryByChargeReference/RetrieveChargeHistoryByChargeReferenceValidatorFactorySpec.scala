/*
 * Copyright 2026 HM Revenue & Customs
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

package v4.retrieveChargeHistoryByChargeReference

import api.models.domain.Nino
import api.utils.UnitSpec
import common.models.ChargeReference
import v4.retrieveChargeHistoryByChargeReference.model.request.RetrieveChargeHistoryByChargeReferenceRequestData

class RetrieveChargeHistoryByChargeReferenceValidatorFactorySpec extends UnitSpec {

  private implicit val correlationId: String = "1234"

  private val validNino            = "AA123456A"
  private val validChargeReference = "XD000024425799"

  private val expectedRequestData =
    RetrieveChargeHistoryByChargeReferenceRequestData(Nino(validNino), ChargeReference(validChargeReference))

  private val validatorFactory = new RetrieveChargeHistoryByChargeReferenceValidatorFactory

  "creating a validator" should {
    "return the parsed domain object" when {
      "given a valid request" in {
        val result = validatorFactory.validator(validNino, validChargeReference)
        result.validateAndWrapResult() shouldBe Right(expectedRequestData)
      }
    }
  }

}
