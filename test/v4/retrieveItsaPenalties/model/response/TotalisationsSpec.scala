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
import RetrieveItsaPenaltiesFixture.totalisationsmodel

class TotalisationsSpec extends UnitSpec {

  val downstreamJson: JsValue = Json.parse(
    """
        |{
        |  "lspTotalValue": 200,
        |  "penalisedPrincipalTotal": 2000,
        |  "lppPostedTotal": 165.25,
        |  "lppEstimatedTotal": 15.26
        |}
        |""".stripMargin
  )

  val mtdJson: JsValue = Json.parse(
    """
        |{
        |  "lateSubmissionPenaltyTotalValue": 200,
        |  "penalisedPrincipalTotal": 2000,
        |  "latePaymentPenaltyPostedTotal": 165.25,
        |  "latePaymentPenaltyEstimateTotal": 15.26
        |}
        |""".stripMargin
  )

  "Totalisations" should {

    "successfully read from valid json" in {
      downstreamJson.as[Totalisations] shouldBe totalisationsmodel
    }

    "fail to read from invalid json" in {
      JsObject.empty.validate[Totalisations] shouldBe a[JsError]
    }

    "write to json" in {
      Json.toJson(totalisationsmodel) shouldBe mtdJson
    }
  }

}
