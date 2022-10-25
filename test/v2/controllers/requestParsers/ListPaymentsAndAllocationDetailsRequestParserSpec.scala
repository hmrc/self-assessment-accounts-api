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

package v2.controllers.requestParsers

import api.models.domain.Nino
import api.models.errors.{BadRequestError, ErrorWrapper, RuleInvalidDateRangeError, NinoFormatError}
import support.UnitSpec
import v2.mocks.validators.MockListPaymentsAndAllocationDetailsValidator
import v2.models.request.listPaymentsAndAllocationDetails.{ListPaymentsAndAllocationDetailsRawData, ListPaymentsAndAllocationDetailsRequest}

class ListPaymentsAndAllocationDetailsRequestParserSpec extends UnitSpec {
  val nino: String                   = "AA999999A"
  val dateFrom: Option[String]       = Some("2021-01-01")
  val dateTo: Option[String]         = Some("2022-01-01")
  val paymentLot: Option[String]     = Some("081203010024")
  val paymentLotItem: Option[String] = Some("000001")

  val validRequestRawDataWithoutOptionals: ListPaymentsAndAllocationDetailsRawData =
    ListPaymentsAndAllocationDetailsRawData(nino, None, None, None, None)

  val validRequestWithoutOptionals: ListPaymentsAndAllocationDetailsRequest =
    ListPaymentsAndAllocationDetailsRequest(Nino(nino), None, None, None, None)

  val validRequestRawDataWithOptionals: ListPaymentsAndAllocationDetailsRawData =
    ListPaymentsAndAllocationDetailsRawData(nino, dateFrom, dateTo, paymentLot, paymentLotItem)

  val validRequestWithOptionals: ListPaymentsAndAllocationDetailsRequest =
    ListPaymentsAndAllocationDetailsRequest(Nino(nino), dateFrom, dateTo, paymentLot, paymentLotItem)

  implicit val correlationId: String = "X-123"

  trait Test extends MockListPaymentsAndAllocationDetailsValidator {
    lazy val parser = new ListPaymentsAndAllocationDetailsRequestParser(mockValidator)
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockListPaymentsAndAllocationDetailsValidator.validate(validRequestRawDataWithoutOptionals).returns(Nil)

        parser.parseRequest(validRequestRawDataWithoutOptionals) shouldBe Right(validRequestWithoutOptionals)
      }

      "valid request data with date from and date to supplied" in new Test {
        MockListPaymentsAndAllocationDetailsValidator.validate(validRequestRawDataWithOptionals).returns(Nil)

        parser.parseRequest(validRequestRawDataWithOptionals) shouldBe Right(validRequestWithOptionals)
      }

    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockListPaymentsAndAllocationDetailsValidator
          .validate(validRequestRawDataWithoutOptionals)
          .returns(List(NinoFormatError))

        parser.parseRequest(validRequestRawDataWithoutOptionals) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occurs" in new Test {
        MockListPaymentsAndAllocationDetailsValidator
          .validate(validRequestRawDataWithOptionals)
          .returns(List(NinoFormatError, RuleInvalidDateRangeError))

        parser.parseRequest(validRequestRawDataWithOptionals) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, RuleInvalidDateRangeError))))
      }
    }
  }

}
