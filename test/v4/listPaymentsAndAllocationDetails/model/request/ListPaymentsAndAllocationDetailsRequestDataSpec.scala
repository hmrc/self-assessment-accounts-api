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

package v4.listPaymentsAndAllocationDetails.model.request

import api.models.domain.{DateRange, Nino}
import api.utils.UnitSpec

import java.time.LocalDate

class ListPaymentsAndAllocationDetailsRequestDataSpec extends UnitSpec {

  "ListPaymentsAndAllocationDetailsRequestData" should {
    "hold the NINO, date range and payment details" in {
      val nino           = Nino("AA999999A")
      val fromAndToDates = DateRange(LocalDate.parse("2021-01-01"), LocalDate.parse("2022-01-01"))
      val paymentLot     = "081203010024"
      val paymentLotItem = "000001"

      val requestData =
        ListPaymentsAndAllocationDetailsRequestData(nino, Some(fromAndToDates), Some(paymentLot), Some(paymentLotItem))

      requestData.nino shouldBe nino
      requestData.fromAndToDates shouldBe Some(fromAndToDates)
      requestData.paymentLot shouldBe Some(paymentLot)
      requestData.paymentLotItem shouldBe Some(paymentLotItem)
    }

    "support a request without optional fields" in {
      val nino        = Nino("AA999999A")
      val requestData = ListPaymentsAndAllocationDetailsRequestData(nino, None, None, None)

      requestData.nino shouldBe nino
      requestData.fromAndToDates shouldBe None
      requestData.paymentLot shouldBe None
      requestData.paymentLotItem shouldBe None
    }
  }

}
