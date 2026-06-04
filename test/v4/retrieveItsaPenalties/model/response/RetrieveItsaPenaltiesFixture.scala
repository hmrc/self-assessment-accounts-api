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

object RetrieveItsaPenaltiesFixture {

  val lateSubmissionsModel: LateSubmissions =
    LateSubmissions(
      lateSubmissionId = "1054",
      incomeSource = Some("Income Tax Liability"),
      taxPeriod = Some("24C3"),
      taxReturnStatus = Some(TaxReturnStatus.fulfilled),
      taxPeriodStartDate = Some("2024-07-01"),
      taxPeriodEndDate = Some("2024-09-30"),
      taxPeriodDueDate = Some("2024-11-07"),
      returnReceiptDate = Some("2024-11-13")
    )

  val lateSubmissionPenaltySummaryModel: LateSubmissionPenaltySummary =
    LateSubmissionPenaltySummary(
      activePenaltyPoints = 10,
      inactivePenaltyPoints = 12,
      regimeThreshold = 10,
      penaltyChargeAmount = 684.25,
      periodOfComplianceAchievement = Some("2025-11-30")
    )

  val totalisationsmodel: Totalisations =
    Totalisations(
      lateSubmissionPenaltyTotalValue = 200,
      penalisedPrincipalTotal = 2000,
      latePaymentPenaltyPostedTotal = 165.25,
      latePaymentPenaltyEstimateTotal = 15.26
    )

  val timeToPaymodel: TimeToPay =
    TimeToPay(
      startDate = "2024-12-15",
      endDate = "2025-06-15"
    )

  val appealInformationModel: AppealInformation =
    AppealInformation(
      status = Status.`under-appeal`,
      level = Some(Level.`appeal-first-tier-tribunal`),
      description = "Reason to appeal"
    )

  val lateSubmissionPenaltyDetailModel: LateSubmissionPenaltyDetail =
    LateSubmissionPenaltyDetail(
      penaltyNumber = "12345678901234",
      penaltyOrder = Some("01"),
      penaltyCategory = Some(SubmissionPenaltyCategory.point),
      penaltyStatus = Some(SubmissionPenaltyStatus.active),
      frequencyAdjustmentPointIndicator = Some(true),
      penaltyCreationDate = "2024-11-14",
      penaltyExpiryDate = "2026-11-14",
      expiryReason = Some(ExpiryReason.`submission-frequency-change`),
      communicationsDate = Some("2024-11-15"),
      lateSubmissions = Some(
        Seq(lateSubmissionsModel)
      ),
      appealInformation = Some(Seq(appealInformationModel)),
      chargeReference = Some("XP001286394838"),
      chargeAmount = Some(543.29),
      chargeOutstandingAmount = Some(123.26),
      chargeDueDate = Some("2024-12-04")
    )

  val lateSubmissionPenaltyModel: LateSubmissionPenalty =
    LateSubmissionPenalty(
      summary = lateSubmissionPenaltySummaryModel,
      details = Seq(lateSubmissionPenaltyDetailModel)
    )

  val latePaymentPenaltyDetailModel: LatePaymentPenaltyDetail =
    LatePaymentPenaltyDetail(
      principalChargeReference = "XV123451234512",
      penaltyCategory = Some(PaymentPenaltyCategory.lpp1),
      penaltyStatus = PaymentPenaltyStatus.posted,
      penaltyAmountAccruing = 99.99,
      penaltyAmountPosted = 1001.45,
      penaltyAmountPaid = Some(1001.45),
      penaltyAmountOutstanding = Some(99.99),
      latePaymentPenalty1LowerRateCalculationAmount = Some(99.99),
      latePaymentPenalty1LowerRatePercentage = Some(2),
      latePaymentPenalty1HigherRateCalculationAmount = Some(99.99),
      latePaymentPenalty1HigherRatePercentage = Some(2),
      latePaymentPenalty2Days = Some(31),
      latePaymentPenalty2Percentage = Some(4),
      penaltyChargeCreationDate = Some("2024-12-08"),
      communicationsDate = Some("2024-12-09"),
      penaltyChargeReference = Some("XP298765432109"),
      penaltyChargeDueDate = Some("2025-01-08"),
      appealInformation = Some(Seq(appealInformationModel)),
      principalChargeDocNumber = Some("123456789012"),
      principalChargeBillingFrom = "2024-07-01",
      principalChargeBillingTo = "2024-09-30",
      principalChargeDueDate = "2024-11-07",
      principalChargeLatestClearing = Some("2025-02-15"),
      timeToPay = Some(
        Seq(timeToPaymodel)
      )
    )

  val latePaymentPenaltyModel: LatePaymentPenalty =
    LatePaymentPenalty(
      details = Some(
        Seq(latePaymentPenaltyDetailModel)
      )
    )

