/*
 * Copyright 2023 HM Revenue & Customs
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

package v3.fixtures.retrieveBalanceAndTransactions

import api.models.domain.TaxYear
import play.api.libs.json.{JsValue, Json}
import v3.models.response.retrieveBalanceAndTransactions.{BalanceDetails, BalancePerYear}

object BalanceDetailsFixture {

  private val taxYear: TaxYear = TaxYear("2022")

  val balancePerYear: BalancePerYear = BalancePerYear(123.45, taxYear.asMtd)

  val balanceDetails: BalanceDetails = BalanceDetails(
    payableAmount = 124.20,
    payableDueDate = Some("2022-03-03"),
    pendingChargeDueAmount = 0.12,
    pendingChargeDueDate = Some("2022-01-01"),
    overdueAmount = 12.65,
    bcdBalancePerYear = List(balancePerYear),
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
    bcdBalancePerYear = Nil,
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
       |   "amount": 123.45,
       |   "taxYear": "2022"
       |}
       |""".stripMargin
  )
  val mtdDetailSingleYearJson: JsValue = Json.parse(
    s"""
       |{
       |   "bcdAmount": 123.45,
       |   "taxYear": "2021-22"
       |}
       |""".stripMargin
  )
  val balanceDetailsDownstreamResponseJson: JsValue = Json.parse(
    s"""
       |{
       |    "balanceDueWithin30Days": 124.20,
       |    "nextPaymentDateForChargesDueIn30Days": "2022-03-03",
       |    "balanceNotDueIn30Days": 0.12,
       |    "nextPaymentDateBalanceNotDue": "2022-01-01",
       |    "overDueAmount": 12.65,
       |    "earliestPaymentDateOverDue": "2022-05-05",
       |    "totalBalance": 1263.12,
       |    "bcdBalancePerYear": [
       |      $downstreamDetailSingleYearJson
       |    ],
       |    "amountCodedOut": 12.12,
       |    "totalBCDBalance": 99.10,
       |    "unallocatedCredit": 8.56,
       |    "allocatedCredit": 12.34,
       |    "totalCredit": 8.34,
       |    "firstPendingAmountRequested": 3.45,
       |    "secondPendingAmountRequested": 13.45,
       |    "availableCredit": 235.99
       |  }
       |""".stripMargin
  )
  val minimalBalanceDetailsDownstreamResponseJson: JsValue = Json.parse(
    s"""
       |{
       |    "balanceDueWithin30Days": 124.20,
       |    "balanceNotDueIn30Days": 0.12,
       |    "overDueAmount": 12.65,
       |    "totalBalance": 1263.12
       |  }
       |""".stripMargin
  )
  val balanceDetailsMtdResponseJson: JsValue = Json.parse(
    s"""
       |{
       |    "payableAmount": 124.20,
       |    "payableDueDate": "2022-03-03",
       |    "pendingChargeDueAmount": 0.12,
       |    "pendingChargeDueDate": "2022-01-01",
       |    "overdueAmount": 12.65,
       |    "bcdBalancePerYear": [
       |      $mtdDetailSingleYearJson
       |    ],
       |    "earliestPaymentDateOverdue": "2022-05-05",
       |    "totalBalance": 1263.12,
       |    "amountCodedOut": 12.12,
       |    "totalBcdBalance": 99.10,
       |    "unallocatedCredit": 8.56,
       |    "allocatedCredit": 12.34,
       |    "totalCredit": 8.34,
       |    "firstPendingAmountRequested": 3.45,
       |    "secondPendingAmountRequested": 13.45,
       |    "availableCredit": 235.99
       |  }
       |""".stripMargin
  )

}
