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

package v4.retrieveBalanceAndTransactions.def1.model

import play.api.libs.json.{JsObject, JsValue, Json}
import v4.retrieveBalanceAndTransactions.def1.model.response.{CodedOutStatus, FinancialDetailsItem}

trait FinancialDetailsItemFixture extends FinancialDetailsItemLocksFixture {

  val financialDetailsItem: FinancialDetailsItem = FinancialDetailsItem(
    itemId = Some("001"),
    dueDate = Some("2022-02-02"),
    amount = Some(1.23),
    clearingDate = Some("2021-01-01"),
    clearingReason = Some("Incoming Payment"),
    outgoingPaymentMethod = Some("Repayment to Card"),
    locks = Some(financialDetailsItemLocks),
    isReturn = Some(true),
    paymentReference = Some("paymentReference"),
    paymentAmount = Some(2.23),
    paymentMethod = Some("paymentMethod"),
    paymentLot = Some("paymentLot"),
    paymentLotItem = Some("paymentLotItem"),
    clearingSAPDocument = Some("clearingSAPDocument"),
    isChargeEstimate = Some(true),
    codedOutStatus = None
  )

  val financialDetailsItemHip: FinancialDetailsItem = financialDetailsItem.copy(codedOutStatus = Some(CodedOutStatus.`not-collected`))

  val financialDetailsItemWithoutLocks: FinancialDetailsItem = financialDetailsItem.copy(locks = None)

  val financialDetailsItemWithoutLocksHip: FinancialDetailsItem = financialDetailsItemHip.copy(locks = None)

  val financialDetailsItemEmpty: FinancialDetailsItem =
    FinancialDetailsItem(
      itemId = None,
      dueDate = None,
      amount = None,
      clearingDate = None,
      clearingReason = None,
      outgoingPaymentMethod = None,
      locks = None,
      isReturn = None,
      paymentReference = None,
      paymentAmount = None,
      paymentMethod = None,
      paymentLot = None,
      paymentLotItem = None,
      clearingSAPDocument = None,
      isChargeEstimate = None,
      codedOutStatus = None
    )

  val financialDetailsItemWithoutLocksMtdJson: JsValue =
    Json.parse(s"""
         |{
         |  "itemId": "001",
         |  "dueDate": "2022-02-02",
         |  "amount": 1.23,
         |  "clearingDate": "2021-01-01",
         |  "clearingReason": "Incoming Payment",
         |  "outgoingPaymentMethod": "Repayment to Card",
         |  "isReturn": true,
         |  "paymentReference": "paymentReference",
         |  "paymentAmount": 2.23,
         |  "paymentMethod": "paymentMethod",
         |  "paymentLot": "paymentLot",
         |  "paymentLotItem": "paymentLotItem",
         |  "clearingSAPDocument": "clearingSAPDocument",
         |  "isChargeEstimate": true
         |}
         |""".stripMargin)

  val financialDetailsItemWithoutLocksMtdJsonHip: JsValue =
    Json.parse(s"""
                  |{
                  |  "itemId": "001",
                  |  "dueDate": "2022-02-02",
                  |  "amount": 1.23,
                  |  "clearingDate": "2021-01-01",
                  |  "clearingReason": "Incoming Payment",
                  |  "outgoingPaymentMethod": "Repayment to Card",
                  |  "isReturn": true,
                  |  "paymentReference": "paymentReference",
                  |  "paymentAmount": 2.23,
                  |  "paymentMethod": "paymentMethod",
                  |  "paymentLot": "paymentLot",
                  |  "paymentLotItem": "paymentLotItem",
                  |  "clearingSAPDocument": "clearingSAPDocument",
                  |  "isChargeEstimate": true,
                  |  "codedOutStatus": "not-collected"
                  |}
                  |""".stripMargin)

  val financialDetailsItemMtdJson: JsValue =
    financialDetailsItemWithoutLocksMtdJson.as[JsObject] ++ Json.obj("locks" -> financialDetailsItemLocksMtdJson)

  val financialDetailsItemMtdJsonHip: JsValue =
    financialDetailsItemWithoutLocksMtdJsonHip.as[JsObject] ++ Json.obj("locks" -> financialDetailsItemLocksMtdJson)

  val financialDetailsItemDownstreamJson: JsValue =
    Json.parse("""
        |{
        |  "subItem": "001",
        |  "dueDate": "2022-02-02",
        |  "amount": 1.23,
        |  "clearingDate": "2021-01-01",
        |  "clearingReason": "Incoming Payment",
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
        |  "clearingSAPDocument": "clearingSAPDocument",
        |  "codingInitiationDate": "2021-01-11",
        |  "statisticalDocument": "Y",
        |  "returnReason": "returnReason"
        |}
        |""".stripMargin)

    val financialDetailsItemDownstreamHipJson: JsValue =
    Json.parse("""
        |{
        |  "subItem": "001",
        |  "dueDate": "2022-02-02",
        |  "amount": 1.23,
        |  "clearingDate": "2021-01-01",
        |  "clearingReason": "Incoming Payment",
        |  "outgoingPaymentMethod": "A",
        |  "paymentLock": "K",
        |  "clearingLock": "0",
        |  "interestLock": "interestLock",
        |  "dunningLock": "dunningLock",
        |  "returnFlag": "Y",
        |  "paymentReference": "paymentReference",
        |  "promisetoPay": "X",
        |  "paymentAmount": 2.23,
        |  "paymentMethod": "paymentMethod",
        |  "paymentLot": "paymentLot",
        |  "paymentLotItem": "paymentLotItem",
        |  "clearingSAPDocument": "clearingSAPDocument",
        |  "codingInitiationDate": "2021-01-11",
        |  "statisticalDocument": "Y",
        |  "codedOutStatus": "N",
        |  "returnReason": "returnReason"
        |}
        |""".stripMargin)

}
