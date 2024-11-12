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

import api.models.domain.MtdSource
import api.models.errors.SourceFormatError
import cats.data.Validated.{Invalid, Valid}
import support.UnitSpec

class ResolveSourceSpec extends UnitSpec {

  "ResolveSource" should {
    "return no errors" when {
      "passed a valid Source" in {
        ResolveSource("hmrcHeld") shouldBe Valid(MtdSource.`hmrcHeld`)
        ResolveSource("user") shouldBe Valid(MtdSource.`user`)
        ResolveSource("latest") shouldBe Valid(MtdSource.`latest`)
      }
    }

    "return an error" when {
      "passed an invalid source" in {
        ResolveSource("notASource") shouldBe Invalid(List(SourceFormatError))
      }
    }
  }

}