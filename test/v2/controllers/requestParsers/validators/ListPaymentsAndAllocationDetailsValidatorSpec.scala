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
import api.models.errors.{DateFromFormatError, DateToFormatError, MissingFromDateError, MissingToDateError, NinoFormatError, PaymentLotFormatError, PaymentLotItemFormatError, RuleDateRangeInvalidError, RuleDateToBeforeDateFromError}
import mocks.MockAppConfig
import support.UnitSpec
import v2.models.request.listPaymentsAndAllocationDetails.{ListPaymentsAndAllocationDetailsRawData, ListPaymentsAndAllocationDetailsRequest}

class ListPaymentsAndAllocationDetailsValidatorSpec extends UnitSpec with MockAppConfig {
  val nino: String                    = "AA999999A"
  val dateFrom: Option[String]        = Some("2021-01-01")
  val dateTo: Option[String]          = Some("2022-01-01")
  val paymentLot: Option[String]      = Some("081203010024")
  val paymentLotItem: Option[String]  = Some("000001")

  val validRequestRawDataWithoutOptionals: ListPaymentsAndAllocationDetailsRawData =
    ListPaymentsAndAllocationDetailsRawData(nino, None, None, None, None)
  val validRequestWithoutOptionals: ListPaymentsAndAllocationDetailsRequest =
    ListPaymentsAndAllocationDetailsRequest(Nino(nino), None, None, None, None)

  val validRequestRawDataWithOptionals: ListPaymentsAndAllocationDetailsRawData=
    ListPaymentsAndAllocationDetailsRawData(nino, dateFrom, dateTo, paymentLot, paymentLotItem)
  val validRequestWithOptionals: ListPaymentsAndAllocationDetailsRequest =
    ListPaymentsAndAllocationDetailsRequest(Nino(nino), dateFrom, dateTo, paymentLot, paymentLotItem)


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

      "an invalid dateFrom is supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(dateFrom = Some("abc"))) shouldBe List(DateFromFormatError)
      }

      "an invalid dateTo is supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(dateTo = Some("abc"))) shouldBe List(DateToFormatError)
      }

      "an invalid date range is supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(dateTo = Some("2023-01-01"))) shouldBe List(RuleDateRangeInvalidError)
      }

      "the dateFrom supplied is after the dateTo supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(dateTo = dateFrom, dateFrom = dateTo)) shouldBe List(RuleDateToBeforeDateFromError)
      }

      "a from date is supplied and a to date is not supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(dateTo = None)) shouldBe List(MissingToDateError)
      }

      "a to date is supplied and a from date is not supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(dateFrom = None)) shouldBe List(MissingFromDateError)
      }

      "an invalid paymentLot to is supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(paymentLot = Some("abc123!@£"))) shouldBe List(PaymentLotFormatError)
      }

      "an invalid paymentLotItem is supplied" in {
        validator.validate(validRequestRawDataWithOptionals.copy(paymentLotItem = Some("abc123!@£"))) shouldBe List(PaymentLotItemFormatError)
      }

      "multiple invalid values are supplied" in {
        val input = validRequestRawDataWithOptionals.copy(nino = "invalid", dateFrom = Some("invalid"), dateTo = Some("invalid"), paymentLot = Some("invalid!"), paymentLotItem = Some("invalid!"))
        val expectedErrors = List(NinoFormatError, DateFromFormatError, DateToFormatError, PaymentLotFormatError, PaymentLotItemFormatError)

        validator.validate(input) shouldBe expectedErrors
      }
    }
  }

}
