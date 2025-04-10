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

import play.api.libs.json._
import v3.retrieveBalanceAndTransactions.def1.model.response.{DocumentDetails, LastClearing, LatePaymentInterest, ReducedCharge}

object DocumentDetailsFixture {

  val lastClearing: LastClearing = LastClearing(
    lastClearingDate = Some("2018-04-05"),
    lastClearingReason = Some("Incoming Payment"),
    lastClearedAmount = Some(3.99)
  )

  val latePaymentInterest: LatePaymentInterest = LatePaymentInterest(
    latePaymentInterestId = Some("1234567890123456"),
    accruingInterestAmount = Some(4.99),
    interestRate = Some(6.99),
    interestStartDate = Some("2020-04-01"),
    interestEndDate = Some("2020-04-05"),
    interestAmount = Some(8.99),
    interestDunningLockAmount = Some(7.99),
    interestOutstandingAmount = Some(9.99)
  )

  val reducedCharge: ReducedCharge = ReducedCharge(
    chargeType = Some("???"),
    documentNumber = Some("???"),
    amendmentDate = Some("2018-04-05"),
    taxYear = Some("2017-18")
  )

  val documentDetails: DocumentDetails = DocumentDetails(
    taxYear = Some("2020-21"),
    documentId = "1455",
    formBundleNumber = Some("88888888"),
    creditReason = Some("Voluntary Payment"),
    documentDate = "2018-04-05",
    documentText = Some("ITSA- Bal Charge"),
    documentDueDate = Some("2021-04-05"),
    documentDescription = Some("ITSA- POA 1"),
    originalAmount = 1.99,
    outstandingAmount = 2.99,
    lastClearing = Some(lastClearing),
    isChargeEstimate = true,
    isCodedOut = true,
    paymentLot = Some("AB1023456789"),
    paymentLotItem = Some("000001"),
    effectiveDateOfPayment = Some("2021-04-05"),
    latePaymentInterest = Some(latePaymentInterest),
    amountCodedOut = Some(5.99),
    reducedCharge = Some(reducedCharge),
    poaRelevantAmount = Some(5.99)
  )

  val documentDetailsWithoutDocDueDate: DocumentDetails = documentDetails.copy(documentDueDate = None)
  val documentDetailsWithoutPOAAmount: DocumentDetails  = documentDetails.copy(poaRelevantAmount = None)

  val documentDetailsMinimal: DocumentDetails = DocumentDetails(
    None,
    documentId = "1455",
    None,
    None,
    documentDate = "2018-04-05",
    None,
    documentDueDate = None,
    None,
    originalAmount = 1.99,
    outstandingAmount = 2.99,
    None,
    isChargeEstimate = false,
    isCodedOut = false,
    None,
    None,
    None,
    None,
    None,
    None,
    None
  )

  val lastClearingJson: JsValue = Json.parse("""
       |{
       |  "lastClearingDate": "2018-04-05",
       |  "lastClearingReason": "Incoming Payment",
       |  "lastClearedAmount": 3.99
       |}
       |""".stripMargin)

  val latePaymentInterestJson: JsValue = Json.parse("""
       |{
       |  "latePaymentInterestId": "1234567890123456",
       |  "accruingInterestAmount": 4.99,
       |  "interestRate": 6.99,
       |  "interestStartDate": "2020-04-01",
       |  "interestEndDate": "2020-04-05",
       |  "interestAmount": 8.99,
       |  "interestDunningLockAmount": 7.99,
       |  "interestOutstandingAmount": 9.99
       |}
       |""".stripMargin)

  val reducedChargeJson: JsValue = Json.parse("""
       |{
       |  "chargeType": "???",
       |  "documentNumber": "???",
       |  "amendmentDate": "2018-04-05",
       |  "taxYear": "2017-18"
       |}
       |""".stripMargin)

