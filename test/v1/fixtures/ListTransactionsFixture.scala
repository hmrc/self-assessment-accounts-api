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

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.domain.Nino
import v1.models.request.listTransactions.ListTransactionsParsedRequest
import v1.models.response.listTransaction.{ListTransactionsResponse, TransactionItem}


object ListTransactionsFixture {

  val nino = Nino("AA123456A")
  val from = "2018-04-05"
  val to = "2019-11-05"

  val requestData: ListTransactionsParsedRequest = ListTransactionsParsedRequest(nino, from, to)

  val fullDesDocIdTransactionItemResponse: JsValue = Json.parse(
    """
      |{
      |    "taxYear": "2020",
      |    "documentId": "X123456790A",
      |    "documentDate": "2020-01-01",
      |    "documentDescription": "Balancing Charge Debit",
      |    "totalAmount": 12.34,
      |    "documentOutstandingAmount": 10.33,
      |    "lastClearingDate": "2020-01-02",
      |    "lastClearingReason": "Refund",
      |    "lastClearedAmount": 2.01,
      |    "statisticalFlag": false
      |}
    """.stripMargin
  )

  val fullDesPaymentLotIdTransactionItemResponse: JsValue = Json.parse(
    """
      |{
      |    "taxYear": "2020",
      |    "documentId": "X123456790B",
      |    "documentDate": "2020-01-01",
      |    "documentDescription": "Payment On Account",
      |    "totalAmount": 12.34,
      |    "documentOutstandingAmount": 10.33,
      |    "lastClearingDate": "2020-01-02",
      |    "lastClearingReason": "Payment Allocation",
      |    "lastClearedAmount": 2.01,
      |    "statisticalFlag": false,
      |    "paymentLot": "081203010024",
      |    "paymentLotItem": "000001"
      |}
    """.stripMargin
  )

  val fullDesTransactionItemResponse: JsValue = Json.parse(
    """
      |{
      |    "taxYear": "2020",
      |    "documentId": "X123456790A",
      |    "documentDate": "2020-01-01",
      |    "documentDescription": "Balancing Charge Debit",
      |    "totalAmount": 12.34,
      |    "documentOutstandingAmount": 10.33,
      |    "lastClearingDate": "2020-01-02",
      |    "lastClearingReason": "Example Reason",
      |    "lastClearedAmount": 2.01,
      |    "statisticalFlag": false,
      |    "paymentLot": "081203010024",
      |    "paymentLotItem": "000001"
      |}
    """.stripMargin
  )