  val responseModel: RetrieveItsaPenaltiesResponse =
    RetrieveItsaPenaltiesResponse(
      totalisations = Some(totalisationsmodel),
      lateSubmissionPenalty = Some(
        lateSubmissionPenaltyModel
      ),
      latePaymentPenalty = Some(latePaymentPenaltyModel)
    )

  val downstreamJson: JsValue = Json.parse(
    """
      |{
      |  "success": {
      |    "processingDate": "2023-11-28T10:15:10Z",
      |    "penaltyData": {
      |      "totalisations": {
      |        "lspTotalValue": 200,
      |        "penalisedPrincipalTotal": 2000,
      |        "lppPostedTotal": 165.25,
      |        "lppEstimatedTotal": 15.26
      |      },
      |      "lsp": {
      |        "lspSummary": {
      |          "activePenaltyPoints": 10,
      |          "inactivePenaltyPoints": 12,
      |          "pocAchievementDate": "2025-11-30",
      |          "regimeThreshold": 10,
      |          "penaltyChargeAmount": 684.25
      |        },
      |        "lspDetails": [
      |          {
      |            "penaltyNumber": "12345678901234",
      |            "penaltyOrder": "01",
      |            "penaltyCategory": "P",
      |            "penaltyStatus": "ACTIVE",
      |            "fapIndicator": "X",
      |            "penaltyCreationDate": "2024-11-14",
      |            "triggeringProcess": "DUNN",
      |            "penaltyExpiryDate": "2026-11-14",
      |            "expiryReason": "FAP",
      |            "communicationsDate": "2024-11-15",
      |            "lateSubmissions": [
      |              {
      |                "lateSubmissionID": "1054",
      |                "incomeSource": "Income Tax Liability",
      |                "taxPeriod": "24C3",
      |                "taxReturnStatus": "Fulfilled",
      |                "taxPeriodStartDate": "2024-07-01",
      |                "taxPeriodEndDate": "2024-09-30",
      |                "taxPeriodDueDate": "2024-11-07",
      |                "returnReceiptDate": "2024-11-13"
      |              }
      |            ],
      |            "appealInformation": [
      |              {
      |                "appealStatus": "A",
      |                "appealLevel": "02",
      |                "appealDescription": "Reason to appeal"
      |              }
      |            ],
      |            "chargeReference": "XP001286394838",
      |            "chargeAmount": 543.29,
      |            "chargeOutstandingAmount": 123.26,
      |            "chargeDueDate": "2024-12-04"
      |          }
      |        ]
      |      },
      |      "lpp": {
      |        "lppDetails": [
      |          {
      |            "principalChargeReference": "XV123451234512",
      |            "supplement": true,
      |            "penaltyCategory": "LPP1",
      |            "penaltyStatus": "P",
      |            "penaltyAmountAccruing": 99.99,
      |            "penaltyAmountPosted": 1001.45,
      |            "penaltyAmountPaid": 1001.45,
      |            "penaltyAmountOutstanding": 99.99,
      |            "lpp1LRCalculationAmt": 99.99,
      |            "lpp1LRDays": "15",
      |            "lpp1LRPercentage": 2,
      |            "lpp1HRCalculationAmt": 99.99,
      |            "lpp1HRDays": "31",
      |            "lpp1HRPercentage": 2,
      |            "lpp2Days": "31",
      |            "lpp2Percentage": 4,
      |            "penaltyChargeCreationDate": "2024-12-08",
      |            "communicationsDate": "2024-12-09",
      |            "penaltyChargeReference": "XP298765432109",
      |            "penaltyChargeDueDate": "2025-01-08",
      |            "appealInformation": [
      |              {
      |                "appealStatus": "A",
      |                "appealLevel": "02",
      |                "appealDescription": "Reason to appeal"
      |              }
      |            ],
      |            "principalChargeDocNumber": "123456789012",
      |            "principalChargeMainTr": "4700",
      |            "principalChargeSubTr": "1174",
      |            "principalChargeBillingFrom": "2024-07-01",
      |            "principalChargeBillingTo": "2024-09-30",
      |            "principalChargeDueDate": "2024-11-07",
      |            "principalChargeLatestClearing": "2025-02-15",
      |            "timeToPay": [
      |              {
      |                "ttpStartDate": "2024-12-15",
      |                "ttpEndDate": "2025-06-15"
      |              }
      |            ]
      |          }
      |        ],
      |        "manualLPPIndicator": true
      |      }
      |    }
      |  }
      |}
      |""".stripMargin
  )

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |  "totalisations": {
      |    "lateSubmissionPenaltyTotalValue": 200,
      |    "penalisedPrincipalTotal": 2000,
      |    "latePaymentPenaltyPostedTotal": 165.25,
      |    "latePaymentPenaltyEstimateTotal": 15.26
      |  },
      |  "lateSubmissionPenalty": {
      |    "summary": {
      |      "activePenaltyPoints": 10,
      |      "inactivePenaltyPoints": 12,
      |      "periodOfComplianceAchievement": "2025-11-30",
      |      "regimeThreshold": 10,
      |      "penaltyChargeAmount": 684.25
      |    },
      |    "details": [
      |      {
      |        "penaltyNumber": "12345678901234",
      |        "penaltyOrder": "01",
      |        "penaltyCategory": "point",
      |        "penaltyStatus": "active",
      |        "frequencyAdjustmentPointIndicator": true,
      |        "penaltyCreationDate": "2024-11-14",
      |        "penaltyExpiryDate": "2026-11-14",
      |        "expiryReason": "submission-frequency-change",
      |        "communicationsDate": "2024-11-15",
      |        "lateSubmissions": [
      |          {
      |            "lateSubmissionId": "1054",
      |            "incomeSource": "Income Tax Liability",
      |            "taxPeriod": "24C3",
      |            "taxReturnStatus": "fulfilled",
      |            "taxPeriodStartDate": "2024-07-01",
      |            "taxPeriodEndDate": "2024-09-30",
      |            "taxPeriodDueDate": "2024-11-07",
      |            "returnReceiptDate": "2024-11-13"
      |          }
      |        ],
      |        "appealInformation": [
      |          {
      |            "status": "under-appeal",
      |            "level": "appeal-first-tier-tribunal",
      |            "description": "Reason to appeal"
      |          }
      |        ],
      |        "chargeReference": "XP001286394838",
      |        "chargeAmount": 543.29,
      |        "chargeOutstandingAmount": 123.26,
      |        "chargeDueDate": "2024-12-04"
      |      }
      |    ]
      |  },
      |  "latePaymentPenalty": {
      |    "details": [
      |      {
      |        "principalChargeReference": "XV123451234512",
      |        "penaltyCategory": "lpp1",
      |        "penaltyStatus": "posted",
      |        "penaltyAmountAccruing": 99.99,
      |        "penaltyAmountPosted": 1001.45,
      |        "penaltyAmountPaid": 1001.45,
      |        "penaltyAmountOutstanding": 99.99,
      |        "latePaymentPenalty1LowerRateCalculationAmount": 99.99,
      |        "latePaymentPenalty1LowerRatePercentage": 2,
      |        "latePaymentPenalty1HigherRateCalculationAmount": 99.99,
      |        "latePaymentPenalty1HigherRatePercentage": 2,
      |        "latePaymentPenalty2Days": 31,
      |        "latePaymentPenalty2Percentage": 4,
      |        "penaltyChargeCreationDate": "2024-12-08",
      |        "communicationsDate": "2024-12-09",
      |        "penaltyChargeReference": "XP298765432109",
      |        "penaltyChargeDueDate": "2025-01-08",
      |        "appealInformation": [
      |          {
      |            "status": "under-appeal",
      |            "level": "appeal-first-tier-tribunal",
      |            "description": "Reason to appeal"
      |          }
      |        ],
      |        "principalChargeDocNumber": "123456789012",
      |        "principalChargeBillingFrom": "2024-07-01",
      |        "principalChargeBillingTo": "2024-09-30",
      |        "principalChargeDueDate": "2024-11-07",
      |        "principalChargeLatestClearing": "2025-02-15",
      |        "timeToPay": [
      |          {
      |            "startDate": "2024-12-15",
      |            "endDate": "2025-06-15"
      |          }
      |        ]
      |      }
      |    ]
      |  }
      |}
      |""".stripMargin
  )

  val lateSubmissionsDownstreamJson: JsValue = Json.parse(
    """
      |{
      |  "lateSubmissionID": "1054",
      |  "incomeSource": "Income Tax Liability",
      |  "taxPeriod": "24C3",
      |  "taxReturnStatus": "Fulfilled",
      |  "taxPeriodStartDate": "2024-07-01",
      |  "taxPeriodEndDate": "2024-09-30",
      |  "taxPeriodDueDate": "2024-11-07",
      |  "returnReceiptDate": "2024-11-13"
      |}
      |""".stripMargin
  )

  val lateSubmissionsMtdJson: JsValue = Json.parse(
    """
      |{
      |  "lateSubmissionId": "1054",
      |  "incomeSource": "Income Tax Liability",
      |  "taxPeriod": "24C3",
      |  "taxReturnStatus": "fulfilled",
      |  "taxPeriodStartDate": "2024-07-01",
      |  "taxPeriodEndDate": "2024-09-30",
      |  "taxPeriodDueDate": "2024-11-07",
      |  "returnReceiptDate": "2024-11-13"
      |}
      |""".stripMargin
  )

}
