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
import v2.models.response.retrieveBalanceAndTransactions.{FinanceDetails, FinancialDetailsItem}

object FinanceDetailsFixture extends FinancialDetailsItemFixture {

  private val taxYear: TaxYear = TaxYear("2022")

  val financeDetailsFullObject: FinanceDetails = FinanceDetails(
    taxYear = taxYear.asMtd,
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
    items = Seq[FinancialDetailsItem](financialDetailsItemModel)
  )

  val financeDetailsNoMainTypeObject: FinanceDetails = FinanceDetails(
    taxYear = taxYear.asMtd,
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
    items = Seq[FinancialDetailsItem](financialDetailsItemModel)
  )

  val mainTypeDownstream: String = "3880"

  val downstreamFinanceDetailFullJson: JsValue = Json.parse(
    s"""
       |{
       |      "taxYear": "${taxYear.asDownstream}",
       |      "documentId": "${financeDetailsFullObject.documentId}",
       |      "chargeType": "${financeDetailsFullObject.chargeType.get}",
       |      "mainType": "${mainTypeDownstream}",
       |      "periodKey": "13RL",
       |      "periodKeyDescription": "abcde",
       |      "taxPeriodFrom": "${financeDetailsFullObject.taxPeriodFrom.get}",
       |      "taxPeriodTo": "${financeDetailsFullObject.taxPeriodTo.get}",
       |      "businessPartner": "6622334455",
       |      "contractAccountCategory": "${financeDetailsFullObject.contractAccountCategory.get}",
       |      "contractAccount": "${financeDetailsFullObject.contractAccount.get}",
       |      "contractObjectType": "ABCD",
       |      "contractObject": "00000003000000002757",
       |      "sapDocumentNumber": "${financeDetailsFullObject.documentNumber.get}",
       |      "sapDocumentNumberItem": "${financeDetailsFullObject.documentNumberItem.get}",
       |      "chargeReference": "${financeDetailsFullObject.chargeReference.get}",
       |      "mainTransaction": "${financeDetailsFullObject.mainTransaction.get}",
       |      "subTransaction": "${financeDetailsFullObject.subTransaction.get}",
       |      "originalAmount": ${financeDetailsFullObject.originalAmount.get},
       |      "outstandingAmount": ${financeDetailsFullObject.outstandingAmount.get},
       |      "clearedAmount": ${financeDetailsFullObject.clearedAmount.get},
       |      "accruedInterest": ${financeDetailsFullObject.accruedInterest.get},
       |      "items":  [$financialDetailsItemDownstreamJson]
       |}
       |""".stripMargin
  )

  val downstreamFinanceDetailMismatchedMainTypeJson: JsValue = Json.parse(s"""
       |{
       |      "taxYear": "${taxYear.asDownstream}",
       |      "documentId": "${financeDetailsFullObject.documentId}",
       |      "mainType": "0000",
       |      "items":  [$financialDetailsItemDownstreamJson]
       |}
       |""".stripMargin)

  val downstreamFinanceDetailMissingMainTypeJson: JsValue = Json.parse(s"""
       |{
       |      "taxYear": "${taxYear.asDownstream}",
       |      "documentId": "${financeDetailsFullObject.documentId}",
       |      "items":  [$financialDetailsItemDownstreamJson]
       |}
       |""".stripMargin)

  val mtdFinanceDetailFullJson: JsValue = Json.parse(
    s"""
       |  {
       |      "taxYear":"${taxYear.asMtd}",
       |      "documentId": "${financeDetailsFullObject.documentId}",
       |      "chargeType": "${financeDetailsFullObject.chargeType.get}",
       |      "mainType": "${financeDetailsFullObject.mainType.get}",
       |      "taxPeriodFrom": "${financeDetailsFullObject.taxPeriodFrom.get}",
       |      "taxPeriodTo": "${financeDetailsFullObject.taxPeriodTo.get}",
       |      "contractAccountCategory":"${financeDetailsFullObject.contractAccountCategory.get}",
       |      "contractAccount":"${financeDetailsFullObject.contractAccount.get}",
       |      "documentNumber": "${financeDetailsFullObject.documentNumber.get}",
       |      "documentNumberItem": "${financeDetailsFullObject.documentNumberItem.get}",
       |      "chargeReference": "${financeDetailsFullObject.chargeReference.get}",
       |      "mainTransaction": "${financeDetailsFullObject.mainTransaction.get}",
       |      "subTransaction": "${financeDetailsFullObject.subTransaction.get}",
       |      "originalAmount": ${financeDetailsFullObject.originalAmount.get},
       |      "outstandingAmount": ${financeDetailsFullObject.outstandingAmount.get},
       |      "clearedAmount": ${financeDetailsFullObject.clearedAmount.get},
       |      "accruedInterest": ${financeDetailsFullObject.accruedInterest.get},
       |      "items": [$financialDetailsItemMtdJson]
       | }
       |""".stripMargin
  )

  val mtdFinanceDetailNoMainTypeJson: JsValue = Json.parse(
    s"""
       | {
       |   "taxYear":"${taxYear.asMtd}",
       |   "documentId": "${financeDetailsFullObject.documentId}",
       |   "items": [$financialDetailsItemMtdJson]
       | }
       |""".stripMargin
  )

}
