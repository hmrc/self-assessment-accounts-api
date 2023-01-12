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

import api.models.errors.{BadRequestError, ErrorWrapper, DocNumberFormatError, NinoFormatError}
import support.UnitSpec
import v2.fixtures.retrieveBalanceAndTransactions.RequestFixture._
import v2.mocks.validators.MockRetrieveBalanceAndTransactionsValidator

class RetrieveBalanceAndTransactionsRequestParserSpec extends UnitSpec {
  implicit val correlationId: String = "X-123"

  trait Test extends MockRetrieveBalanceAndTransactionsValidator {
    lazy val parser = new RetrieveBalanceAndTransactionsRequestParser(mockValidator)
  }

  "parse" should {
    "return a request object" when {
      "valid request data with doc number supplied" in new Test {
        MockRetrieveBalanceAndTransactionsValidator.validate(inputDataDocNumber).returns(Nil)

        parser.parseRequest(inputDataDocNumber) shouldBe Right(requestDocNumber)
      }

      "valid request data with date from and date to supplied" in new Test {
        MockRetrieveBalanceAndTransactionsValidator.validate(inputDataDateRange).returns(Nil)

        parser.parseRequest(inputDataDateRange) shouldBe Right(requestDateRange)
      }

      "valid request data with everything true" in new Test {
        MockRetrieveBalanceAndTransactionsValidator.validate(inputDataEverythingTrue).returns(Nil)

        parser.parseRequest(inputDataEverythingTrue) shouldBe Right(requestEverythingTrue)
      }

      "valid request data with everything false" in new Test {
        MockRetrieveBalanceAndTransactionsValidator.validate(inputDataEverythingFalse).returns(Nil)

        parser.parseRequest(inputDataEverythingFalse) shouldBe Right(requestDocNumber)
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockRetrieveBalanceAndTransactionsValidator
          .validate(inputDataDocNumber)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputDataDocNumber) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occurs" in new Test {
        MockRetrieveBalanceAndTransactionsValidator
          .validate(inputDataDocNumber)
          .returns(List(NinoFormatError, DocNumberFormatError))

        parser.parseRequest(inputDataDocNumber) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(List(NinoFormatError, DocNumberFormatError))))
      }
    }
  }

}
