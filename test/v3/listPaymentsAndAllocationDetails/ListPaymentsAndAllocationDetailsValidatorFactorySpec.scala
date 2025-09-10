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

package v3.listPaymentsAndAllocationDetails

import shared.models.utils.JsonErrorValidators
import shared.utils.UnitSpec
import v3.listPaymentsAndAllocationDetails.def1.Def1_ListPaymentsAndAllocationDetailsValidator

class ListPaymentsAndAllocationDetailsValidatorFactorySpec extends UnitSpec with JsonErrorValidators {

  private val validNino      = "AA123456A"
  private val validFromDate  = Some("2022-08-15")
  private val validToDate    = Some("2022-09-15")
  private val paymentLot     = Some("paymentLot")
  private val paymentLotItem = Some("paymentLotItem")

  private val validatorFactory = new ListPaymentsAndAllocationDetailsValidatorFactory

  "running a validation" should {
    "return the parsed domain object" when {
      "given a valid request" in {
        val result = validatorFactory.validator(validNino, validFromDate, validToDate, paymentLot, paymentLotItem)
        result shouldBe a[Def1_ListPaymentsAndAllocationDetailsValidator]

      }
    }

  }

}