  val minimalDesTransactionItemResponse: JsValue = Json.parse(
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

  val invalidDesTransactionItemResponse: JsValue = Json.parse(
    """
      |{
      |         "taxYear":"2020",
      |         "id":"X123456790A",
      |         "transactionDate":"2020-01-01",
      |         "type":"Balancing Charge Debit",
      |         "originalAmount":12.34,
      |         "outstandingAmount":10.33,
      |         "lastClearingDate":"2020-01-02",
      |         "lastClearingReason": 9999.99,
      |         "lastClearedAmount":"Invalid type STRING"
      |}
    """.stripMargin
  )

  val mtdListTransactionItemResponse: JsValue = Json.parse(
    """
      |{
      |         "taxYear":"2019-20",
      |         "transactionId":"X123456790A",
      |         "paymentId": "081203010024-000001",
      |         "transactionDate":"2020-01-01",
      |         "type":"Balancing Charge Debit",
      |         "originalAmount":12.34,
      |         "outstandingAmount":10.33,
      |         "lastClearingDate":"2020-01-02",
      |         "lastClearingReason":"Example Reason",
      |         "lastClearedAmount":2.01
      |}
    """.stripMargin
  )

  val chargeTransactionItemModel: TransactionItem =
    TransactionItem(
      taxYear = "2019-20",
      transactionId = "X123456790A",
      paymentId = None,
      transactionDate = "2020-01-01",
      `type` = Some("Balancing Charge Debit"),
      originalAmount = 12.34,
      outstandingAmount = 10.33,
      lastClearingDate = Some("2020-01-02"),
      lastClearingReason = Some("Refund"),
      lastClearedAmount = Some(2.01)
    )

  val fullTransactionItemModel: TransactionItem =
    TransactionItem(
      taxYear = "2019-20",
      transactionId = "X123456790A",
      paymentId = Some("081203010024-000001"),
      transactionDate = "2020-01-01",
      `type` = Some("Balancing Charge Debit"),
      originalAmount = 12.34,
      outstandingAmount = 10.33,
      lastClearingDate = Some("2020-01-02"),
      lastClearingReason = Some("Example Reason"),
      lastClearedAmount = Some(2.01)
    )

  val paymentTransactionItemModel: TransactionItem =
    TransactionItem(
      taxYear = "2019-20",
      transactionId = "X123456790B",
      paymentId = Some("081203010024-000001"),
      transactionDate = "2020-01-01",
      `type` = Some("Payment On Account"),
      originalAmount = 12.34,
      outstandingAmount = 10.33,
      lastClearingDate = Some("2020-01-02"),
      lastClearingReason = Some("Payment Allocation"),
      lastClearedAmount = Some(2.01)
    )

  val minimalTransactionItemModel: TransactionItem =
    TransactionItem(
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

  val fullDesSingleListTransactionsResponse: JsValue = Json.parse(
    s"""
       |{
       |   "taxPayerDetails": {
       |      "idType": "NINO",
       |      "idNumber": "AA123456A",
       |      "regimeType": "ITSA"
       |   },
       |   "documentDetails" : [$fullDesTransactionItemResponse]
       |}
      """.stripMargin
  )

  val fullDesMultipleListTransactionsMultipleResponse: JsValue = Json.parse(
    s"""
       |{
       |   "taxPayerDetails": {
       |      "idType": "NINO",
       |      "idNumber": "AA123456A",
       |      "regimeType": "ITSA"
       |   },
       |   "documentDetails" : [$fullDesTransactionItemResponse, $fullDesTransactionItemResponse]
       |}
      """.stripMargin
  )

  val minimalDesListTransactionsResponse: JsValue = Json.parse(
    """
      |{
      |   "taxPayerDetails": {
      |      "idType": "NINO",
      |      "idNumber": "AA123456A",
      |      "regimeType": "ITSA"
      |   },
      |   "documentDetails" : [ ]
      |}
    """.stripMargin
  )

  val minimalItemListTransactionsDesResponse: JsValue = Json.parse(
    s"""
       |{
       |   "taxPayerDetails": {
       |      "idType": "NINO",
       |      "idNumber": "AA123456A",
       |      "regimeType": "ITSA"
       |   },
       |   "documentDetails" : [$minimalDesTransactionItemResponse]
       |}
      """.stripMargin
  )

  val invalidDesListTransactionsResponse: JsValue = Json.parse(
    """
      |{
      |
      |}
    """.stripMargin
  )

  val mtdListTransactionsResponse: JsValue = Json.parse(
    s"""
       |{
       |   "transactions": [$mtdListTransactionItemResponse]
       |}
      """.stripMargin
  )

  val fullSingleListTransactionsModel: ListTransactionsResponse[TransactionItem] =
    ListTransactionsResponse[TransactionItem](transactions = Seq(fullTransactionItemModel))

  val fullMultipleListTransactionsModel: ListTransactionsResponse[TransactionItem] =
    ListTransactionsResponse[TransactionItem](transactions = Seq(fullTransactionItemModel, fullTransactionItemModel))

  val minimalListTransactionsModel: ListTransactionsResponse[TransactionItem] =
    ListTransactionsResponse[TransactionItem](transactions = Seq.empty[TransactionItem])

  val minimalItemListTransactionsModel: ListTransactionsResponse[TransactionItem] = ListTransactionsResponse[TransactionItem](
    transactions = Seq(minimalTransactionItemModel)
  )

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |   "transactions":[
      |      {
      |         "taxYear":"2019-20",
      |         "transactionId":"X123456790A",
      |         "transactionDate":"2020-01-01",
      |         "type":"Balancing Charge Debit",
      |         "originalAmount":12.34,
      |         "outstandingAmount":10.33,
      |         "lastClearingDate":"2020-01-02",
      |         "lastClearingReason":"Refund",
      |         "lastClearedAmount":2.01,
      |         "links": [
      |         {
      |            "href": "/accounts/self-assessment/AA123456A/transactions/X123456790A",
      |			       "method": "GET",
      |			       "rel": "retrieve-transaction-details"
      |		       }
      |        ]
      |      },
      |      {
      |         "taxYear":"2019-20",
      |         "transactionId":"X123456790B",
      |         "paymentId":"081203010024-000001",
      |         "transactionDate":"2020-01-01",
      |         "type":"Payment On Account",
      |         "originalAmount":12.34,
      |         "outstandingAmount":10.33,
      |         "lastClearingDate":"2020-01-02",
      |         "lastClearingReason":"Payment Allocation",
      |         "lastClearedAmount":2.01,
      |          "links": [
      |          {
      |             "href": "/accounts/self-assessment/AA123456A/payments/081203010024-000001",
      |			        "method": "GET",
      |			        "rel": "retrieve-payment-allocations"
      |		        },
      |           {
      |             "href": "/accounts/self-assessment/AA123456A/transactions/X123456790B",
      |			        "method": "GET",
      |			        "rel": "retrieve-transaction-details"
      |		        }
      |          ]
      |      }
      |   ],
      |   "links": [
      |      {
      |        "href": "/accounts/self-assessment/AA123456A/transactions?from=2018-10-01&to=2019-10-01",
      |			   "method": "GET",
      |			   "rel": "self"
      |		   },
      |      {
      |        "href": "/accounts/self-assessment/AA123456A/charges?from=2018-10-01&to=2019-10-01",
      |			   "method": "GET",
      |			   "rel": "list-charges"
      |		   },
      |      {
      |        "href": "/accounts/self-assessment/AA123456A/payments?from=2018-10-01&to=2019-10-01",
      |			   "method": "GET",
      |			   "rel": "list-payments"
      |		   }
      |     ]
      |}
    """.stripMargin
  )
}
