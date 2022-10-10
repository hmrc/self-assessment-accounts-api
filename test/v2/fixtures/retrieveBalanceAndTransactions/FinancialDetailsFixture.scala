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
import v2.models.response.retrieveBalanceAndTransactions.{ChargeDetail, FinancialDetails, FinancialDetailsItem}

object FinancialDetailsFixture extends FinancialDetailsItemFixture {

  private val taxYear: TaxYear = TaxYear("2022")

  val chargeDetailObject: ChargeDetail = ChargeDetail(
    documentId = "123456",
    mainTransaction = Option("1234"),
    mainTransactionDescription = Some("Income Tax Estimate"),
    subTransaction = Some("5678"),
    subTransactionDescription = Some("PAYE")
  )

  val financialDetailsFullObject: FinancialDetails = FinancialDetails(
    taxYear = taxYear.asMtd,
    chargeDetail = Some(chargeDetailObject),
    taxPeriodFrom = Some("2022-01-01"),
    taxPeriodTo = Some("2022-02-01"),
    contractAccount = Some("X"),
    documentNumber = Some("1040000872"),
    documentNumberItem = Some("XM01"),
    chargeReference = Some("XM002610011594"),
    originalAmount = Some(3453.99),
    outstandingAmount = Some(452.11),
    clearedAmount = Some(3411.01),
    accruedInterest = Some(123.78),
    items = Seq[FinancialDetailsItem](financialDetailsItemModel)
  )

  val downstreamFinancialDetailsFullJson: JsValue = Json.parse(
    s"""
       |{
       |      "taxYear": "${taxYear.asDownstream}",
       |      "documentId": "${chargeDetailObject.documentId}",
       |      "chargeType": "${chargeDetailObject.subTransactionDescription.get}",
       |      "mainType": "${chargeDetailObject.mainTransactionDescription.get}",
       |      "periodKey": "13RL",
       |      "periodKeyDescription": "abcde",
       |      "taxPeriodFrom": "${financialDetailsFullObject.taxPeriodFrom.get}",
       |      "taxPeriodTo": "${financialDetailsFullObject.taxPeriodTo.get}",
       |      "businessPartner": "6622334455",       
       |      "contractAccount": "${financialDetailsFullObject.contractAccount.get}",
       |      "contractObjectType": "ABCD",
       |      "contractObject": "00000003000000002757",
       |      "sapDocumentNumber": "${financialDetailsFullObject.documentNumber.get}",
       |      "sapDocumentNumberItem": "${financialDetailsFullObject.documentNumberItem.get}",
       |      "chargeReference": "${financialDetailsFullObject.chargeReference.get}",
       |      "mainTransaction": "${chargeDetailObject.mainTransaction.get}",
       |      "subTransaction": "${chargeDetailObject.subTransaction.get}",
       |      "originalAmount": ${financialDetailsFullObject.originalAmount.get},
       |      "outstandingAmount": ${financialDetailsFullObject.outstandingAmount.get},
       |      "clearedAmount": ${financialDetailsFullObject.clearedAmount.get},
       |      "accruedInterest": ${financialDetailsFullObject.accruedInterest.get},
       |      "items":  [$financialDetailsItemDownstreamJson]
       |}
       |""".stripMargin
  )

  val mtdChargeDetailJson: JsValue = Json.parse(s"""
      |{
      |"documentId": "${chargeDetailObject.documentId}",
      |"mainTransaction": "${chargeDetailObject.mainTransaction.get}",
      |"mainTransactionDescription": "${chargeDetailObject.mainTransactionDescription.get}",
      |"subTransaction": "${chargeDetailObject.subTransaction.get}",
      |"subTransactionDescription" : "${chargeDetailObject.subTransactionDescription.get}"
      |}
      |""".stripMargin)

  val mtdFinancialDetailsFullJson: JsValue = Json.parse(
    s"""
       |  {
       |      "taxYear":"${taxYear.asMtd}",
       |      "chargeDetail": ${mtdChargeDetailJson},
       |      "taxPeriodFrom": "${financialDetailsFullObject.taxPeriodFrom.get}",
       |      "taxPeriodTo": "${financialDetailsFullObject.taxPeriodTo.get}",
       |      "contractAccount":"${financialDetailsFullObject.contractAccount.get}",
       |      "documentNumber": "${financialDetailsFullObject.documentNumber.get}",
       |      "documentNumberItem": "${financialDetailsFullObject.documentNumberItem.get}",
       |      "chargeReference": "${financialDetailsFullObject.chargeReference.get}",
       |      "originalAmount": ${financialDetailsFullObject.originalAmount.get},
       |      "outstandingAmount": ${financialDetailsFullObject.outstandingAmount.get},
       |      "clearedAmount": ${financialDetailsFullObject.clearedAmount.get},
       |      "accruedInterest": ${financialDetailsFullObject.accruedInterest.get},
       |      "items": [$financialDetailsItemMtdJson]
       | }
       |""".stripMargin
  )

}
