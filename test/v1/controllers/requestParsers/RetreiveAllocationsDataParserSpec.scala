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
import v1.mocks.validators.MockRetrieveAllocationsValidator
import v1.models.errors._
import v1.models.requestData.{RetrieveAllocationsRawData, RetrieveAllocationsRequest}

class RetreiveAllocationsDataParserSpec extends UnitSpec {

  val nino = "AA123456B"
  val paymentId = "anId"

  val inputData: RetrieveAllocationsRawData = RetrieveAllocationsRawData(nino, paymentId)

  trait Test extends MockRetrieveAllocationsValidator {
    lazy val parser = new RetrieveAllocationsDataParser(mockValidator)
  }

  "parse" should {
    "return a request object" when {
      "valid request data is supplied" in new Test {

        MockRetrieveAllocationsValidator.validate(inputData)
          .returns(Nil)

        parser.parseRequest(inputData) shouldBe
          Right(RetrieveAllocationsRequest(Nino(nino), "anId"))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {

        MockRetrieveAllocationsValidator.validate(inputData)
          .returns(List(NinoFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(None, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {

        MockRetrieveAllocationsValidator.validate(inputData)
          .returns(List(NinoFormatError, PaymentIdFormatError))

        parser.parseRequest(inputData) shouldBe
          Left(ErrorWrapper(None, BadRequestError, Some(Seq(NinoFormatError, PaymentIdFormatError))))
      }
    }
  }
}
