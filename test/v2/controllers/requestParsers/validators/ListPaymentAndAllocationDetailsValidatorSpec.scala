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

import api.models.errors.{
  EndDateFormatError,
  InvalidDateFromError,
  InvalidDateToError,
  InvalidDocNumberError,
  InvalidOnlyOpenItemsError,
  NinoFormatError,
  PaymentLotFormatError,
  PaymentLotItemFormatError,
  StartDateFormatError
}
import mocks.MockAppConfig
import support.UnitSpec

class ListPaymentAndAllocationDetailsValidatorSpec extends UnitSpec with MockAppConfig {
  val nino: String                    = ""
  val fromDate: Option[String]            = Some("")
  val toDate: Option[String]              = Some("")
  val paymentLot: Option[String]      = Some("")
  val paymentLotItem: Option[String]  = Some("")

  val validRequestRawDataWithoutOptionals: ListPaymentAndAllocationDetailsRawData =
    ListPaymentAndAllocationDetailsRawData(nino, None, None, None, None)
  val validRequestWithoutOptionals: ListPaymentAndAllocationDetailsRequest =
    ListPaymentAndAllocationDetailsRequest(nino, None, None, None, None)

  val validRequestRawDataWithOptionals: ListPaymentAndAllocationDetailsRawData=
    ListPaymentAndAllocationDetailsRawData(nino, fromDate, toDate, paymentLot, paymentLotItem)
  val validRequestWithOptionals: ListPaymentAndAllocationDetailsRequest =
    ListPaymentAndAllocationDetailsRequest(nino, fromDate, toDate, paymentLot, paymentLotItem)


  private val validator = new ListPaymentAndAllocationDetailsValidator(mockAppConfig)

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

      "an invalid from date is supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(from = Some("abc"))) shouldBe List(InvalidDocNumberError)
      }

      "an invalid to date is supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(to = Some("abc"))) shouldBe List(InvalidDateFromError)
      }

      "an invalid to date range is supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(to = Some("bad date"))) shouldBe List(InvalidDateFromError)
      }

      "the from date supplied is after the to date supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(to = fromDate, from = toDate)) shouldBe List(InvalidDateFromError)
      }

      "a from date is supplied and a to date is not supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(to = None)) shouldBe List(InvalidDateFromError)
      }

      "a to date is supplied and a from date is not supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(from = None)) shouldBe List(InvalidDateFromError)
      }

      "an invalid paymentLot to is supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(paymentLot = Some("abc123!@£"))) shouldBe List(InvalidDateToError)
      }

      "an invalid paymentLotItem is supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(paymentLotItem = Some("abc123!@£"))) shouldBe List(InvalidOnlyOpenItemsError)
      }

      "multiple invalid values are supplied" in {
        val input          = validRequestRawDataWithOptionals.copy(nino = "invalid" from = Some("invalid"), to = Some("invalid"), paymentLot = "invalid!", paymentLotItem = "invalid!" )
        val expectedErrors = List(NinoFormatError, StartDateFormatError, EndDateFormatError, PaymentLotFormatError, PaymentLotItemFormatError)

        validator.validate(input) shouldBe expectedErrors
      }
    }
  }

}
