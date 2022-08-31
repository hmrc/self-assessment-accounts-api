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

import support.UnitSpec
import v2.mocks.validators.MockRetrieveSelfAssessmentChargeHistoryValidator
import v2.models.domain.Nino
import v2.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, TransactionIdFormatError}
import v2.models.request.retrieveSelfAssessmentChargeHistory.{RetrieveSelfAssessmentChargeHistoryRawData, RetrieveSelfAssessmentChargeHistoryRequest}

class RetrieveSelfAssessmentChargeHistoryRequestParserSpec extends UnitSpec {

  val validNino: String                    = "AA123456B"
  val validTransactionId: String           = "717f3agW678f"
  implicit val correlationId: String       = "X-123"

  val inputData: RetrieveSelfAssessmentChargeHistoryRawData =
    RetrieveSelfAssessmentChargeHistoryRawData(validNino, validTransactionId)

  trait Test extends MockRetrieveSelfAssessmentChargeHistoryValidator {
    lazy val parser = new RetrieveSelfAssessmentChargeHistoryRequestParser(mockValidator)
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {
        MockRetrieveSelfAssessmentChargeHistoryValidator.validate(inputData).returns(Nil)

        parser.parseRequest(inputData) shouldBe Right(RetrieveSelfAssessmentChargeHistoryRequest(Nino(validNino), validTransactionId))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockRetrieveSelfAssessmentChargeHistoryValidator.validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockRetrieveSelfAssessmentChargeHistoryValidator.validate(inputData)
          .returns(List(NinoFormatError, TransactionIdFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TransactionIdFormatError))))
      }
    }
  }
}
