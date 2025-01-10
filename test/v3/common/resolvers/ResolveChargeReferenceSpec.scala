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

package v3.common.resolvers

import cats.data.Validated.{Invalid, Valid}
import common.errors.ChargeReferenceFormatError
import shared.utils.UnitSpec
import v3.common.models.ChargeReference

class ResolveChargeReferenceSpec extends UnitSpec {

  "ResolveChargeReference" should {
    "return no errors" when {
      "given a valid Charge Reference" in {
        val value  = "AB123456789012"
        val result = ResolveChargeReference(None, None)(value)
        result shouldBe Valid(Some(ChargeReference(value)))
      }
    }

    "return an error" when {
      "given an invalid ChargeReference" in {
        val result = ResolveChargeReference(None, None)("not-a-transaction-id")
        result shouldBe Invalid(List(ChargeReferenceFormatError))
      }
    }
  }

}
