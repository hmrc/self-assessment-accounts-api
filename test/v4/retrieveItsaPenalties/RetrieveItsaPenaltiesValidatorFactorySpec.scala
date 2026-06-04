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

package v4.retrieveItsaPenalties

import shared.utils.UnitSpec
import shared.models.domain.Nino
import shared.controllers.validators.Validator
import v4.retrieveItsaPenalties.model.request.RetrieveItsaPenaltiesRequestData
import shared.models.errors.*

class RetrieveItsaPenaltiesValidatorFactorySpec extends UnitSpec {

  private implicit val correlationId: String = "1234"

  private val validNino: String = "AA123456A"

  private val parsedNino: Nino = Nino(validNino)

  private val validatorFactory: RetrieveItsaPenaltiesValidatorFactory = new RetrieveItsaPenaltiesValidatorFactory

  private def validator(nino: String): Validator[RetrieveItsaPenaltiesRequestData] = validatorFactory.validator(nino)

  "RetrieveItsaPenaltiesValidatorFactory" when {
    "validator" should {
      "return the parsed domain object" when {
        "a valid request is supplied" in {
          val result: Either[ErrorWrapper, RetrieveItsaPenaltiesRequestData] = validator(validNino).validateAndWrapResult()

          result shouldBe Right(RetrieveItsaPenaltiesRequestData(parsedNino))
        }
      }

      "return NinoFormatError" when {
        "an invalid nino is supplied" in {
          val result: Either[ErrorWrapper, RetrieveItsaPenaltiesRequestData] = validator("invalidNino").validateAndWrapResult()

          result shouldBe Left(ErrorWrapper(correlationId, NinoFormatError))
        }
      }
    }
  }

}
