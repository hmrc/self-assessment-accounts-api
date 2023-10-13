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

import api.models.domain.{DateRange, Nino}
import api.models.errors._
import mocks.MockAppConfig
import support.UnitSpec
import v2.models.request.listPaymentsAndAllocationDetails.ListPaymentsAndAllocationDetailsRequestData

import java.time.LocalDate

class ListPaymentsAndAllocationDetailsValidatorFactorySpec extends UnitSpec with MockAppConfig {
  private implicit val correlationId: String = "1234"

  private val validNino           = "AA999999A"
  private val validFromDate       = "2021-01-01"
  private val validToDate         = "2022-01-01"
  private val validPaymentLot     = "081203010024"
  private val validPaymentLotItem = "000001"

  private val parsedNino = Nino(validNino)

  private val parsedFromDate       = LocalDate.parse(validFromDate)
  private val parsedParsedToDate   = LocalDate.parse(validToDate)
  private val parsedFromAndToDates = DateRange(parsedFromDate, parsedParsedToDate)

  private val parsedPaymentLot     = validPaymentLot
  private val parsedPaymentLotItem = validPaymentLotItem

  private val parsedRequest =
    ListPaymentsAndAllocationDetailsRequestData(parsedNino, Some(parsedFromAndToDates), Some(parsedPaymentLot), Some(parsedPaymentLotItem))

  private val parsedRequestWithoutOptionals = ListPaymentsAndAllocationDetailsRequestData(parsedNino, None, None, None)

  private val validatorFactory = new ListPaymentsAndAllocationDetailsValidatorFactory

  private def validator(nino: String, fromDate: Option[String], toDate: Option[String], paymentLot: Option[String], paymentLotItem: Option[String]) =
    validatorFactory.validator(nino, fromDate, toDate, paymentLot, paymentLotItem)

  "running the validation" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result =
          validator(validNino, Some(validFromDate), Some(validToDate), Some(validPaymentLot), Some(validPaymentLotItem)).validateAndWrapResult()

        result shouldBe Right(parsedRequest)
      }

      "passed a valid request with no optionals supplied" in {
        val result = validator(validNino, None, None, None, None).validateAndWrapResult()

        result shouldBe Right(parsedRequestWithoutOptionals)
      }
    }
  }

  "return an error" when {
    "passed an invalid nino" in {
      val result = validator("invalid", None, None, None, None).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
    }

    "passed an invalid fromDate" in {
      val result = validator(validNino, Some("invalid"), Some(validToDate), None, None).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, FromDateFormatError))
    }

    "passed an invalid toDate" in {
      val result = validator(validNino, Some(validFromDate), Some("invalid"), None, None).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, ToDateFormatError))
    }

    "passed a toDate before the fromDate" in {
      val result = validator(validNino, Some(validToDate), Some(validFromDate), None, None).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, RangeToDateBeforeFromDateError))
    }

    "passed a fromDate and no toDate" in {
      val result = validator(validNino, Some(validFromDate), None, None, None).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, RuleMissingToDateError))
    }

    "passed a toDate and no fromDate" in {
      val result = validator(validNino, None, Some(validToDate), None, None).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, MissingFromDateError))
    }

    "passed an invalid paymentLot" in {
      val result = validator(validNino, None, None, Some("abc123!@£"), Some(validPaymentLotItem)).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, PaymentLotFormatError))
    }

    "passed an invalid paymentLotItem" in {
      val result = validator(validNino, None, None, Some(validPaymentLot), Some("invalid")).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, PaymentLotItemFormatError))
    }

    "passed a paymentLot and no paymentLotItem" in {
      val result = validator(validNino, None, None, Some(validPaymentLot), None).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, MissingPaymentLotItemError))
    }

    "passed a paymentLotItem and no paymentLot" in {
      val result = validator(validNino, None, None, None, Some(validPaymentLotItem)).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, MissingPaymentLotError))
    }

    "a from date that preceeds the minimum is supplied" in {
      val result = validator(validNino, Some("1800-01-01"), Some(validToDate), None, None).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, FromDateFormatError))
    }

    "a from date that procedes the maximum is supplied" in {
      val result = validator(validNino, Some(validFromDate), Some("2100-01-21"), None, None).validateAndWrapResult()

      result shouldBe Left(ErrorWrapper(correlationId, ToDateFormatError))
    }
  }

  "return multiple errors" when {
    "passed multiple invalid fields" in {
      val result = validator(validNino, Some("invalid"), Some("invalid"), None, None).validateAndWrapResult()

      result shouldBe Left(
        ErrorWrapper(
          correlationId,
          BadRequestError,
          Some(List(FromDateFormatError, ToDateFormatError))
        )
      )
    }
  }

}
