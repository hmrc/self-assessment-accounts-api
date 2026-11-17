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

import api.utils.UnitSpec
import play.api.libs.json.{JsValue, Json}
import v4.retrieveItsaPenalties.model.response.RetrieveItsaPenaltiesFixture.timeToPayForPenaltyChargesModel

class TimeToPayForPenaltyChargesSpec extends UnitSpec {

  val downstreamJson: JsValue = Json.parse(
    """
      |{
      |  "ttpForPenalty": true,
      |  "ttpStartDateForPenalty": "2024-12-15",
      |  "ttpEndDateForPenalty": "2025-06-15"
      |}
      |""".stripMargin
  )

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |  "penaltyChargeIndicator": true,
      |  "startDate": "2024-12-15",
      |  "endDate": "2025-06-15"
      |}
      |""".stripMargin
  )

  "TimeToPayForParentCharges" should {

    "successfully read from valid json" in {
      downstreamJson.as[TimeToPayForPenaltyCharges] shouldBe timeToPayForPenaltyChargesModel
    }

    "write to json" in {
      Json.toJson(timeToPayForPenaltyChargesModel) shouldBe mtdJson
    }
  }

}
