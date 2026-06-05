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

package v4.retrieveItsaPenalties.model.response

import play.api.libs.json.*
import shared.utils.UnitSpec
import RetrieveItsaPenaltiesFixture.appealInformationModel

class AppealInformationSpec extends UnitSpec {

  val downstreamJson: JsValue = Json.parse(
    """
      |{
      |  "appealStatus": "A",
      |  "appealLevel": "02",
      |  "appealDescription": "Reason to appeal"
      |}
      |""".stripMargin
  )

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |  "status": "under-appeal",
      |  "level": "appeal-first-tier-tribunal",
      |  "description": "Reason to appeal"
      |}
      |""".stripMargin
  )

  "AppealInformation" should {

    "successfully read from valid json" in {
      downstreamJson.as[AppealInformation] shouldBe appealInformationModel
    }

    "fail to read from invalid json" in {
      JsObject.empty.validate[AppealInformation] shouldBe a[JsError]
    }

    "write to json" in {
      Json.toJson(appealInformationModel) shouldBe mtdJson
    }
  }

}
