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
import api.utils.UnitSpec
import RetrieveItsaPenaltiesFixture.timeToPaymodel

class TimeToPaySpec extends UnitSpec {

  val downstreamJson: JsValue = Json.parse(
    """
        |{
        |  "ttpStartDate": "2024-12-15",
        |  "ttpEndDate": "2025-06-15"
        |}
        |""".stripMargin
  )

  val mtdJson: JsValue = Json.parse(
    """
        |{
        |  "startDate": "2024-12-15",
        |  "endDate": "2025-06-15"
        |}
        |""".stripMargin
  )

  "TimeToPay" should {

    "successfully read from valid json" in {
      downstreamJson.as[TimeToPay] shouldBe timeToPaymodel
    }

    "fail to read from invalid json" in {
      JsObject.empty.validate[TimeToPay] shouldBe a[JsError]
    }

    "write to json" in {
      Json.toJson(timeToPaymodel) shouldBe mtdJson
    }
  }

}
