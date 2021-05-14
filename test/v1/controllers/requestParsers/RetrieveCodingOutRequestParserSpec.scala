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
import uk.gov.hmrc.domain.Nino
import v1.mocks.validators.MockRetrieveCodingOutValidator
import v1.models.errors._
import v1.models.request.retrieveCodingOut.{RetrieveCodingOutParsedRequest, RetrieveCodingOutRawRequest}

class RetrieveCodingOutRequestParserSpec extends UnitSpec {

  val taxYear: String = "2021-22"
  val nino: String = "AA123456B"
  val source: Option[String] = Some("hmrcHeld")
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val retrieveCodingOutRawData: RetrieveCodingOutRawRequest = RetrieveCodingOutRawRequest(nino, taxYear, source)

  trait Test extends MockRetrieveCodingOutValidator {
    lazy val parser = new RetrieveCodingOutRequestParser(mockValidator)
  }

  "parsing a retrieve coding out request" should {
    "return a retrieve coding out request" when {
      "valid data is provided" in new Test {

        MockValidator.validate(retrieveCodingOutRawData)
          .returns(Nil)

        parser.parseRequest(retrieveCodingOutRawData) shouldBe
          Right(RetrieveCodingOutParsedRequest(Nino(nino), taxYear, source))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test {
        MockValidator.validate(retrieveCodingOutRawData)
          .returns(List(NinoFormatError))


        parser.parseRequest(retrieveCodingOutRawData) shouldBe
          Left(ErrorWrapper(correlationId, NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test {
        MockValidator.validate(retrieveCodingOutRawData)
          .returns(List(NinoFormatError, TaxYearFormatError, SourceFormatError))


        parser.parseRequest(retrieveCodingOutRawData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError, SourceFormatError))))
      }
    }
  }
}