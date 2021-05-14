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

import play.api.libs.json.Json
import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import v1.mocks.validators.MockCreateOrAmendCodingOutValidator
import v1.models.errors.{BadRequestError, ErrorWrapper, NinoFormatError, TaxYearFormatError}
import v1.models.request.createOrAmendCodingOut.{CreateOrAmendCodingOutParsedRequest, CreateOrAmendCodingOutRawRequest, CreateOrAmendCodingOutRequestBody}

class CreateOrAmendCodingOutParserSpec extends UnitSpec{

  val nino = "AA123456A"
  val taxYear = "2021-22"
  implicit val correlationId: String = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"

  private val validJson = Json.parse(
    """
      |{
      |   "payeUnderpayments": 2000.99,
      |   "selfAssessmentUnderPayments": 2000.99,
      |   "debts": 2000.99,
      |   "inYearAdjustments": 5000.99
      |}
      |""".stripMargin)

  val request = CreateOrAmendCodingOutRawRequest(nino, taxYear, validJson)
  val validBody = CreateOrAmendCodingOutRequestBody(Some(2000.99), Some(2000.99), Some(2000.99), Some(5000.99))

  trait Test extends MockCreateOrAmendCodingOutValidator {
    lazy val parser = new CreateOrAmendCodingOutParser(mockValidator)
  }

  "parse" should{
    "return a parsed request" when {
      "no validation errors occur" in new Test {
        MockCreateOrAmendCodingOutValidator.validate(request).returns(Nil)
        parser.parseRequest(request) shouldBe Right(CreateOrAmendCodingOutParsedRequest(Nino(nino), taxYear, validBody))
      }
    }

    "return an error wrapper" when {
      "when a single validation error occurs" in new Test {
        MockCreateOrAmendCodingOutValidator.validate(request).returns(List(NinoFormatError))
        parser.parseRequest(request) shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }

      "when multiple validation errors occur" in new Test {
        MockCreateOrAmendCodingOutValidator.validate(request).returns(List(NinoFormatError, TaxYearFormatError))
        parser.parseRequest(request) shouldBe Left(ErrorWrapper(correlationId, BadRequestError, Some(Seq(NinoFormatError, TaxYearFormatError))))
      }
    }
  }

}
