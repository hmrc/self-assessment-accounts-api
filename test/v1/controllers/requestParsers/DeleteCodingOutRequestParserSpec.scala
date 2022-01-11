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
import v1.mocks.validators.MockDeleteCodingOutValidator
import v1.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, TaxYearFormatError}
import v1.models.request.deleteCodingOut._

class DeleteCodingOutRequestParserSpec extends UnitSpec {

  val taxYear: String = "2021-22"
  val nino: String = "AA123456B"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  val deleteCodingOutRawData: DeleteCodingOutRawRequest = DeleteCodingOutRawRequest(nino, taxYear)

  trait Test extends MockDeleteCodingOutValidator {
    lazy val parser = new DeleteCodingOutParser(mockValidator)
  }

  "parse" should {
    "return request object" when {
      "valid request data is supplied" in new Test{
        MockValidator.validate(deleteCodingOutRawData).returns(Nil)


        parser.parseRequest(deleteCodingOutRawData) shouldBe
          Right(DeleteCodingOutParsedRequest(Nino(nino), taxYear))
      }
    }

    "return an ErrorWrapper" when {
      "a single validation error occurs" in new Test{
        MockValidator.validate(deleteCodingOutRawData)
          .returns(List(NinoFormatError))


        parser.parseRequest(deleteCodingOutRawData) shouldBe
          Left(ErrorWrapper(correlationId,NinoFormatError, None))
      }

      "multiple validation errors occur" in new Test{
        MockValidator.validate(deleteCodingOutRawData)
          .returns(List(NinoFormatError, TaxYearFormatError))


        parser.parseRequest(deleteCodingOutRawData) shouldBe
          Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }
}