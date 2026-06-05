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
import RetrieveItsaPenaltiesFixture.lateSubmissionPenaltyModel

class LateSubmissionPenaltySpec extends UnitSpec {

  val downstreamJson: JsValue = Json.parse(
    """
        |{
        |  "lspSummary": {
        |    "activePenaltyPoints": 10,
        |    "inactivePenaltyPoints": 12,
        |    "pocAchievementDate": "2025-11-30",
        |    "regimeThreshold": 10,
        |    "penaltyChargeAmount": 684.25
        |  },
        |  "lspDetails": [
        |    {
        |      "penaltyNumber": "12345678901234",
        |      "penaltyOrder": "01",
        |      "penaltyCategory": "P",
        |      "penaltyStatus": "ACTIVE",
        |      "fapIndicator": "X",
        |      "penaltyCreationDate": "2024-11-14",
        |      "penaltyExpiryDate": "2026-11-14",
        |      "expiryReason": "FAP",
        |      "communicationsDate": "2024-11-15",
        |      "lateSubmissions": [
        |       {
        |      "lateSubmissionID": "1054",
        |      "incomeSource": "Income Tax Liability",
        |      "taxPeriod": "24C3",
        |      "taxReturnStatus": "Fulfilled",
        |      "taxPeriodStartDate": "2024-07-01",
        |      "taxPeriodEndDate": "2024-09-30",
        |      "taxPeriodDueDate": "2024-11-07",
        |      "returnReceiptDate": "2024-11-13"
        |        }
        |     ],
        |    "appealInformation": [
        |        {
        |      "appealStatus": "A",
        |      "appealLevel": "02",
        |      "appealDescription": "Reason to appeal"
        |        }
        |      ],
        |      "chargeReference": "XP001286394838",
        |      "chargeAmount": 543.29,
        |      "chargeOutstandingAmount": 123.26,
        |      "chargeDueDate": "2024-12-04"
        |    }
        |  ]
        |}
        |""".stripMargin
  )

  val mtdJson: JsValue = Json.parse(
    """
        |{
        |  "summary": {
        |    "activePenaltyPoints": 10,
        |    "inactivePenaltyPoints": 12,
        |    "periodOfComplianceAchievement": "2025-11-30",
        |    "regimeThreshold": 10,
        |    "penaltyChargeAmount": 684.25
        |  },
        |  "details": [
        |    {
        |      "penaltyNumber": "12345678901234",
        |      "penaltyOrder": "01",
        |      "penaltyCategory": "point",
        |      "penaltyStatus": "active",
        |      "frequencyAdjustmentPointIndicator": true,
        |      "penaltyCreationDate": "2024-11-14",
        |      "penaltyExpiryDate": "2026-11-14",
        |      "expiryReason": "submission-frequency-change",
        |      "communicationsDate": "2024-11-15",
        |      "lateSubmissions": [
        |       {
        |      "lateSubmissionId": "1054",
        |      "incomeSource": "Income Tax Liability",
        |      "taxPeriod": "24C3",
        |      "taxReturnStatus": "fulfilled",
        |      "taxPeriodStartDate": "2024-07-01",
        |      "taxPeriodEndDate": "2024-09-30",
        |      "taxPeriodDueDate": "2024-11-07",
        |      "returnReceiptDate": "2024-11-13"
        |       }
        |        ],
        |      "appealInformation": [
        |       {
        |      "status": "under-appeal",
        |      "level": "appeal-first-tier-tribunal",
        |      "description": "Reason to appeal"
        |          }
        |       ],
        |      "chargeReference": "XP001286394838",
        |      "chargeAmount": 543.29,
        |      "chargeOutstandingAmount": 123.26,
        |      "chargeDueDate": "2024-12-04"
        |    }
        |  ]
        |}
        |""".stripMargin
  )

  "LateSubmissionPenalty" should {

    "successfully read from valid json" in {
      downstreamJson.as[LateSubmissionPenalty] shouldBe lateSubmissionPenaltyModel
    }

    "fail to read from invalid json" in {
      JsObject.empty.validate[LateSubmissionPenalty] shouldBe a[JsError]
    }

    "write to json" in {
      Json.toJson(lateSubmissionPenaltyModel) shouldBe mtdJson
    }
  }

}