  val documentDetailsMtdResponseWithoutPOARelevantAmountJson: JsObject =
    Json
      .parse(s"""
       |{
       |  "taxYear": "2020-21",
       |  "documentId": "1455",
       |  "formBundleNumber": "88888888",
       |  "creditReason": "Voluntary Payment",
       |  "documentDate": "2018-04-05",
       |  "documentText": "ITSA- Bal Charge",
       |  "documentDueDate": "2021-04-05",
       |  "documentDescription": "ITSA- POA 1",
       |  "originalAmount": 1.99,
       |  "outstandingAmount": 2.99,
       |  "lastClearing": $lastClearingJson,
       |  "isChargeEstimate": true,
       |  "isCodedOut": true,
       |  "paymentLot": "AB1023456789",
       |  "paymentLotItem": "000001",
       |  "effectiveDateOfPayment": "2021-04-05",
       |  "latePaymentInterest": $latePaymentInterestJson,
       |  "amountCodedOut": 5.99,
       |  "reducedCharge": $reducedChargeJson
       |}
       |""".stripMargin)
      .as[JsObject]

  val documentDetailsMtdResponseJson: JsObject =
    Json
      .parse(s"""
           |{
           |  "taxYear": "2020-21",
           |  "documentId": "1455",
           |  "formBundleNumber": "88888888",
           |  "creditReason": "Voluntary Payment",
           |  "documentDate": "2018-04-05",
           |  "documentText": "ITSA- Bal Charge",
           |  "documentDueDate": "2021-04-05",
           |  "documentDescription": "ITSA- POA 1",
           |  "originalAmount": 1.99,
           |  "outstandingAmount": 2.99,
           |  "lastClearing": $lastClearingJson,
           |  "isChargeEstimate": true,
           |  "isCodedOut": true,
           |  "paymentLot": "AB1023456789",
           |  "paymentLotItem": "000001",
           |  "effectiveDateOfPayment": "2021-04-05",
           |  "latePaymentInterest": $latePaymentInterestJson,
           |  "amountCodedOut": 5.99,
           |  "reducedCharge": $reducedChargeJson,
           |  "poaRelevantAmount":5.99
           |}
           |""".stripMargin)
      .as[JsObject]

  val documentDetailsWithoutDocDueDateMtdResponseJson: JsObject = documentDetailsMtdResponseJson - "documentDueDate"

  val documentDetailWithoutPoaRelevantAmountAndDocDueDateMtdResponseJson: JsObject =
    documentDetailsMtdResponseWithoutPOARelevantAmountJson - "documentDueDate"

  val documentDetailsDownstreamResponseJson: JsValue = newDownstreamDocumentDetailsJson("2021", maybeDocumentDueDate = Some("2021-04-05"))
  val documentDetailsDownstreamResponseHipJson: JsValue = newDownstreamDocumentDetailsHipJson("2021", maybeDocumentDueDate = Some("2021-04-05"))
  val documentDetailsWithoutDocDueDateDownstreamResponseJson: JsValue = newDownstreamDocumentDetailsJson("2021", maybeDocumentDueDate = None)
  val documentDetailsWithoutDocDueDateDownstreamResponseHipJson: JsValue = newDownstreamDocumentDetailsHipJson("2021", maybeDocumentDueDate = None)

  val documentDetailsWithoutPOAAmountDownstreamResponseJson: JsValue =
    newDownstreamDocumentDetailsWithoutPOAAmountJson("2021", maybeDocumentDueDate = Some("2021-04-05"))

  val documentDetailsWithoutPOAAmountDownstreamResponseHipJson: JsValue =
    newDownstreamDocumentDetailsWithoutPOAAmountHipJson("2021", maybeDocumentDueDate = Some("2021-04-05"))

  val documentDetailsWithoutPOAAmntAndDocDueDateDownstreamResponseJson: JsValue =
    newDownstreamDocumentDetailsWithoutPOAAmountJson("2021", maybeDocumentDueDate = None)

  val documentDetailsWithoutPOAAmntAndDocDueDateDownstreamResponseHipJson: JsValue =
    newDownstreamDocumentDetailsWithoutPOAAmountHipJson("2021", maybeDocumentDueDate = None)

  val documentDetailsDownstreamResponseMinimalJson: JsValue = Json.parse(s"""
       |{
       |  "documentId": "1455",
       |  "documentDate": "2018-04-05",
       |  "totalAmount": 1.99,
       |  "documentOutstandingAmount": 2.99,
       |  "statisticalFlag": false
       |}
       |""".stripMargin)

