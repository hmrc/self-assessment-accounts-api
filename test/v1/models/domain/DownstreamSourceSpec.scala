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

package v1.models.domain

import support.UnitSpec
import utils.enums.EnumJsonSpecSupport
import v1.models.domain.DownstreamSource._

class DownstreamSourceSpec extends UnitSpec with EnumJsonSpecSupport {

  testRoundTrip[DownstreamSource](
    ("HMRC HELD", `HMRC HELD`),
    ("CUSTOMER", `CUSTOMER`)
  )

  "toMtdSource" should {
    "return the correct identifier value" in {
      DownstreamSource.`HMRC HELD`.toMtdSource shouldBe "hmrcHeld"
      DownstreamSource.`CUSTOMER`.toMtdSource shouldBe "user"
    }
  }
}