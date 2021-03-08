/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json.{JsObject, JsValue, Json}
import v1.models.response.listTransaction.{ListTransactionsResponse, TransactionItem}

object ListTransactionsFixture {

  val fullTransactionItemWithoutPaymentLotDesResponse: JsValue = Json.parse(
    """
      |{
      |   "taxYear": "2020",
      |   "documentId": "X1234567890A",
      |   "documentDate": "2020-01-01",
      |   "documentDescription": "Balancing Charge Debit",
      |   "totalAmount": 12.34,
      |   "documentOutstandingAmount": 10.33,
      |   "lastClearingDate": "2020-01-02",
      |   "lastClearingReason": "Incoming payment",
      |   "lastClearedAmount": 2.01,
      |   "statisticalFlag": false
      |}
    """.stripMargin
  )

  val fullTransactionItemWithPaymentLotDesResponse: JsValue = Json.parse(
    """
      |{
      |   "taxYear": "2020",
      |   "documentId": "X1234567890A",
      |   "documentDate": "2020-01-05",
      |   "documentDescription": "Payment On Account",
      |   "totalAmount": 12.34,
      |   "documentOutstandingAmount": 10.33,
      |   "lastClearingDate": "2020-01-06",
      |   "lastClearingReason": "Outgoing payment paid",
      |   "lastClearedAmount": 2.01,
      |   "statisticalFlag": false,
      |   "paymentLot": "081203010024",
      |   "paymentLotItem": "000001"
      |}
    """.stripMargin
  )

  val minimalTransactionItemDesResponse: JsValue = Json.parse(
    """
      |{
      |   "taxYear": "2020",
      |   "documentId": "X1234567890A",
      |   "documentDate": "2020-01-01",
      |   "totalAmount": 12.34,
      |   "documentOutstandingAmount": 10.33,
      |   "statisticalFlag": false
      |}
    """.stripMargin
  )

  val fullTransactionItemDesResponse: JsValue = Json.parse(
    """
      |{
      |   "taxYear": "2020",
      |   "documentId": "X1234567890A",
      |   "documentDate": "2020-01-05",
      |   "documentDescription": "Payment On Account",
      |   "totalAmount": 12.34,
      |   "documentOutstandingAmount": 10.33,
      |   "lastClearingDate": "2020-01-06",
      |   "lastClearingReason": "Outgoing payment paid",
      |   "lastClearedAmount": 2.01,
      |   "statisticalFlag": false,
      |   "paymentLot": "081203010024",
      |   "paymentLotItem": "000001"
      |}
    """.stripMargin
  )

  val fullTransactionItemMtdResponse: JsValue = Json.parse(
    """
      |{
      |   "taxYear": "2019-20",
      |   "transactionId": "X1234567890A",
      |   "paymentId": "081203010024-000001",
      |   "transactionDate": "2020-01-05",
      |   "type": "Payment On Account",
      |   "originalAmount": 12.34,
      |   "outstandingAmount": 10.33,
      |   "lastClearingDate": "2020-01-06",
      |   "lastClearingReason": "Outgoing payment paid",
      |   "lastClearedAmount": 2.01
      |}
    """.stripMargin
  )

  val emptyDesResponse: JsValue = JsObject.empty

  val minimalItemListTransactionsDesResponse: JsValue = Json.parse(
    s"""
       |{
       |   "taxPayerDetails": {
       |      "idType": "NINO",
       |      "idNumber": "AA123456A",
       |      "regimeType": "ITSA"
       |   },
       |   "documentDetails": [$minimalTransactionItemDesResponse]
       |}
     """.stripMargin
  )

  val fullSingleItemListTransactionsDesResponse: JsValue = Json.parse(
    s"""
       |{
       |   "taxPayerDetails": {
       |      "idType": "NINO",
       |      "idNumber": "AA123456A",
       |      "regimeType": "ITSA"
       |   },
       |   "documentDetails": [$fullTransactionItemDesResponse]
       |}
     """.stripMargin
  )

  val fullMultipleItemsListTransactionsDesResponse: JsValue = Json.parse(
    s"""
       |{
       |   "taxPayerDetails": {
       |      "idType": "NINO",
       |      "idNumber": "AA123456A",
       |      "regimeType": "ITSA"
       |   },
       |   "documentDetails": [$fullTransactionItemWithoutPaymentLotDesResponse, $fullTransactionItemWithPaymentLotDesResponse]
       |}
     """.stripMargin
  )

  val listTransactionsMtdResponse: JsValue = Json.parse(
    s"""
       |{
       |   "transactions": [$fullTransactionItemMtdResponse]
       |}
     """.stripMargin
  )

