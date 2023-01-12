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

package api.controllers.requestParsers.validators.validations

import api.models.errors.IdFormatError
import support.UnitSpec

class IdValidationSpec extends UnitSpec {

  val validNumber             = BigDecimal(2000)
  val negativeNumber          = BigDecimal(-2000.99)
  val incorrectDecimalsNumber = BigDecimal(2000.9)
  val tooBigNumber            = BigDecimal(199999999999.99)
  val path                    = "path"

  "number validation" should {
    "return no errors" when {
      "a valid number is supplied" in {
        IdValidation.validate(validNumber, path) shouldBe Nil
      }
    }

    "return IdFormatError" when {
      "a negative number is supplied" in {
        IdValidation.validate(negativeNumber, path) shouldBe List(IdFormatError.copy(paths = Some(Seq(path))))
      }

      "a number with too many decimals is supplied" in {
        IdValidation.validate(incorrectDecimalsNumber, path) shouldBe List(IdFormatError.copy(paths = Some(Seq(path))))
      }

      "a number that is too large is supplied" in {
        IdValidation.validate(tooBigNumber, path) shouldBe List(IdFormatError.copy(paths = Some(Seq(path))))
      }
    }

  }

}
