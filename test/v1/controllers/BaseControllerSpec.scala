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

package v1.controllers

import support.UnitSpec
import utils.Logging
import v1.models.hateoas.Method

class BaseControllerSpec extends UnitSpec {
  val controller = new BaseController with Logging

  val numbers = Seq("1", "1.0", "1.00")

  val strings = Seq(
    Method.GET -> "GET" -> "Enum",
    Method.POST -> "POST" -> "Enum",
    Method.DELETE -> "DELETE" -> "Enum",
    "beans" -> "beans" -> "String"
  )

  "base controller" when {
    "toSerializedString is called" should {
      numbers.foreach {
        number =>
          s"return [1.00] when passed the number [$number]" in {
              controller.toSerializedString(BigDecimal(number)).toString shouldBe "1.00"
          }
      }

      strings.foreach {
        case (mapping, inputType) =>
          val (in, out) = mapping
          s"return [ $out ] when passed the $inputType [ $in ]" in {
            controller.toSerializedString(in) shouldBe "\"" + out + "\""
          }
      }
    }
  }
}