  val documentDetailsDownstreamResponseMinimalHipJson: JsValue = Json.parse(s"""
       |{
       |  "documentID": "1455",
       |  "documentDate": "2018-04-05",
       |  "totalAmount": 1.99,
       |  "documentOutstandingAmount": 2.99,
       |  "statisticalFlag": "N"
       |}
       |""".stripMargin)

  def newDownstreamDocumentDetailsJson(taxYear: String, maybeDocumentDueDate: Option[String]): JsValue = {
    val docDueDateLine = maybeDocumentDueDate.map(date => s"""  "documentDueDate": "$date",""").getOrElse("")

    Json.parse(s"""
       |{
       |  "taxYear": "$taxYear",
       |  "taxYearReducedCharge": "2018",
       |  "documentId": "1455",
       |  "documentNumberReducedCharge": "???",
       |  "formBundleNumber": "88888888",
       |  "documentDate": "2018-04-05",
       |  "documentText": "ITSA- Bal Charge",
       |  "effectiveDateOfPayment": "2021-04-05",
       |  $docDueDateLine
       |  "documentDescription": "ITSA- POA 1",
       |  "totalAmount": 1.99,
       |  "documentOutstandingAmount": 2.99,
       |  "lastClearingDate": "2018-04-05",
       |  "lastClearingReason": "Incoming Payment",
       |  "lastClearedAmount": 3.99,
       |  "statisticalFlag": true,
       |  "informationCode": "k",
       |  "paymentLot": "AB1023456789",
       |  "paymentLotItem": "000001",
       |  "accruingInterestAmount": 4.99,
       |  "amendmentDateReducedCharge": "2018-04-05",
       |  "amountCodedOut": 5.99,
       |  "chargeTypeReducedCharge": "???",
       |  "creditReason": "Voluntary Payment",
       |  "interestRate": 6.99,
       |  "interestFromDate": "2020-04-01",
       |  "interestEndDate": "2020-04-05",
       |  "latePaymentInterestID": "1234567890123456",
       |  "lpiWithDunningLock": 7.99,
       |  "latePaymentInterestAmount": 8.99,
       |  "interestOutstandingAmount": 9.99,
       |  "poaRelevantAmount":5.99
       |}
       |""".stripMargin)
  }

  def newDownstreamDocumentDetailsHipJson(taxYear: String, maybeDocumentDueDate: Option[String]): JsValue = {
    val docDueDateLine = maybeDocumentDueDate.map(date => s"""  "documentDueDate": "$date",""").getOrElse("")

    Json.parse(s"""
                  |{
                  |  "taxYear": "$taxYear",
                  |  "taxYearReducedCharge": "2018",
                  |  "documentID": "1455",
                  |  "documentNumberReducedCharge": "???",
                  |  "formBundleNumber": "88888888",
                  |  "documentDate": "2018-04-05",
                  |  "documentText": "ITSA- Bal Charge",
                  |  "effectiveDateOfPayment": "2021-04-05",
                  |  $docDueDateLine
                  |  "documentDescription": "ITSA- POA 1",
                  |  "totalAmount": 1.99,
                  |  "documentOutstandingAmount": 2.99,
                  |  "lastClearingDate": "2018-04-05",
                  |  "lastClearingReason": "Incoming Payment",
                  |  "lastClearedAmount": 3.99,
                  |  "statisticalFlag": "Y",
                  |  "informationCode": "k",
                  |  "paymentLot": "AB1023456789",
                  |  "paymentLotItem": "000001",
                  |  "accruingInterestAmount": 4.99,
                  |  "amendmentDateReducedCharge": "2018-04-05",
                  |  "amountCodedOut": 5.99,
                  |  "chargeTypeReducedCharge": "???",
                  |  "creditReason": "Voluntary Payment",
                  |  "interestRate": 6.99,
                  |  "interestFromDate": "2020-04-01",
                  |  "interestEndDate": "2020-04-05",
                  |  "latePaymentInterestID": "1234567890123456",
                  |  "lpiWithDunningLock": 7.99,
                  |  "latePaymentInterestAmount": 8.99,
                  |  "interestOutstandingAmount": 9.99,
                  |  "poaRelevantAmount":5.99
                  |}
                  |""".stripMargin)
  }

