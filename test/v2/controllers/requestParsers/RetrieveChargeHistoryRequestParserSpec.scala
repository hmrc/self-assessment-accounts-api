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

package v2.controllers.requestParsers

import support.UnitSpec
import v2.mocks.validators.MockRetrieveChargeHistoryValidator
import api.models.domain.Nino
import api.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, TransactionIdFormatError}
import v2.models.request.retrieveChargeHistory.{RetrieveChargeHistoryRawData, RetrieveChargeHistoryRequest}

class RetrieveChargeHistoryRequestParserSpec extends UnitSpec {

  val validNino: String              = "AA123456B"
  val validTransactionId: String     = "717f3agW678f"
  implicit val correlationId: String = "X-123"

  val inputData: RetrieveChargeHistoryRawData =
    RetrieveChargeHistoryRawData(validNino, validTransactionId)

  trait Test extends MockRetrieveChargeHistoryValidator {
    lazy val parser = new RetrieveChargeHistoryRequestParser(mockValidator)
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockRetrieveChargeHistoryValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe Right(RetrieveChargeHistoryRequest(Nino(validNino), validTransactionId))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockRetrieveChargeHistoryValidator
          .validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockRetrieveChargeHistoryValidator
          .validate(inputData)
          .returns(List(NinoFormatError, TransactionIdFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TransactionIdFormatError))))
      }
    }
  }

}
