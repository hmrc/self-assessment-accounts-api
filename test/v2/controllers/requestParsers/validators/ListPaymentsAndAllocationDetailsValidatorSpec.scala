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

import api.models.domain.Nino
import api.models.errors._
import mocks.MockAppConfig
import support.UnitSpec
import v2.models.request.listPaymentsAndAllocationDetails.{ListPaymentsAndAllocationDetailsRawData, ListPaymentsAndAllocationDetailsRequest}

class ListPaymentsAndAllocationDetailsValidatorSpec extends UnitSpec with MockAppConfig {
  val nino: String                   = "AA999999A"
  val fromDate: Option[String]       = Some("2021-01-01")
  val toDate: Option[String]         = Some("2022-01-01")
  val paymentLot: Option[String]     = Some("081203010024")
  val paymentLotItem: Option[String] = Some("000001")

  val validRequestRawDataWithoutOptionals: ListPaymentsAndAllocationDetailsRawData =
    ListPaymentsAndAllocationDetailsRawData(nino, None, None, None, None)

  val validRequestWithoutOptionals: ListPaymentsAndAllocationDetailsRequest =
    ListPaymentsAndAllocationDetailsRequest(Nino(nino), None, None, None, None)

  val validRequestRawDataWithOptionals: ListPaymentsAndAllocationDetailsRawData =
    ListPaymentsAndAllocationDetailsRawData(nino, fromDate, toDate, paymentLot, paymentLotItem)

  val validRequestWithOptionals: ListPaymentsAndAllocationDetailsRequest =
    ListPaymentsAndAllocationDetailsRequest(Nino(nino), fromDate, toDate, paymentLot, paymentLotItem)

  private val validator = new ListPaymentsAndAllocationDetailsValidator(mockAppConfig)

  "running a validation" should {
    "return no errors" when {
      "a valid request without any optionals is supplied" in {
        validator.validate(validRequestRawDataWithoutOptionals) shouldBe Nil
      }

      "a valid request with optionals is supplied" in {
        validator.validate(validRequestRawDataWithOptionals) shouldBe Nil
      }
    }

    "return a parameter error" when {
      "an invalid nino is supplied" in {
        validator.validate(validRequestRawDataWithoutOptionals.copy(nino = "nino")) shouldBe List(NinoFormatError)
      }

      "an invalid fromDate is supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(fromDate = Some("abc"))) shouldBe List(V2_FromDateFormatError)
      }

      "an invalid toDate is supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(toDate = Some("abc"))) shouldBe List(V2_ToDateFormatError)
      }

      "the fromDate supplied is after the toDate supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(toDate = fromDate, fromDate = toDate)) shouldBe List(V2_RangeToDateBeforeFromDateError)
      }

      "a from date is supplied and a to date is not supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(toDate = None)) shouldBe List(V2_MissingToDateError)
      }

      "a to date is supplied and a from date is not supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(fromDate = None)) shouldBe List(V2_MissingFromDateError)
      }

      "an invalid paymentLot to is supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(paymentLot = Some("abc123!@£"))) shouldBe List(PaymentLotFormatError)
      }

      "an invalid paymentLotItem is supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(paymentLotItem = Some("abc123!@£"))) shouldBe List(PaymentLotItemFormatError)
      }

      "a paymentLotItem is supplied but a paymentLot is not supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(paymentLot = None)) shouldBe List(MissingPaymentLotError)
      }

      "multiple invalid values are supplied" in {
        val input = validRequestRawDataWithOptionals.copy(
          nino = "invalid",
          fromDate = Some("invalid"),
          toDate = Some("invalid"),
          paymentLot = Some("invalid!"),
          paymentLotItem = Some("invalid!"))
        val expectedErrors = List(NinoFormatError, V2_FromDateFormatError, V2_ToDateFormatError, PaymentLotFormatError, PaymentLotItemFormatError)

        validator.validate(input) shouldBe expectedErrors
      }
    }
  }

}
