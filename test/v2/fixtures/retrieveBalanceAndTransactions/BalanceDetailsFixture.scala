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

object BalanceDetailsFixture {
  val balancePerYearObject: BalancePerYear = BalancePerYear(Some("2022"), Some(123.45))

  val balanceDetailsObject: BalanceDetails = BalanceDetails(
    payableAmount = 124.20,
    payableDueDate = Some("2022-03-03"),
    pendingChargeDueAmount = 0.12,
    pendingChargeDueDate = Some("2022-01-01"),
    overdueAmount = 12.65,
    earliestPaymentDateOverDue = Some("2022-05-05"),
    totalBalance = 1263.12,
    amountCodedOut = Some(12.12),
    bcdBalancePerYear = Some(Seq(balancePerYearObject)),
    totalBcdBalance = Some(99.10),
    unallocatedCredit = Some(8.56),
    allocatedCredit = Some(12.34),
    totalCredit = Some(8.34),
    firstPendingAmountRequested = Some(3.45),
    secondPendingAmountRequested = Some(13.45),
    availableCredit = Some(235.99)
  )

  val downstreamDetailSingleYearJson: JsValue = Json.parse(
    s"""
       |{
       |   "taxYear": "${balancePerYearObject.taxYear.get}",
       |   "amount": ${balancePerYearObject.bcdAmount.get}
       |}
       |""".stripMargin
  )

  val mtdDetailSingleYearJson: JsValue = Json.parse(
    s"""
       |{
       |   "taxYear": "${balancePerYearObject.taxYear.get}",
       |   "bcdAmount": ${balancePerYearObject.bcdAmount.get}
       |}
       |""".stripMargin
  )

  val downstreamResponseJson: JsValue = Json.parse(
    s"""
       |{
       |    "balanceDueWithin30Days": ${balanceDetailsObject.payableAmount},
       |    "nextPaymentDateForChargesDueIn30Days": "${balanceDetailsObject.payableDueDate.get}",
       |    "balanceNotDueIn30Days": ${balanceDetailsObject.pendingChargeDueAmount},
       |    "nextPaymentDateBalanceNotDue": "${balanceDetailsObject.pendingChargeDueDate.get}",
       |    "overDueAmount": ${balanceDetailsObject.overdueAmount},
       |    "earliestPaymentDateOverDue": "${balanceDetailsObject.earliestPaymentDateOverDue.get}",
       |    "totalBalance": ${balanceDetailsObject.totalBalance},
       |    "amountCodedOut": ${balanceDetailsObject.amountCodedOut.get},
       |    "bcdBalancePerYear": [
       |      $downstreamDetailSingleYearJson
       |    ],    
       |    "totalBCDBalance": ${balanceDetailsObject.totalBcdBalance.get},
       |    "unallocatedCredit": ${balanceDetailsObject.unallocatedCredit.get},
       |    "allocatedCredit": ${balanceDetailsObject.allocatedCredit.get},
       |    "totalCredit": ${balanceDetailsObject.totalCredit.get},
       |    "firstPendingAmountRequested": ${balanceDetailsObject.firstPendingAmountRequested.get},
       |    "secondPendingAmountRequested": ${balanceDetailsObject.secondPendingAmountRequested.get},
       |    "availableCredit": ${balanceDetailsObject.availableCredit.get}
       |  }
       |""".stripMargin
  )

  val mtdResponseJson: JsValue = Json.parse(
    s"""
       |{
       |    "payableAmount": ${balanceDetailsObject.payableAmount},
       |    "payableDueDate": "${balanceDetailsObject.payableDueDate.get}",
       |    "pendingChargeDueAmount": ${balanceDetailsObject.pendingChargeDueAmount},
       |    "pendingChargeDueDate": "${balanceDetailsObject.pendingChargeDueDate.get}",
       |    "overdueAmount": ${balanceDetailsObject.overdueAmount},
       |    "earliestPaymentDateOverDue": "${balanceDetailsObject.earliestPaymentDateOverDue.get}",
       |    "totalBalance": ${balanceDetailsObject.totalBalance},
       |    "amountCodedOut": ${balanceDetailsObject.amountCodedOut.get},
       |    "bcdBalancePerYear": [
       |      $mtdDetailSingleYearJson
       |    ],      
       |    "totalBcdBalance": ${balanceDetailsObject.totalBcdBalance.get},
       |    "unallocatedCredit": ${balanceDetailsObject.unallocatedCredit.get},
       |    "allocatedCredit": ${balanceDetailsObject.allocatedCredit.get},
       |    "totalCredit": ${balanceDetailsObject.totalCredit.get},
       |    "firstPendingAmountRequested": ${balanceDetailsObject.firstPendingAmountRequested.get},
       |    "secondPendingAmountRequested": ${balanceDetailsObject.secondPendingAmountRequested.get},
       |    "availableCredit": ${balanceDetailsObject.availableCredit.get}
       |  }
       |""".stripMargin
  )

}
