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

package v1.controllers.requestParsers

import support.UnitSpec
import v1.models.domain.Nino
import v1.fixtures.RetrieveBalanceFixture._
import v1.mocks.validators.MockRetrieveBalanceValidator
import v1.models.errors.{ErrorWrapper, NinoFormatError}
import v1.models.request.retrieveBalance.RetrieveBalanceParsedRequest

class RetrieveBalanceRequestParserSpec extends UnitSpec {

  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  trait Test extends MockRetrieveBalanceValidator {
    lazy val parser = new RetrieveBalanceRequestParser(mockValidator)
  }

  "parsing a retrieve Balance request" should {
    "return a retrieve Balance request" when {
      "valid data is provided" in new Test {

        MockRetrieveBalanceValidator.validate(validRetrieveBalanceRawRequest)
          .returns(Nil)

        parser.parseRequest(validRetrieveBalanceRawRequest) shouldBe
          Right(RetrieveBalanceParsedRequest(Nino(validNino)))
      }
    }
  }

  "return an error" when {
    "invalid data is provided" in new Test{
      MockRetrieveBalanceValidator.validate(invalidRetrieveBalanceRawRequest)
        .returns(List(NinoFormatError))

      parser.parseRequest(invalidRetrieveBalanceRawRequest) shouldBe
        Left(ErrorWrapper(correlationId, NinoFormatError, None))
    }
  }
}