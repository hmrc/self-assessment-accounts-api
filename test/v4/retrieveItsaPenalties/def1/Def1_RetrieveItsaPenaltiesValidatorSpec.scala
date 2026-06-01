/*
 * Copyright 2026 HM Revenue & Customs
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

package v4.retrieveItsaPenalties.def1

import shared.models.domain.Nino
import shared.models.errors.*
import shared.utils.UnitSpec
import v4.retrieveItsaPenalties.model.request.RetrieveItsaPenaltiesRequestData

class Def1_RetrieveItsaPenaltiesValidatorSpec extends UnitSpec {
  private implicit val correlationId: String = "1234"

  private val validNino = "AA123456A"

  private val parsedNino = Nino(validNino)

  private def validator(nino: String) =
    new Def1_RetrieveItsaPenaltiesValidator(nino)

  "validator" should {
    "return the parsed domain object" when {
      "passed a valid request" in {
        val result: Either[ErrorWrapper, RetrieveItsaPenaltiesRequestData] =
          validator(validNino).validateAndWrapResult()

        result shouldBe Right(RetrieveItsaPenaltiesRequestData(parsedNino))
      }

    }
    "should return a single error" when {
      "an invalid nino is supplied" in {
        val result: Either[ErrorWrapper, RetrieveItsaPenaltiesRequestData] =
          validator("invalidNino").validateAndWrapResult()

        result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
      }
    }
  }

}
