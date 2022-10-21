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

import api.models.domain.TaxYear
import play.api.libs.json.{JsValue, Json}
import v2.models.response.retrieveBalanceAndTransactions.{BalanceDetails, BalancePerYear}

object BalanceDetailsFixture {

  private val taxYear: TaxYear = TaxYear("2022")

  val balancePerYearObject: BalancePerYear = BalancePerYear(Some(123.45), Some(taxYear.asMtd))

  val balanceDetails: BalanceDetails = BalanceDetails(
    payableAmount = 124.20,
    payableDueDate = Some("2022-03-03"),
    pendingChargeDueAmount = 0.12,
    pendingChargeDueDate = Some("2022-01-01"),
    overdueAmount = 12.65,
    bcdBalancePerYear = Some(Seq(balancePerYearObject)),
    earliestPaymentDateOverdue = Some("2022-05-05"),
    totalBalance = 1263.12,
    amountCodedOut = Some(12.12),
    totalBcdBalance = Some(99.10),
    unallocatedCredit = Some(8.56),
    allocatedCredit = Some(12.34),
    totalCredit = Some(8.34),
    firstPendingAmountRequested = Some(3.45),
    secondPendingAmountRequested = Some(13.45),
    availableCredit = Some(235.99)
  )

  val minimalBalanceDetails: BalanceDetails = BalanceDetails(
    payableAmount = 124.20,
    payableDueDate = None,
    pendingChargeDueAmount = 0.12,
    pendingChargeDueDate = None,
    overdueAmount = 12.65,
    bcdBalancePerYear = None,
    earliestPaymentDateOverdue = None,
    totalBalance = 1263.12,
    amountCodedOut = None,
    totalBcdBalance = None,
    unallocatedCredit = None,
    allocatedCredit = None,
    totalCredit = None,
    firstPendingAmountRequested = None,
    secondPendingAmountRequested = None,
    availableCredit = None
  )

  val downstreamDetailSingleYearJson: JsValue = Json.parse(
    s"""
       |{
       |   "amount": ${balancePerYearObject.bcdAmount.get},
       |   "taxYear": "${taxYear.asDownstream}"
       |}
       |""".stripMargin
  )

  val mtdDetailSingleYearJson: JsValue = Json.parse(
    s"""
       |{
       |   "bcdAmount": ${balancePerYearObject.bcdAmount.get},
       |   "taxYear": "${balancePerYearObject.taxYear.get}"
       |}
       |""".stripMargin
  )

  val balanceDetailsDownstreamResponseJson: JsValue = Json.parse(
    s"""
       |{
       |    "balanceDueWithin30Days": ${balanceDetails.payableAmount},
       |    "nextPaymentDateForChargesDueIn30Days": "${balanceDetails.payableDueDate.get}",
       |    "balanceNotDueIn30Days": ${balanceDetails.pendingChargeDueAmount},
       |    "nextPaymentDateBalanceNotDue": "${balanceDetails.pendingChargeDueDate.get}",
       |    "overDueAmount": ${balanceDetails.overdueAmount},
       |    "earliestPaymentDateOverDue": "${balanceDetails.earliestPaymentDateOverdue.get}",
       |    "totalBalance": ${balanceDetails.totalBalance},
       |    "bcdBalancePerYear": [
       |      $downstreamDetailSingleYearJson
       |    ],    
       |    "amountCodedOut": ${balanceDetails.amountCodedOut.get},
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

  val minimalBalanceDetailsDownstreamResponseJson: JsValue = Json.parse(
    s"""
       |{
       |    "balanceDueWithin30Days": ${balanceDetails.payableAmount},
       |    "balanceNotDueIn30Days": ${balanceDetails.pendingChargeDueAmount},
       |    "overDueAmount": ${balanceDetails.overdueAmount},
       |    "totalBalance": ${balanceDetails.totalBalance}
       |  }
       |""".stripMargin
  )

  val balanceDetailsMtdResponseJson: JsValue = Json.parse(
    s"""
       |{
       |    "payableAmount": ${balanceDetails.payableAmount},
       |    "payableDueDate": "${balanceDetails.payableDueDate.get}",
       |    "pendingChargeDueAmount": ${balanceDetails.pendingChargeDueAmount},
       |    "pendingChargeDueDate": "${balanceDetails.pendingChargeDueDate.get}",
       |    "overdueAmount": ${balanceDetails.overdueAmount},
       |    "bcdBalancePerYear": [
       |      $mtdDetailSingleYearJson
       |    ],
       |    "earliestPaymentDateOverdue": "${balanceDetails.earliestPaymentDateOverdue.get}",
       |    "totalBalance": ${balanceDetails.totalBalance},
       |    "amountCodedOut": ${balanceDetails.amountCodedOut.get},
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
