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
import v1.mocks.validators.MockRetrieveTransactionsValidator
import v1.models.errors._
import v1.models.request.retrieveTransactions.{RetrieveTransactionsParsedRequest, RetrieveTransactionsRawRequest}

class RetrieveTransactionsRequestParserSpec extends UnitSpec {

  val nino = "AA123456B"
  val from = "2019/02/02"
  val to = "2019/02/03"

  trait Test extends MockRetrieveTransactionsValidator {
    lazy val parser = new RetrieveTransactionsRequestParser(mockValidator)
  }

  "parse" should {
    "return a parsed request" when {
      "no validation errors occur" in new Test {
        private val inputData = RetrieveTransactionsRawRequest(nino, Some(from), Some(to))
        MockRetrieveTransactionsValidator.validate(inputData).returns(Nil)
        parser.parseRequest(inputData) shouldBe Right(RetrieveTransactionsParsedRequest(Nino(nino), from, to))
      }
    }
    "return an error wrapper" when {
      "the validator returns a single error" in new Test {
        private val inputData = RetrieveTransactionsRawRequest("AA123", Some(from), Some(to))
        MockRetrieveTransactionsValidator.validate(inputData).returns(List(NinoFormatError))
        parser.parseRequest(inputData) shouldBe Left(ErrorWrapper(None, NinoFormatError))
      }
      "the validator returns multiple errors" in new Test {
        private val inputData = RetrieveTransactionsRawRequest(nino, None, None)
        MockRetrieveTransactionsValidator.validate(inputData).returns(List(MissingFromDateError, MissingToDateError))
        parser.parseRequest(inputData) shouldBe Left(ErrorWrapper(None, BadRequestError, Some(Seq(MissingFromDateError, MissingToDateError))))
      }
    }
  }

}
