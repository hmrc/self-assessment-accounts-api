/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.fixtures

import play.api.libs.json.{JsValue, Json}
import v1.models.response.retrieveTransaction.{RetrieveTransactionsResponse, TransactionItem}

object RetrieveTransactionFixture {

  val fullDesDocIdTransactionItemResponse: JsValue = Json.parse(
    """
      |{
      |         "taxYear":"2019",
      |         "documentId":"X123456790A",
      |         "transactionDate":"2020-01-01",
      |         "type":"Balancing Charge Debit",
      |         "originalAmount":12.34,
      |         "outstandingAmount":10.33,
      |         "lastClearingDate":"2020-01-02",
      |         "lastClearingReason":"Example reason",
      |         "lastClearedAmount":2.01
      |}
      |""".stripMargin)

  val fullDesPaymentLotIdTransactionItemResponse: JsValue = Json.parse(
    """
      |{
      |         "taxYear":"2019",
      |         "paymentLot":"081203010024",
      |         "paymentLotItem":"000001",
      |         "transactionDate":"2020-01-01",
      |         "type":"Balancing Charge Debit",
      |         "originalAmount":12.34,
      |         "outstandingAmount":10.33,
      |         "lastClearingDate":"2020-01-02",
      |         "lastClearingReason":"Example reason",
      |         "lastClearedAmount":2.01
      |}
      |""".stripMargin)

  val fullDesTransactionItemResponse: JsValue = Json.parse(
    """
      |{
      |         "taxYear":"2019",
      |         "documentId":"X123456790A",
      |         "paymentLot":"081203010024",
      |         "paymentLotItem":"000001",
      |         "transactionDate":"2020-01-01",
      |         "type":"Balancing Charge Debit",
      |         "originalAmount":12.34,
      |         "outstandingAmount":10.33,
      |         "lastClearingDate":"2020-01-02",
      |         "lastClearingReason":"Example reason",
      |         "lastClearedAmount":2.01
      |}
      |""".stripMargin)

  val minimalDesTransactionItemResponse: JsValue = Json.parse(
    """
      |{
      |
      |}
      |""".stripMargin)

  val invalidDesTransactionItemResponse: JsValue = Json.parse(
    """
      |{
      |         "taxYear":"2019",
      |         "id":"X123456790A",
      |         "transactionDate":"2020-01-01",
      |         "type":12.34,
      |         "originalAmount":"Balancing Charge Debit",
      |         "outstandingAmount":10.33,
      |         "lastClearingDate":"2020-01-02",
      |         "lastClearingReason":2.01,
      |         "lastClearedAmount":"Example reason"
      |}
      |""".stripMargin)

  val mtdRetrieveTransactionItemResponse: JsValue = Json.parse(
    """
      |{
      |         "taxYear":"2018-19",
      |         "id":"081203010024-000001",
      |         "transactionDate":"2020-01-01",
      |         "type":"Balancing Charge Debit",
      |         "originalAmount":12.34,
      |         "outstandingAmount":10.33,
      |         "lastClearingDate":"2020-01-02",
      |         "lastClearingReason":"Example reason",
      |         "lastClearedAmount":2.01
      |}
      |""".stripMargin)

  val fullDocIdTransactionItemModel: TransactionItem = TransactionItem(taxYear = Some("2018-19"),
                                                                  id = Some("X123456790A"),
                                                                  transactionDate = Some("2020-01-01"),
                                                                  `type` = Some("Balancing Charge Debit"),
                                                                  originalAmount = Some(12.34),
                                                                  outstandingAmount = Some(10.33),
                                                                  lastClearingDate = Some("2020-01-02"),
                                                                  lastClearingReason = Some("Example reason"),
                                                                  lastClearedAmount = Some(2.01)
    )

  val fullTransactionItemModel: TransactionItem = TransactionItem(taxYear = Some("2018-19"),
                                                                  id = Some("081203010024-000001"),
                                                                  transactionDate = Some("2020-01-01"),
                                                                  `type` = Some("Balancing Charge Debit"),
                                                                  originalAmount = Some(12.34),
                                                                  outstandingAmount = Some(10.33),
                                                                  lastClearingDate = Some("2020-01-02"),
                                                                  lastClearingReason = Some("Example reason"),
                                                                  lastClearedAmount = Some(2.01)
  )

  val minimalTransactionItemModel: TransactionItem = TransactionItem(None, None, None, None, None, None, None, None, None)

  val fullDesSingleRetreiveTransactionResponse: JsValue = Json.parse(
    s"""
      |{
      |  "transactions" : [$fullDesTransactionItemResponse]
      |}
      |""".stripMargin)

  val fullDesMultipleRetreiveTransactionMultipleResponse: JsValue = Json.parse(
    s"""
      |{
      |  "transactions" : [$fullDesTransactionItemResponse, $fullDesTransactionItemResponse]
      |}
      |""".stripMargin)

  val minimalDesRetreiveTransactionResponse: JsValue = Json.parse(
    """
      |{
      |  "transactions" : []
      |}
      |""".stripMargin)

  val emptyItemDesRetrieveTransactionResponse: JsValue = Json.parse(
    s"""
      |{
      |  "transactions" : [$minimalDesTransactionItemResponse]
      |}
      |""".stripMargin
  )

  val invalidDesRetrieveTransactionResponse: JsValue = Json.parse(
    """
      |{
      |
      |}
      |""".stripMargin)

  val mtdRetrievetransactionResponse: JsValue = Json.parse(
    s"""
      |{
      |   "transactions": [$mtdRetrieveTransactionItemResponse]
      |}
      |""".stripMargin)

  val fullSingleRetreiveTransactionModel: RetrieveTransactionsResponse[TransactionItem] =
    RetrieveTransactionsResponse[TransactionItem](transactions = Seq(fullTransactionItemModel))


  val fullMultipleRetreiveTransactionModel: RetrieveTransactionsResponse[TransactionItem] =
    RetrieveTransactionsResponse[TransactionItem](transactions = Seq(fullTransactionItemModel, fullTransactionItemModel))

  val minimalRetreiveTransactionModel: RetrieveTransactionsResponse[TransactionItem] = RetrieveTransactionsResponse(transactions = Seq.empty[TransactionItem])
}
