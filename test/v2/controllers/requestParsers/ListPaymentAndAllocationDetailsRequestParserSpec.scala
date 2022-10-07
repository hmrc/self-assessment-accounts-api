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

import api.models.errors.{BadRequestError, ErrorWrapper, InvalidDateRangeError, InvalidDocNumberError, NinoFormatError}
import support.UnitSpec
import v2.mocks.validators.MockListPaymentAndAllocationDetailsValidator

class ListPaymentAndAllocationDetailsRequestParserSpec extends UnitSpec {
  val nino: String                    = ""
  val from: Option[String]            = Some("")
  val to: Option[String]              = Some("")
  val paymentLot: Option[String]      = Some("")
  val paymentLotItem: Option[String]  = Some("")

  val validRequestRawDataWithoutOptionals: ListPaymentAndAllocationDetailsRawData =
    ListPaymentAndAllocationDetailsRawData(nino, None, None, None, None)
  val validRequestWithoutOptionals: ListPaymentAndAllocationDetailsRequest =
    ListPaymentAndAllocationDetailsRequest(nino, None, None, None, None)

  val validRequestRawDataWithOptionals: ListPaymentAndAllocationDetailsRawData=
    ListPaymentAndAllocationDetailsRawData(nino, from, to, paymentLot, paymentLotItem)
  val validRequestWithOptionals: ListPaymentAndAllocationDetailsRequest =
    ListPaymentAndAllocationDetailsRequest(nino, from, to, paymentLot, paymentLotItem)


  implicit val correlationId: String = "X-123"

  trait Test extends MockListPaymentAndAllocationDetailsValidator {
    lazy val parser = new ListPaymentAndAllocationDetailsRequestParser(mockValidator)
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockListPaymentAndAllocationDetailsValidator.validate(validRequestRawDataWithoutOptionals).returns(Nil)

        parser.parseRequest(validRequestRawDataWithoutOptionals) shouldBe Right(validRequestWithoutOptionals)
      }

      "valid request data with date from and date to supplied" in new Test {
        MockListPaymentAndAllocationDetailsValidator.validate(validRequestRawDataWithOptionals).returns(Nil)

        parser.parseRequest(validRequestRawDataWithOptionals) shouldBe Right(validRequestWithOptionals)
      }

    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockListPaymentAndAllocationDetailsValidator
          .validate(validRequestRawDataWithoutOptionals)
          .returns(List(NinoFormatError))

        parser.parseRequest(validRequestRawDataWithoutOptionals) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occurs" in new Test {
        MockListPaymentAndAllocationDetailsValidator
          .validate(validRequestRawDataWithOptionals)
          .returns(List(NinoFormatError, InvalidDocNumberError))

        parser.parseRequest(validRequestRawDataWithOptionals) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, InvalidDateRangeError))))
      }
    }
  }

}
