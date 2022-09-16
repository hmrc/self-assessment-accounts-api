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
import v2.models.response.retrieveBalanceAndTransactions.{FinancialDetails, FinancialDetailsItem}

trait FinancialDetailsFixture extends FinancialDetailsItemFixture {

  private val taxYear: TaxYear = TaxYear("2022")

  val financialDetailsFullObject: FinancialDetails = FinancialDetails(
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

  val financialDetailsNoMainTypeObject: FinancialDetails = FinancialDetails(
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

  val downstreamFinancialDetailsFullJson: JsValue = Json.parse(
    s"""
       |{
       |      "taxYear": "${taxYear.asDownstream}",
       |      "documentId": "${financialDetailsFullObject.documentId}",
       |      "chargeType": "${financialDetailsFullObject.chargeType.get}",
       |      "mainType": "$mainTypeDownstream",
       |      "periodKey": "13RL",
       |      "periodKeyDescription": "abcde",
       |      "taxPeriodFrom": "${financialDetailsFullObject.taxPeriodFrom.get}",
       |      "taxPeriodTo": "${financialDetailsFullObject.taxPeriodTo.get}",
       |      "businessPartner": "6622334455",
       |      "contractAccountCategory": "${financialDetailsFullObject.contractAccountCategory.get}",
       |      "contractAccount": "${financialDetailsFullObject.contractAccount.get}",
       |      "contractObjectType": "ABCD",
       |      "contractObject": "00000003000000002757",
       |      "sapDocumentNumber": "${financialDetailsFullObject.documentNumber.get}",
       |      "sapDocumentNumberItem": "${financialDetailsFullObject.documentNumberItem.get}",
       |      "chargeReference": "${financialDetailsFullObject.chargeReference.get}",
       |      "mainTransaction": "${financialDetailsFullObject.mainTransaction.get}",
       |      "subTransaction": "${financialDetailsFullObject.subTransaction.get}",
       |      "originalAmount": ${financialDetailsFullObject.originalAmount.get},
       |      "outstandingAmount": ${financialDetailsFullObject.outstandingAmount.get},
       |      "clearedAmount": ${financialDetailsFullObject.clearedAmount.get},
       |      "accruedInterest": ${financialDetailsFullObject.accruedInterest.get},
       |      "items":  [$financialDetailsItemDownstreamJson]
       |}
       |""".stripMargin
  )

  val downstreamFinancialDetailsMismatchedMainTypeJson: JsValue = Json.parse(s"""
       |{
       |      "taxYear": "${taxYear.asDownstream}",
       |      "documentId": "${financialDetailsFullObject.documentId}",
       |      "mainType": "0000",
       |      "items":  [$financialDetailsItemDownstreamJson]
       |}
       |""".stripMargin)

  val downstreamFinancialDetailsMissingMainTypeJson: JsValue = Json.parse(s"""
       |{
       |      "taxYear": "${taxYear.asDownstream}",
       |      "documentId": "${financialDetailsFullObject.documentId}",
       |      "items":  [$financialDetailsItemDownstreamJson]
       |}
       |""".stripMargin)

  val mtdFinancialDetailsFullJson: JsValue = Json.parse(
    s"""
       |  {
       |      "taxYear":"${taxYear.asMtd}",
       |      "documentId": "${financialDetailsFullObject.documentId}",
       |      "chargeType": "${financialDetailsFullObject.chargeType.get}",
       |      "mainType": "${financialDetailsFullObject.mainType.get}",
       |      "taxPeriodFrom": "${financialDetailsFullObject.taxPeriodFrom.get}",
       |      "taxPeriodTo": "${financialDetailsFullObject.taxPeriodTo.get}",
       |      "contractAccountCategory":"${financialDetailsFullObject.contractAccountCategory.get}",
       |      "contractAccount":"${financialDetailsFullObject.contractAccount.get}",
       |      "documentNumber": "${financialDetailsFullObject.documentNumber.get}",
       |      "documentNumberItem": "${financialDetailsFullObject.documentNumberItem.get}",
       |      "chargeReference": "${financialDetailsFullObject.chargeReference.get}",
       |      "mainTransaction": "${financialDetailsFullObject.mainTransaction.get}",
       |      "subTransaction": "${financialDetailsFullObject.subTransaction.get}",
       |      "originalAmount": ${financialDetailsFullObject.originalAmount.get},
       |      "outstandingAmount": ${financialDetailsFullObject.outstandingAmount.get},
       |      "clearedAmount": ${financialDetailsFullObject.clearedAmount.get},
       |      "accruedInterest": ${financialDetailsFullObject.accruedInterest.get},
       |      "items": [$financialDetailsItemMtdJson]
       | }
       |""".stripMargin
  )

  val mtdFinancialDetailsNoMainTypeJson: JsValue = Json.parse(
    s"""
       | {
       |   "taxYear":"${taxYear.asMtd}",
       |   "documentId": "${financialDetailsFullObject.documentId}",
       |   "items": [$financialDetailsItemMtdJson]
       | }
       |""".stripMargin
  )

}
