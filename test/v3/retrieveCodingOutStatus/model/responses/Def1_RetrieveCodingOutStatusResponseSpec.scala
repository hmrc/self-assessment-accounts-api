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

package v3.retrieveCodingOutStatus.model.responses

import play.api.libs.json.Json
import shared.utils.UnitSpec
import v3.retrieveCodingOutStatus.def1.model.response.Def1_RetrieveCodingOutStatusResponse
import v3.retrieveCodingOutStatus.model.responses.ResponseFixture._

class Def1_RetrieveCodingOutStatusResponseSpec extends UnitSpec {

  "Def1_RetrieveCodingOutStatusResponse" when {
    "read from valid JSON" should {
      "produce the expected Def1_RetrieveCodingOutStatusResponse object" in {
        downstreamResponseJson.as[Def1_RetrieveCodingOutStatusResponse] shouldBe def1ResponseModel
      }
    }

    "written to JSON" should {
      "produce the expected JSON object" in {
        Json.toJson(def1ResponseModel) shouldBe mtdResponseJson
      }
    }
  }

}
