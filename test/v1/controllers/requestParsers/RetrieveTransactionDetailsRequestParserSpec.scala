/*
 * Copyright 2020 HM Revenue & Customs
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
import uk.gov.hmrc.domain.Nino
import v1.mocks.validators.MockRetrieveTransactionDetailsValidator
import v1.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, TransactionIdFormatError}
import v1.models.request.retrieveTransactionDetails.{RetrieveTransactionDetailsParsedRequest, RetrieveTransactionDetailsRawRequest}

class RetrieveTransactionDetailsRequestParserSpec extends UnitSpec {

  trait Test extends MockRetrieveTransactionDetailsValidator {
    lazy val parser = new RetrieveTransactionDetailsRequestParser(mockValidator)
  }

  private val validNino = "AA111111A"
  private val validTransactionId = "F02LDPDEE"
  private val invalidNino = "notANino"
  private val invalidTransactionId = "notATransactionNino"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  "parsing a retrieve TransactionDetails request" should {
    "return a retrieve TransactionDetails request" when {
      "valid data is provided" in new Test {

        MockRetrieveTransactionDetailsValidator.validate(RetrieveTransactionDetailsRawRequest(validNino, validTransactionId))
          .returns(Nil)

        parser.parseRequest(RetrieveTransactionDetailsRawRequest(validNino, validTransactionId)) shouldBe
          Right(RetrieveTransactionDetailsParsedRequest(Nino(validNino), validTransactionId))
      }
    }
  }

  "return an error" when {
    "invalid data is provided" in new Test{
      MockRetrieveTransactionDetailsValidator.validate(RetrieveTransactionDetailsRawRequest(invalidNino, validTransactionId))
        .returns(List(NinoFormatError))

      parser.parseRequest(RetrieveTransactionDetailsRawRequest(invalidNino, validTransactionId)) shouldBe
        Left(ErrorWrapper(correlationId, NinoFormatError, None))
    }
  }

  "return multiple errors" when {
    "multiple request parameters are supplied" in new Test{
      MockRetrieveTransactionDetailsValidator.validate(RetrieveTransactionDetailsRawRequest(invalidNino, invalidTransactionId))
        .returns(List(NinoFormatError, TransactionIdFormatError))

      parser.parseRequest(RetrieveTransactionDetailsRawRequest(invalidNino, invalidTransactionId)) shouldBe
        Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TransactionIdFormatError))))
    }
  }
}