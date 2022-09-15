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
import v2.models.response.retrieveBalanceAndTransactions.{FinanceDetail, Item}

object FinanceDetailFixture {

  val financeDetailFullObject: FinanceDetail = FinanceDetail(
    taxYear = "2022",
    documentId = "123456",
    chargeType = Some("PAYE"),
    mainType = Some("Income Tax Estimate"),
    taxPeriodFrom = Some("2022-01-01"),
    taxPeriodTo = Some("2022-02-01"),
    contractAccountCategory = Some("02"),
    contractAccount = Some("X"),
    documentNumber = Some("1040000872"),
    documentNumberItem = Some("XM01"),
    chargeReference = Some("XM002610011594"),
    mainTransaction = Some("TR122"),
    subTransaction = Some("SUB221"),
    originalAmount = Some(3453.99),
    outstandingAmount = Some(452.11),
    clearedAmount = Some(3411.01),
    accruedInterest = Some(123.78),
    items = Seq[Item](Item("2022", "other"))
  )

  val financeDetailNoMainTypeObject: FinanceDetail = FinanceDetail(
    taxYear = "2022",
    documentId = "123456",
    chargeType = None,
    mainType = None,
    taxPeriodFrom = None,
    taxPeriodTo = None,
    contractAccountCategory = None,
    contractAccount = None,
    documentNumber = None,
    documentNumberItem = None,
    chargeReference = None,
    mainTransaction = None,
    subTransaction = None,
    originalAmount = None,
    outstandingAmount = None,
    clearedAmount = None,
    accruedInterest = None,
    items = Seq[Item](Item("2022", "other"))
  )

  val mainTypeDownstream: String = "3880"

  val downstreamFinanceDetailFullJson: JsValue = Json.parse(
    s"""
       |{
       |      "taxYear": "${financeDetailFullObject.taxYear}",
       |      "documentId": "${financeDetailFullObject.documentId}",
       |      "chargeType": "${financeDetailFullObject.chargeType.get}",
       |      "mainType": "${mainTypeDownstream}",
       |      "periodKey": "13RL",
       |      "periodKeyDescription": "abcde",
       |      "taxPeriodFrom": "${financeDetailFullObject.taxPeriodFrom.get}",
       |      "taxPeriodTo": "${financeDetailFullObject.taxPeriodTo.get}",
       |      "businessPartner": "6622334455",
       |      "contractAccountCategory": "${financeDetailFullObject.contractAccountCategory.get}",
       |      "contractAccount": "${financeDetailFullObject.contractAccount.get}",
       |      "contractObjectType": "ABCD",
       |      "contractObject": "00000003000000002757",
       |      "sapDocumentNumber": "${financeDetailFullObject.documentNumber.get}",
       |      "sapDocumentNumberItem": "${financeDetailFullObject.documentNumberItem.get}",
       |      "chargeReference": "${financeDetailFullObject.chargeReference.get}",
       |      "mainTransaction": "${financeDetailFullObject.mainTransaction.get}",
       |      "subTransaction": "${financeDetailFullObject.subTransaction.get}",
       |      "originalAmount": ${financeDetailFullObject.originalAmount.get},
       |      "outstandingAmount": ${financeDetailFullObject.outstandingAmount.get},
       |      "clearedAmount": ${financeDetailFullObject.clearedAmount.get},
       |      "accruedInterest": ${financeDetailFullObject.accruedInterest.get},
       |      "items":  [{"taxYear": "2022", "other":"other" }]
       |}
       |""".stripMargin
  )

  val downstreamFinanceDetailMismatchedMainTypeJson: JsValue = Json.parse(s"""
       |{
       |      "taxYear": "${financeDetailFullObject.taxYear}",
       |      "documentId": "${financeDetailFullObject.documentId}",
       |      "mainType": "0000",
       |      "items":  [{"taxYear": "2022", "other":"other" }]
       |}
       |""".stripMargin)

  val downstreamFinanceDetailMissingMainTypeJson: JsValue = Json.parse(s"""
       |{
       |      "taxYear": "${financeDetailFullObject.taxYear}",
       |      "documentId": "${financeDetailFullObject.documentId}",
       |      "mainType": "0000",
       |      "items":  [{"taxYear": "2022", "other":"other" }]
       |}
       |""".stripMargin)

  val mtdFinanceDetailFullJson: JsValue = Json.parse(
    s"""
       |  {
       |      "taxYear":"${financeDetailFullObject.taxYear}",
       |      "documentId": "${financeDetailFullObject.documentId}",
       |      "chargeType": "${financeDetailFullObject.chargeType.get}",
       |      "mainType": "${financeDetailFullObject.mainType.get}",
       |      "taxPeriodFrom": "${financeDetailFullObject.taxPeriodFrom.get}",
       |      "taxPeriodTo": "${financeDetailFullObject.taxPeriodTo.get}",
       |      "contractAccountCategory":"${financeDetailFullObject.contractAccountCategory.get}",
       |      "contractAccount":"${financeDetailFullObject.contractAccount.get}",
       |      "documentNumber": "${financeDetailFullObject.documentNumber.get}",
       |      "documentNumberItem": "${financeDetailFullObject.documentNumberItem.get}",
       |      "chargeReference": "${financeDetailFullObject.chargeReference.get}",
       |      "mainTransaction": "${financeDetailFullObject.mainTransaction.get}",
       |      "subTransaction": "${financeDetailFullObject.subTransaction.get}",
       |      "originalAmount": ${financeDetailFullObject.originalAmount.get},
       |      "outstandingAmount": ${financeDetailFullObject.outstandingAmount.get},
       |      "clearedAmount": ${financeDetailFullObject.clearedAmount.get},
       |      "accruedInterest": ${financeDetailFullObject.accruedInterest.get},
       |      "items": [{"taxYear": "2022", "other":"other"}]
       | }
       |""".stripMargin
  )

  val mtdFinanceDetailNoMainTypeJson: JsValue = Json.parse(
    s"""
       | {
       |   "taxYear":"${financeDetailFullObject.taxYear}",
       |   "documentId": "${financeDetailFullObject.documentId}",
       |   "items": [{"taxYear": "2022", "other":"other"}]
       | }
       |""".stripMargin
  )

}
