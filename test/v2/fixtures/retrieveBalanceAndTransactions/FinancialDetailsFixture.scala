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

object FinancialDetailsFixture extends FinancialDetailsItemFixture {

  private val taxYear: TaxYear = TaxYear("2022")

  val financialDetails: FinancialDetails = FinancialDetails(
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

  val financialDetailsNoMainType: FinancialDetails = FinancialDetails(
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

  val financialDetailsDownstreamResponseJson: JsValue = Json.parse(
    s"""
       |{
       |      "taxYear": "${taxYear.asDownstream}",
       |      "documentId": "${financialDetails.documentId}",
       |      "chargeType": "${financialDetails.chargeType.get}",
       |      "mainType": "${mainTypeDownstream}",
       |      "periodKey": "13RL",
       |      "periodKeyDescription": "abcde",
       |      "taxPeriodFrom": "${financialDetails.taxPeriodFrom.get}",
       |      "taxPeriodTo": "${financialDetails.taxPeriodTo.get}",
       |      "businessPartner": "6622334455",
       |      "contractAccountCategory": "${financialDetails.contractAccountCategory.get}",
       |      "contractAccount": "${financialDetails.contractAccount.get}",
       |      "contractObjectType": "ABCD",
       |      "contractObject": "00000003000000002757",
       |      "sapDocumentNumber": "${financialDetails.documentNumber.get}",
       |      "sapDocumentNumberItem": "${financialDetails.documentNumberItem.get}",
       |      "chargeReference": "${financialDetails.chargeReference.get}",
       |      "mainTransaction": "${financialDetails.mainTransaction.get}",
       |      "subTransaction": "${financialDetails.subTransaction.get}",
       |      "originalAmount": ${financialDetails.originalAmount.get},
       |      "outstandingAmount": ${financialDetails.outstandingAmount.get},
       |      "clearedAmount": ${financialDetails.clearedAmount.get},
       |      "accruedInterest": ${financialDetails.accruedInterest.get},
       |      "items":  [
       |          $financialDetailsItemDownstreamJson
       |      ]
       |}
       |""".stripMargin
  )

  val financialDetailsMismatchedMainTypeDownstreamResponseJson: JsValue = Json.parse(s"""
       |{
       |      "taxYear": "${taxYear.asDownstream}",
       |      "documentId": "${financialDetails.documentId}",
       |      "mainType": "0000",
       |      "items":  [$financialDetailsItemDownstreamJson]
       |}
       |""".stripMargin)

  val financialDetailsMissingMainTypeDownstreamResponseJson: JsValue = Json.parse(s"""
       |{
       |      "taxYear": "${taxYear.asDownstream}",
       |      "documentId": "${financialDetails.documentId}",
       |      "items":  [$financialDetailsItemDownstreamJson]
       |}
       |""".stripMargin)

  val financialDetailsMtdResponseJson: JsValue = Json.parse(
    s"""
       |  {
       |      "taxYear": "${taxYear.asMtd}",
       |      "documentId": "${financialDetails.documentId}",
       |      "chargeType": "${financialDetails.chargeType.get}",
       |      "mainType": "${financialDetails.mainType.get}",
       |      "taxPeriodFrom": "${financialDetails.taxPeriodFrom.get}",
       |      "taxPeriodTo": "${financialDetails.taxPeriodTo.get}",
       |      "contractAccountCategory":"${financialDetails.contractAccountCategory.get}",
       |      "contractAccount":"${financialDetails.contractAccount.get}",
       |      "documentNumber": "${financialDetails.documentNumber.get}",
       |      "documentNumberItem": "${financialDetails.documentNumberItem.get}",
       |      "chargeReference": "${financialDetails.chargeReference.get}",
       |      "mainTransaction": "${financialDetails.mainTransaction.get}",
       |      "subTransaction": "${financialDetails.subTransaction.get}",
       |      "originalAmount": ${financialDetails.originalAmount.get},
       |      "outstandingAmount": ${financialDetails.outstandingAmount.get},
       |      "clearedAmount": ${financialDetails.clearedAmount.get},
       |      "accruedInterest": ${financialDetails.accruedInterest.get},
       |      "items": [$financialDetailsItemMtdJson]
       | }
       |""".stripMargin
  )

  val financialDetailsNoMainTypeMtdResponseJson: JsValue = Json.parse(
    s"""
       | {
       |   "taxYear":"${taxYear.asMtd}",
       |   "documentId": "${financialDetails.documentId}",
       |   "items": [$financialDetailsItemMtdJson]
       | }
       |""".stripMargin
  )

}
