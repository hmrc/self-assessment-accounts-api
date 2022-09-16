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

import play.api.libs.json._
import v2.models.response.retrieveBalanceAndTransactions.{DocumentDetails, LastClearing, LatePaymentInterest, ReducedCharge}

trait DocumentDetailsFixture {

  // MTD model:

  val lastClearing: LastClearing = LastClearing(
    lastClearingDate = Some("2018-04-05"),
    lastClearingReason = Some("Incoming Payment"),
    lastClearedAmount = Some(5009.99)
  )

  val latePaymentInterest: LatePaymentInterest = LatePaymentInterest(
    latePaymentInterestId = Some("1234567890123456"),
    accruingInterestAmount = Some(5009.99),
    interestRate = Some(1.23),
    interestStartDate = Some("2020-04-01"),
    interestEndDate = Some("2020-04-05"),
    interestAmount = Some(5009.99),
    interestDunningLockAmount = Some(5009.99),
    interestOutstandingAmount = Some(5009.99)
  )

  val reducedCharge: ReducedCharge = ReducedCharge(
    chargeType = Some("???"),
    documentNumber = Some("???"),
    amendmentDate = Some("2018-04-05"),
    taxYear = Some("2018")
  )

  val documentDetails: DocumentDetails = DocumentDetails(
    taxYear = Some("2020-21"),
    documentId = "1455",
    formBundleNumber = Some("88888888"),
    creditReason = Some("Voluntary Payment"),
    documentDate = "2018-04-05",
    documentText = Some("ITSA- Bal Charge"),
    documentDueDate = "2021-04-05",
    documentDescription = Some("ITSA- POA 1"),
    originalAmount = 5009.99,
    outstandingAmount = 5009.99,
    lastClearing = Some(lastClearing),
    isStatistical = true,
    informationCode = Some("Coding Out"),
    paymentLot = Some("AB1023456789"),
    paymentLotItem = Some("000001"),
    effectiveDateOfPayment = Some("2021-04-05"),
    latePaymentInterest = Some(latePaymentInterest),
    amountCodedOut = Some(5009.99),
    reducedCharge = Some(reducedCharge)
  )

  val lastClearingJson: JsValue = Json.parse("""
       |{
       |  "lastClearingDate": "2018-04-05",
       |  "lastClearingReason": "Incoming Payment",
       |  "lastClearedAmount": 5009.99
       |}
       |""".stripMargin)

  val latePaymentInterestJson: JsValue = Json.parse("""
       |{
       |  "latePaymentInterestId": "1234567890123456",
       |  "accruingInterestAmount": 5009.99,
       |  "interestRate": 1.23,
       |  "interestStartDate": "2020-04-01",
       |  "interestEndDate": "2020-04-05",
       |  "interestAmount": 5009.99,
       |  "interestDunningLockAmount": 5009.99,
       |  "interestOutstandingAmount": 5009.99
       |}
       |""".stripMargin)

  val reducedChargeJson: JsValue = Json.parse("""
       |{
       |  "chargeType": "???",
       |  "documentNumber": "???",
       |  "amendmentDate": "2018-04-05",
       |  "taxYear": "2018"
       |}
       |""".stripMargin)

  val mtdDocumentDetailsJson: JsValue =
    Json.parse(s"""
       |{
       |  "taxYear": "2020-21",
       |  "documentId": "1455",
       |  "formBundleNumber": "88888888",
       |  "creditReason": "Voluntary Payment",
       |  "documentDate": "2018-04-05",
       |  "documentText": "ITSA- Bal Charge",
       |  "documentDueDate": "2021-04-05",
       |  "documentDescription": "ITSA- POA 1",
       |  "originalAmount": 5009.99,
       |  "outstandingAmount": 5009.99,
       |  "lastClearing": $lastClearingJson,
       |  "isStatistical": true,
       |  "informationCode": "Coding Out",
       |  "paymentLot": "AB1023456789",
       |  "paymentLotItem": "000001",
       |  "effectiveDateOfPayment": "2021-04-05",
       |  "latePaymentInterest": $latePaymentInterestJson,
       |  "amountCodedOut": 5009.99,
       |  "reducedCharge": $reducedChargeJson
       |}
       |""".stripMargin)

  // Downstream model:

  def newDownstreamDocumentDetailsJson(taxYear: String): JsValue = Json.parse(s"""
       |{
       |  "taxYear": "$taxYear",
       |  "taxYearReducedCharge": "2018",
       |  "documentId": "1455",
       |  "documentNumberReducedCharge": "???",
       |  "formBundleNumber": "88888888",
       |  "documentDate": "2018-04-05",
       |  "documentText": "ITSA- Bal Charge",
       |  "effectiveDateOfPayment": "2021-04-05",
       |  "documentDueDate": "2021-04-05",
       |  "documentDescription": "ITSA- POA 1",
       |  "totalAmount": 5009.99,
       |  "documentOutstandingAmount": 5009.99,
       |  "lastClearingDate": "2018-04-05",
       |  "lastClearingReason": "Incoming Payment",
       |  "lastClearedAmount": 5009.99,
       |  "statisticalFlag": true,
       |  "informationCode": "i",
       |  "paymentLot": "AB1023456789",
       |  "paymentLotItem": "000001",
       |  "accruingInterestAmount": 5009.99,
       |  "amendmentDateReducedCharge": "2018-04-05",
       |  "amountCodedOut": 5009.99,
       |  "chargeTypeReducedCharge": "???",
       |  "creditReason": "Voluntary Payment",
       |  "interestRate": 1.23,
       |  "interestFromDate": "2020-04-01",
       |  "interestEndDate": "2020-04-05",
       |  "latePaymentInterestID": "1234567890123456",
       |  "lpiWithDunningLock": 5009.99,
       |  "latePaymentInterestAmount": 5009.99,
       |  "interestOutstandingAmount": 5009.99
       |}
       |""".stripMargin)

  val downstreamDocumentDetailsJson: JsValue = newDownstreamDocumentDetailsJson("2021")

}