  def newDownstreamDocumentDetailsWithoutPOAAmountJson(taxYear: String, maybeDocumentDueDate: Option[String]): JsValue = {
    val docDueDateLine = maybeDocumentDueDate.map(date => s"""  "documentDueDate": "$date",""").getOrElse("")

    Json.parse(s"""
       |{
       |  "taxYear": "$taxYear",
       |  "taxYearReducedCharge": "2018",
       |  "documentId": "1455",
       |  "documentNumberReducedCharge": "???",
       |  "formBundleNumber": "88888888",
       |  "documentDate": "2018-04-05",
       |  "documentText": "ITSA- Bal Charge",
       |  "effectiveDateOfPayment": "2021-04-05",
       |  $docDueDateLine
       |  "documentDescription": "ITSA- POA 1",
       |  "totalAmount": 1.99,
       |  "documentOutstandingAmount": 2.99,
       |  "lastClearingDate": "2018-04-05",
       |  "lastClearingReason": "Incoming Payment",
       |  "lastClearedAmount": 3.99,
       |  "statisticalFlag": true,
       |  "informationCode": "k",
       |  "paymentLot": "AB1023456789",
       |  "paymentLotItem": "000001",
       |  "accruingInterestAmount": 4.99,
       |  "amendmentDateReducedCharge": "2018-04-05",
       |  "amountCodedOut": 5.99,
       |  "chargeTypeReducedCharge": "???",
       |  "creditReason": "Voluntary Payment",
       |  "interestRate": 6.99,
       |  "interestFromDate": "2020-04-01",
       |  "interestEndDate": "2020-04-05",
       |  "latePaymentInterestID": "1234567890123456",
       |  "lpiWithDunningLock": 7.99,
       |  "latePaymentInterestAmount": 8.99,
       |  "interestOutstandingAmount": 9.99
       |}
       |""".stripMargin)
  }

  def newDownstreamDocumentDetailsWithoutPOAAmountHipJson(taxYear: String, maybeDocumentDueDate: Option[String]): JsValue = {
    val docDueDateLine = maybeDocumentDueDate.map(date => s"""  "documentDueDate": "$date",""").getOrElse("")

    Json.parse(s"""
                  |{
                  |  "taxYear": "$taxYear",
                  |  "taxYearReducedCharge": "2018",
                  |  "documentID": "1455",
                  |  "documentNumberReducedCharge": "???",
                  |  "formBundleNumber": "88888888",
                  |  "documentDate": "2018-04-05",
                  |  "documentText": "ITSA- Bal Charge",
                  |  "effectiveDateOfPayment": "2021-04-05",
                  |  $docDueDateLine
                  |  "documentDescription": "ITSA- POA 1",
                  |  "totalAmount": 1.99,
                  |  "documentOutstandingAmount": 2.99,
                  |  "lastClearingDate": "2018-04-05",
                  |  "lastClearingReason": "Incoming Payment",
                  |  "lastClearedAmount": 3.99,
                  |  "statisticalFlag": "Y",
                  |  "informationCode": "k",
                  |  "paymentLot": "AB1023456789",
                  |  "paymentLotItem": "000001",
                  |  "accruingInterestAmount": 4.99,
                  |  "amendmentDateReducedCharge": "2018-04-05",
                  |  "amountCodedOut": 5.99,
                  |  "chargeTypeReducedCharge": "???",
                  |  "creditReason": "Voluntary Payment",
                  |  "interestRate": 6.99,
                  |  "interestFromDate": "2020-04-01",
                  |  "interestEndDate": "2020-04-05",
                  |  "latePaymentInterestID": "1234567890123456",
                  |  "lpiWithDunningLock": 7.99,
                  |  "latePaymentInterestAmount": 8.99,
                  |  "interestOutstandingAmount": 9.99
                  |}
                  |""".stripMargin)
  }

}
