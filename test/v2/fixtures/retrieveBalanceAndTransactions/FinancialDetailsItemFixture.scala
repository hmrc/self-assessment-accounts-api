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
import v2.models.response.retrieveBalanceAndTransactions.FinancialDetailsItem

trait FinancialDetailsItemFixture {

  val financialDetailsItemModel: FinancialDetailsItem = FinancialDetailsItem(
    subItem = Some("subItem"),
    dueDate = Some("2022-02-02"),
    amount = Some(1.23),
    clearingDate = Some("2021-01-01"),
    clearingReason = Some("01"),
    outgoingPaymentMethod = Some("Repayment to Card"),
    paymentLock = Some("Additional Security Checks"),
    clearingLock = Some("No Reallocation"),
    interestLock = Some("interestLock"),
    dunningLock = Some("dunningLock"),
    isReturn = Some(true),
    paymentReference = Some("paymentReference"),
    paymentAmount = Some(2.23),
    paymentMethod = Some("paymentMethod"),
    paymentLot = Some("paymentLot"),
    paymentLotItem = Some("paymentLotItem"),
    isStatistical = Some(true),
    returnReason = Some("returnReason")
  )

  val financialDetailsItemModelEmpty: FinancialDetailsItem =
    FinancialDetailsItem(None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None, None)

  val financialDetailsItemMtdJson: JsValue =
    Json.parse("""
      |{
      |  "subItem": "subItem",
      |  "dueDate": "2022-02-02",
      |  "amount": 1.23,
      |  "clearingDate": "2021-01-01",
      |  "clearingReason": "01",
      |  "outgoingPaymentMethod": "Repayment to Card",
      |  "paymentLock": "Additional Security Checks",
      |  "clearingLock": "No Reallocation",
      |  "interestLock": "interestLock",
      |  "dunningLock": "dunningLock",
      |  "isReturn": true,
      |  "paymentReference": "paymentReference",         
      |  "paymentAmount": 2.23,
      |  "paymentMethod": "paymentMethod",
      |  "paymentLot": "paymentLot",
      |  "paymentLotItem": "paymentLotItem",
      |  "isStatistical": true,
      |  "returnReason": "returnReason"
      |}
      |""".stripMargin)

  val financialDetailsItemDownstreamJson: JsValue =
    Json.parse("""
        |{
        |  "subItem": "subItem",
        |  "dueDate": "2022-02-02",
        |  "amount": 1.23,
        |  "clearingDate": "2021-01-01",
        |  "clearingReason": "01",
        |  "outgoingPaymentMethod": "A",
        |  "paymentLock": "K",
        |  "clearingLock": "0",
        |  "interestLock": "interestLock",
        |  "dunningLock": "dunningLock",
        |  "returnFlag": true,
        |  "paymentReference": "paymentReference",
        |  "promisetoPay": "X",
        |  "paymentAmount": 2.23,
        |  "paymentMethod": "paymentMethod",
        |  "paymentLot": "paymentLot",
        |  "paymentLotItem": "paymentLotItem",
        |  "clearingSAPDocument": "3350000253",
        |  "codingInitiationDate": "2021-01-11",
        |  "statisticalDocument": "G",
        |  "returnReason": "returnReason"
        |}
        |""".stripMargin)

}
