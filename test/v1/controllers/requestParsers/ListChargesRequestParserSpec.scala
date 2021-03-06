/*
 * Copyright 2021 HM Revenue & Customs
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
import v1.models.domain.Nino
import v1.mocks.validators.MockListChargesValidator
import v1.models.errors._
import v1.models.request.listCharges._

class ListChargesRequestParserSpec extends UnitSpec {

  val nino: String = "AA123456B"
  val from: String = "2019/02/02"
  val to: String = "2019/02/03"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  trait Test extends MockListChargesValidator {
    lazy val parser = new ListChargesRequestParser(mockValidator)
  }

  "parse" should {
    "return a parsed request" when {
      "no validation errors occur" in new Test {
        private val inputData = ListChargesRawRequest(nino, Some(from), Some(to))
        MockListChargesValidator.validate(inputData).returns(Nil)
        parser.parseRequest(inputData) shouldBe Right(ListChargesParsedRequest(Nino(nino), from, to))
      }
    }
    "return an error wrapper" when {
      "the validator returns a single error" in new Test {
        private val inputData = ListChargesRawRequest("AA123", Some(from), Some(to))
        MockListChargesValidator.validate(inputData).returns(List(NinoFormatError))
        parser.parseRequest(inputData) shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
      "the validator returns multiple errors" in new Test {
        private val inputData = ListChargesRawRequest(nino, None, None)
        MockListChargesValidator.validate(inputData).returns(List(MissingFromDateError, MissingToDateError))
        parser.parseRequest(inputData) shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(MissingFromDateError, MissingToDateError))))
      }
    }
  }

}