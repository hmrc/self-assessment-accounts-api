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
import RetrieveItsaPenaltiesFixture.latePaymentPenaltyModel

class LatePaymentPenaltySpec extends UnitSpec {

  val downstreamJson: JsValue = Json.parse(
    """
      |{
      |  "lppDetails": [
      |     {
      |  "principalChargeReference": "XV123451234512",
      |  "penaltyCategory": "LPP1",
      |  "penaltyStatus": "P",
      |  "penaltyAmountAccruing": 99.99,
      |  "penaltyAmountPosted": 1001.45,
      |  "penaltyAmountPaid": 1001.45,
      |  "penaltyAmountOutstanding": 99.99,
      |  "lpp1LRCalculationAmt": 99.99,
      |  "lpp1LRPercentage": 2,
      |  "lpp1HRCalculationAmt": 99.99,
      |  "lpp1HRPercentage": 2,
      |  "lpp2Days": "31",
      |  "lpp2Percentage": 4,
      |  "penaltyChargeCreationDate": "2024-12-08",
      |  "communicationsDate": "2024-12-09",
      |  "penaltyChargeReference": "XP298765432109",
      |  "penaltyChargeDueDate": "2025-01-08",
      |    "appealInformation": [
      |              {
      |                "appealStatus": "A",
      |                "appealLevel": "02",
      |                "appealDescription": "Reason to appeal"
      |              }
      |            ],
      |  "principalChargeDocNumber": "123456789012",
      |  "principalChargeBillingFrom": "2024-07-01",
      |  "principalChargeBillingTo": "2024-09-30",
      |  "principalChargeDueDate": "2024-11-07",
      |  "principalChargeLatestClearing": "2025-02-15",
      |   "timeToPay": [
      |              {
      |                "ttpStartDate": "2024-12-15",
      |                "ttpEndDate": "2025-06-15"
      |              }
      |            ]
      |  }
      |   ]
      |  }
      |""".stripMargin
  )

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |   "details": [
      |      {
      |  "principalChargeReference": "XV123451234512",
      |  "penaltyCategory": "lpp1",
      |  "penaltyStatus": "posted",
      |  "penaltyAmountAccruing": 99.99,
      |  "penaltyAmountPosted": 1001.45,
      |  "penaltyAmountPaid": 1001.45,
      |  "penaltyAmountOutstanding": 99.99,
      |  "latePaymentPenalty1LowerRateCalculationAmount": 99.99,
      |  "latePaymentPenalty1LowerRatePercentage": 2,
      |  "latePaymentPenalty1HigherRateCalculationAmount": 99.99,
      |  "latePaymentPenalty1HigherRatePercentage": 2,
      |  "latePaymentPenalty2Days": 31,
      |  "latePaymentPenalty2Percentage": 4,
      |  "penaltyChargeCreationDate": "2024-12-08",
      |  "communicationsDate": "2024-12-09",
      |  "penaltyChargeReference": "XP298765432109",
      |  "penaltyChargeDueDate": "2025-01-08",
      |   "appealInformation": [
      |          {
      |            "status": "under-appeal",
      |            "level": "appeal-first-tier-tribunal",
      |            "description": "Reason to appeal"
      |          }
      |        ],
      |  "principalChargeDocNumber": "123456789012",
      |  "principalChargeBillingFrom": "2024-07-01",
      |  "principalChargeBillingTo": "2024-09-30",
      |  "principalChargeDueDate": "2024-11-07",
      |  "principalChargeLatestClearing": "2025-02-15",
      |  "timeToPay": [
      |          {
      |            "startDate": "2024-12-15",
      |            "endDate": "2025-06-15"
      |          }
      |        ]
      |  }
      |   ]
      | }
      |""".stripMargin
  )

  "LatePaymentPenalty" should {

    "read from json" in {
      downstreamJson.as[LatePaymentPenalty] shouldBe latePaymentPenaltyModel
    }

    "write to json" in {
      Json.toJson(latePaymentPenaltyModel) shouldBe mtdJson
    }

  }

}
