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

package v1.controllers.requestParsers

import support.UnitSpec
import api.models.domain.Nino
import v1.fixtures.RetrieveChargeHistoryFixture._
import v1.mocks.validators.MockRetrieveChargeHistoryValidator
import api.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, TransactionIdFormatError}
import v1.models.request.retrieveChargeHistory.RetrieveChargeHistoryParsedRequest

class RetrieveChargeHistoryRequestParserSpec extends UnitSpec {

  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  trait Test extends MockRetrieveChargeHistoryValidator {
    lazy val parser = new RetrieveChargeHistoryRequestParser(mockValidator)
  }

  "parsing a retrieve Balance request" should {
    "return a retrieve Balance request" when {
      "valid data is provided" in new Test {

        MockRetrieveChargeHistoryValidator
          .validate(validRetrieveChargeHistoryRawRequest)
          .returns(Nil)

        parser.parseRequest(validRetrieveChargeHistoryRawRequest) shouldBe
          Right(RetrieveChargeHistoryParsedRequest(Nino(validNino), validTransactionId))
      }
    }
  }

  "return an error" when {
    "an invalid nino is provided" in new Test {
      MockRetrieveChargeHistoryValidator
        .validate(invalidRetrieveChargeHistoryRawRequestInvalidNino)
        .returns(List(NinoFormatError))

      parser.parseRequest(invalidRetrieveChargeHistoryRawRequestInvalidNino) shouldBe
        Left(ErrorWrapper(correlationId, NinoFormatError, None))
    }
    "an invalid charge id is provided" in new Test {
      MockRetrieveChargeHistoryValidator
        .validate(invalidRetrieveChargeHistoryRawRequestInvalidTransactionId)
        .returns(List(TransactionIdFormatError))

      parser.parseRequest(invalidRetrieveChargeHistoryRawRequestInvalidTransactionId) shouldBe
        Left(ErrorWrapper(correlationId, TransactionIdFormatError, None))
    }
  }

  "return multiple errors" when {
    "an invalid nino and invalid charge id are provided" in new Test {
      MockRetrieveChargeHistoryValidator
        .validate(invalidRetrieveChargeHistoryRawRequestInvalidNinoAndTransactionId)
        .returns(List(NinoFormatError, TransactionIdFormatError))

      parser.parseRequest(invalidRetrieveChargeHistoryRawRequestInvalidNinoAndTransactionId) shouldBe
        Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TransactionIdFormatError))))
    }
  }

}