  val listTransactionsMtdResponseWithHateoas: JsValue = Json.parse(
    """
      |{
      |   "transactions": [
      |      {
      |         "taxYear": "2019-20",
      |         "transactionId": "X1234567890A",
      |         "transactionDate": "2020-01-01",
      |         "type": "Balancing Charge Debit",
      |         "originalAmount": 12.34,
      |         "outstandingAmount": 10.33,
      |         "lastClearingDate": "2020-01-02",
      |         "lastClearingReason": "Incoming payment",
      |         "lastClearedAmount": 2.01,
      |         "links": [
      |            {
      |               "href": "/accounts/self-assessment/AA123456A/transactions/X1234567890A",
      |               "method": "GET",
      |               "rel": "retrieve-transaction-details"
      |            }
      |         ]
      |      },
      |      {
      |         "taxYear": "2019-20",
      |         "transactionId": "X1234567890A",
      |         "paymentId": "081203010024-000001",
      |         "transactionDate": "2020-01-05",
      |         "type": "Payment On Account",
      |         "originalAmount": 12.34,
      |         "outstandingAmount": 10.33,
      |         "lastClearingDate": "2020-01-06",
      |         "lastClearingReason": "Outgoing payment paid",
      |         "lastClearedAmount": 2.01,
      |         "links": [
      |            {
      |               "href": "/accounts/self-assessment/AA123456A/payments/081203010024-000001",
      |               "method": "GET",
      |               "rel": "retrieve-payment-allocations"
      |            },
      |            {
      |               "href": "/accounts/self-assessment/AA123456A/transactions/X1234567890A",
      |               "method": "GET",
      |               "rel": "retrieve-transaction-details"
      |            }
      |         ]
      |      }
      |   ],
      |   "links": [
      |      {
      |         "href": "/accounts/self-assessment/AA123456A/transactions?from=2018-05-05&to=2019-12-05",
      |         "method": "GET",
      |         "rel": "self"
      |      },
      |      {
      |         "href": "/accounts/self-assessment/AA123456A/charges?from=2018-05-05&to=2019-12-05",
      |         "method": "GET",
      |         "rel": "list-charges"
      |      },
      |      {
      |         "href": "/accounts/self-assessment/AA123456A/payments?from=2018-05-05&to=2019-12-05",
      |         "method": "GET",
      |         "rel": "list-payments"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val chargesTransactionItemModel: TransactionItem = TransactionItem(
    taxYear = "2019-20",
    transactionId = "X1234567890A",
    paymentId = None,
    transactionDate = "2020-01-01",
    `type` = Some("Balancing Charge Debit"),
    originalAmount = 12.34,
    outstandingAmount = 10.33,
    lastClearingDate = Some("2020-01-02"),
    lastClearingReason = Some("Incoming payment"),
    lastClearedAmount = Some(2.01)
  )

  val paymentsTransactionItemModel: TransactionItem = TransactionItem(
    taxYear = "2019-20",
    transactionId = "X1234567890A",
    paymentId = Some("081203010024-000001"),
    transactionDate = "2020-01-05",
    `type` = Some("Payment On Account"),
    originalAmount = 12.34,
    outstandingAmount = 10.33,
    lastClearingDate = Some("2020-01-06"),
    lastClearingReason = Some("Outgoing payment paid"),
    lastClearedAmount = Some(2.01)
  )

  val minimalTransactionItemModel: TransactionItem = TransactionItem(
    taxYear = "2019-20",
    transactionId = "X1234567890A",
    paymentId = None,
    transactionDate = "2020-01-01",
    `type` = None,
    originalAmount = 12.34,
    outstandingAmount = 10.33,
    lastClearingDate = None,
    lastClearingReason = None,
    lastClearedAmount = None
  )

  val fullTransactionItemModel: TransactionItem = TransactionItem(
    taxYear = "2019-20",
    transactionId = "X1234567890A",
    paymentId = Some("081203010024-000001"),
    transactionDate = "2020-01-05",
    `type` = Some("Payment On Account"),
    originalAmount = 12.34,
    outstandingAmount = 10.33,
    lastClearingDate = Some("2020-01-06"),
    lastClearingReason = Some("Outgoing payment paid"),
    lastClearedAmount = Some(2.01)
  )

  val minimalItemListTransactionsModel: ListTransactionsResponse[TransactionItem] = ListTransactionsResponse[TransactionItem](
    transactions = Seq(minimalTransactionItemModel)
  )

  val fullSingleItemListTransactionsModel: ListTransactionsResponse[TransactionItem] = ListTransactionsResponse[TransactionItem](
    transactions = Seq(fullTransactionItemModel)
  )

  val fullMultipleItemsListTransactionsModel: ListTransactionsResponse[TransactionItem] = ListTransactionsResponse[TransactionItem](
    transactions = Seq(chargesTransactionItemModel, paymentsTransactionItemModel)
  )
}