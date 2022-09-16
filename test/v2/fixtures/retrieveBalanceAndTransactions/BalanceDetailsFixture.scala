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

package v2.fixtures.retrieveBalanceAndTransactions

import play.api.libs.json.{JsValue, Json}
import v2.models.response.retrieveBalanceAndTransactions.{BalanceDetails, BalancePerYear}

trait BalanceDetailsFixture {
  val balancePerYear: BalancePerYear = BalancePerYear(Some("2022"), Some(123.45))

  val balanceDetails: BalanceDetails = BalanceDetails(
    payableAmount = 124.20,
    payableDueDate = Some("2022-03-03"),
    pendingChargeDueAmount = 0.12,
    pendingChargeDueDate = Some("2022-01-01"),
    overdueAmount = 12.65,
    earliestPaymentDateOverDue = Some("2022-05-05"),
    totalBalance = 1263.12,
    amountCodedOut = Some(12.12),
    bcdBalancePerYear = Some(Seq(balancePerYear)),
    totalBcdBalance = Some(99.10),
    unallocatedCredit = Some(8.56),
    allocatedCredit = Some(12.34),
    totalCredit = Some(8.34),
    firstPendingAmountRequested = Some(3.45),
    secondPendingAmountRequested = Some(13.45),
    availableCredit = Some(235.99)
  )

  val balancePerYearDownstreamJson: JsValue = Json.parse(
    s"""
       |{
       |   "taxYear": "${balancePerYear.taxYear.get}",
       |   "amount": ${balancePerYear.bcdAmount.get}
       |}
       |""".stripMargin
  )

  val balancePerYearMtdJson: JsValue = Json.parse(
    s"""
       |{
       |   "taxYear": "${balancePerYear.taxYear.get}",
       |   "bcdAmount": ${balancePerYear.bcdAmount.get}
       |}
       |""".stripMargin
  )

  val balanceDetailsDownstreamJson: JsValue = Json.parse(
    s"""
       |{
       |    "balanceDueWithin30Days": ${balanceDetails.payableAmount},
       |    "nextPaymentDateForChargesDueIn30Days": "${balanceDetails.payableDueDate.get}",
       |    "balanceNotDueIn30Days": ${balanceDetails.pendingChargeDueAmount},
       |    "nextPaymentDateBalanceNotDue": "${balanceDetails.pendingChargeDueDate.get}",
       |    "overDueAmount": ${balanceDetails.overdueAmount},
       |    "earliestPaymentDateOverDue": "${balanceDetails.earliestPaymentDateOverDue.get}",
       |    "totalBalance": ${balanceDetails.totalBalance},
       |    "amountCodedOut": ${balanceDetails.amountCodedOut.get},
       |    "bcdBalancePerYear": [
       |      $balancePerYearDownstreamJson
       |    ],
       |    "totalBCDBalance": ${balanceDetails.totalBcdBalance.get},
       |    "unallocatedCredit": ${balanceDetails.unallocatedCredit.get},
       |    "allocatedCredit": ${balanceDetails.allocatedCredit.get},
       |    "totalCredit": ${balanceDetails.totalCredit.get},
       |    "firstPendingAmountRequested": ${balanceDetails.firstPendingAmountRequested.get},
       |    "secondPendingAmountRequested": ${balanceDetails.secondPendingAmountRequested.get},
       |    "availableCredit": ${balanceDetails.availableCredit.get}
       |  }
       |""".stripMargin
  )

  val balanceDetailsMtdJson: JsValue = Json.parse(
    s"""
       |{
       |    "payableAmount": ${balanceDetails.payableAmount},
       |    "payableDueDate": "${balanceDetails.payableDueDate.get}",
       |    "pendingChargeDueAmount": ${balanceDetails.pendingChargeDueAmount},
       |    "pendingChargeDueDate": "${balanceDetails.pendingChargeDueDate.get}",
       |    "overdueAmount": ${balanceDetails.overdueAmount},
       |    "earliestPaymentDateOverDue": "${balanceDetails.earliestPaymentDateOverDue.get}",
       |    "totalBalance": ${balanceDetails.totalBalance},
       |    "amountCodedOut": ${balanceDetails.amountCodedOut.get},
       |    "bcdBalancePerYear": [
       |      $balancePerYearMtdJson
       |    ],
       |    "totalBcdBalance": ${balanceDetails.totalBcdBalance.get},
       |    "unallocatedCredit": ${balanceDetails.unallocatedCredit.get},
       |    "allocatedCredit": ${balanceDetails.allocatedCredit.get},
       |    "totalCredit": ${balanceDetails.totalCredit.get},
       |    "firstPendingAmountRequested": ${balanceDetails.firstPendingAmountRequested.get},
       |    "secondPendingAmountRequested": ${balanceDetails.secondPendingAmountRequested.get},
       |    "availableCredit": ${balanceDetails.availableCredit.get}
       |  }
       |""".stripMargin
  )

}
