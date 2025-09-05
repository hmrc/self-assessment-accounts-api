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

package v3.retrieveBalanceAndTransactions.def1.model

import play.api.libs.json.{JsValue, Json}
import shared.models.domain.TaxYear
import v3.retrieveBalanceAndTransactions.def1.model.response.{ChargeDetail, FinancialDetails, FinancialDetailsItem}

object FinancialDetailsFixture extends FinancialDetailsItemFixture {

  private val taxYear: TaxYear = TaxYear.ending(2022)

  val chargeDetail: ChargeDetail = ChargeDetail(
    documentId = "123456",
    documentType = Option("1234"),
    documentTypeDescription = Some("Income Tax Estimate"),
    chargeType = Some("5678"),
    chargeTypeDescription = Some("PAYE")
  )

  val financialDetailsFull: FinancialDetails = financialDetailsWith(List(financialDetailsItem))

  val financialDetailsWithoutLocks: FinancialDetails = financialDetailsWith(List(financialDetailsItemWithoutLocks))

  private def financialDetailsWith(items: Seq[FinancialDetailsItem]): FinancialDetails = FinancialDetails(
    taxYear = taxYear.asMtd,
    chargeDetail = chargeDetail,
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
    items = items
  )

  val downstreamFinancialDetailsFullJson: JsValue = Json.parse(
    s"""
       |{
       |      "taxYear": "2022",
       |      "documentId": "123456",
       |      "chargeType": "PAYE",
       |      "mainType": "Income Tax Estimate",
       |      "periodKey": "13RL",
       |      "periodKeyDescription": "abcde",
       |      "taxPeriodFrom": "2022-01-01",
       |      "taxPeriodTo": "2022-02-01",
       |      "businessPartner": "6622334455",
       |      "contractAccountCategory": "02",
       |      "contractAccount": "X",
       |      "contractObjectType": "ABCD",
       |      "contractObject": "00000003000000002757",
       |      "sapDocumentNumber": "1040000872",
       |      "sapDocumentNumberItem": "XM01",
       |      "chargeReference": "XM002610011594",
       |      "mainTransaction": "1234",
       |      "subTransaction": "5678",
       |      "originalAmount": 3453.99,
       |      "outstandingAmount": 452.11,
       |      "clearedAmount": 3411.01,
       |      "accruedInterest": 123.78,
       |      "items":  [$financialDetailsItemDownstreamJson]
       |}
       |""".stripMargin
  )

  val downstreamFinancialDetailsFullHipJson: JsValue = Json.parse(
    s"""
       |{
       |      "taxYear": "2022",
       |      "documentID": "123456",
       |      "chargeType": "PAYE",
       |      "mainType": "Income Tax Estimate",
       |      "periodKey": "13RL",
       |      "periodKeyDescription": "abcde",
       |      "taxPeriodFrom": "2022-01-01",
       |      "taxPeriodTo": "2022-02-01",
       |      "businessPartner": "6622334455",
       |      "contractAccountCategory": "02",
       |      "contractAccount": "X",
       |      "contractObjectType": "ABCD",
       |      "contractObject": "00000003000000002757",
       |      "sapDocumentNumber": "1040000872",
       |      "sapDocumentNumberItem": "XM01",
       |      "chargeReference": "XM002610011594",
       |      "mainTransaction": "1234",
       |      "subTransaction": "5678",
       |      "originalAmount": 3453.99,
       |      "outstandingAmount": 452.11,
       |      "clearedAmount": 3411.01,
       |      "accruedInterest": 123.78,
       |      "items":  [$financialDetailsItemDownstreamHipJson]
       |}
       |""".stripMargin
  )

  val mtdChargeDetailJson: JsValue = Json.parse(s"""
      |{
      |"documentId": "123456",
      |"documentType": "1234",
      |"documentTypeDescription": "Income Tax Estimate",
      |"chargeType": "5678",
      |"chargeTypeDescription" : "PAYE"
      |}
      |""".stripMargin)

  val mtdFinancialDetailsFullJson: JsValue         = mtdFinancialDetailsWith(financialDetailsItemMtdJson)
  val mtdFinancialDetailsWithoutLocksJson: JsValue = mtdFinancialDetailsWith(financialDetailsItemWithoutLocksMtdJson)

  private def mtdFinancialDetailsWith(items: JsValue): JsValue = Json.parse(s"""
       |  {
       |      "taxYear":"2021-22",
       |      "chargeDetail": $mtdChargeDetailJson,
       |      "taxPeriodFrom": "2022-01-01",
       |      "taxPeriodTo": "2022-02-01",
       |      "contractAccount": "X",
       |      "documentNumber": "1040000872",
       |      "documentNumberItem": "XM01",
       |      "chargeReference": "XM002610011594",
       |      "originalAmount": 3453.99,
       |      "outstandingAmount": 452.11,
       |      "clearedAmount": 3411.01,
       |      "accruedInterest": 123.78,
       |      "items": [$items]
       | }
       |""".stripMargin)

}
