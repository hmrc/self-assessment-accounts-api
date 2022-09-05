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

package api.controllers.requestParsers.validators.validations

import api.models.errors.ValueFormatError
import support.UnitSpec

class NumberValidationSpec extends UnitSpec {

  val validNumber             = Some(BigDecimal(2000.99))
  val negativeNumber          = Some(BigDecimal(-2000.99))
  val incorrectDecimalsNumber = Some(BigDecimal(2000.999))
  val tooBigNumber            = Some(BigDecimal(199999999999.99))
  val path                    = "path"

  "number validation" should {
    "return no errors" when {
      "a valid number is supplied" in {
        NumberValidation.validateOptional(validNumber, path) shouldBe Nil
      }

      "None is supplied" in {
        NumberValidation.validateOptional(None, path) shouldBe Nil
      }
    }

    "return ValueFormatError" when {
      "a negative number is supplied" in {
        NumberValidation.validateOptional(negativeNumber, path) shouldBe List(ValueFormatError.copy(paths = Some(Seq(path))))
      }

      "a number with too many decimals is supplied" in {
        NumberValidation.validateOptional(incorrectDecimalsNumber, path) shouldBe List(ValueFormatError.copy(paths = Some(Seq(path))))
      }

      "a number that is too large is supplied" in {
        NumberValidation.validateOptional(tooBigNumber, path) shouldBe List(ValueFormatError.copy(paths = Some(Seq(path))))
      }
    }

  }

}
