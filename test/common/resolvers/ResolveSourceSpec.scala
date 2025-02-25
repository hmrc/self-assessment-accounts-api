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

package common.resolvers

import cats.data.Validated.{Invalid, Valid}
import common.errors.SourceFormatError
import common.models.MtdSource
import shared.utils.UnitSpec

class ResolveSourceSpec extends UnitSpec {

  "ResolveSource" should {
    "return no errors" when {
      "passed a valid source" in {
        ResolveSource(Some("hmrcHeld")) shouldBe Valid(Some(MtdSource.`hmrcHeld`))
        ResolveSource(Some("user")) shouldBe Valid(Some(MtdSource.`user`))
        ResolveSource(Some("latest")) shouldBe Valid(Some(MtdSource.`latest`))
      }

      "no source is provided" in {
        ResolveSource(None) shouldBe Valid(None)
      }
    }

    "return an error" when {
      "passed an invalid source" in {
        ResolveSource(Some("notASource")) shouldBe Invalid(List(SourceFormatError))
      }
    }
  }

}
