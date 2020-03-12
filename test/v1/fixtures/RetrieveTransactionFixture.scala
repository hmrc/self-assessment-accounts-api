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
import uk.gov.hmrc.domain.Nino
import v1.models.request.retrieveTransactions.RetrieveTransactionsParsedRequest
import v1.models.response.retrieveTransaction.{RetrieveTransactionsResponse, TransactionItem}

object RetrieveTransactionFixture {

  val nino = Nino("AA123456A")
  val dateFrom = "2018-04-05"
  val dateTo = "2019-11-05"

  val requestData: RetrieveTransactionsParsedRequest = RetrieveTransactionsParsedRequest(nino, dateFrom, dateTo)

  val fullDesDocIdTransactionItemResponse: JsValue = Json.parse(
    """
      |{
      |         "taxYear":"2020",
      |         "documentId":"X123456790A",
      |         "transactionDate":"2020-01-01",
      |         "type":"Balancing Charge Debit",
      |         "originalAmount":12.34,
      |         "outstandingAmount":10.33,
      |         "lastClearingDate":"2020-01-02",
      |         "lastClearingReason":"Refund",
      |         "lastClearedAmount":2.01
      |}
      |""".stripMargin)

  val fullDesPaymentLotIdTransactionItemResponse: JsValue = Json.parse(
    """
      |{
      |         "taxYear":"2020",
      |         "paymentLot":"081203010024",
      |         "paymentLotItem":"000001",
      |         "transactionDate":"2020-01-01",
      |         "type":"Payment On Account",
      |         "originalAmount":12.34,
      |         "outstandingAmount":10.33,
      |         "lastClearingDate":"2020-01-02",
      |         "lastClearingReason":"Payment Allocation",
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
      |         "transactionId": "X123456790A",
      |         "paymentId":"081203010024-000001",
      |         "transactionDate":"2020-01-01",
      |         "type":"Balancing Charge Debit",
      |         "originalAmount":12.34,
      |         "outstandingAmount":10.33,
      |         "lastClearingDate":"2020-01-02",
      |         "lastClearingReason":"Example reason",
      |         "lastClearedAmount":2.01
      |}
      |""".stripMargin)

  val chargeTransactionItemModel: TransactionItem = TransactionItem(taxYear = Some("2019-20"),
                                                                  transactionId = Some("X123456790A"),
                                                                  paymentId = None,
                                                                  transactionDate = Some("2020-01-01"),
                                                                  `type` = Some("Balancing Charge Debit"),
                                                                  originalAmount = Some(12.34),
                                                                  outstandingAmount = Some(10.33),
                                                                  lastClearingDate = Some("2020-01-02"),
                                                                  lastClearingReason = Some("Refund"),
                                                                  lastClearedAmount = Some(2.01)
    )

  val fullTransactionItemModel: TransactionItem = TransactionItem(taxYear = Some("2018-19"),
                                                                  transactionId = Some("X123456790A"),
                                                                  paymentId = Some("081203010024-000001"),
                                                                  transactionDate = Some("2020-01-01"),
                                                                  `type` = Some("Balancing Charge Debit"),
                                                                  originalAmount = Some(12.34),
                                                                  outstandingAmount = Some(10.33),
                                                                  lastClearingDate = Some("2020-01-02"),
                                                                  lastClearingReason = Some("Example reason"),
                                                                  lastClearedAmount = Some(2.01)
  )

  val paymentTransactionItemModel: TransactionItem = TransactionItem(taxYear = Some("2019-20"),
                                                                  transactionId = None,
                                                                  paymentId = Some("081203010024-000001"),
                                                                  transactionDate = Some("2020-01-01"),
                                                                  `type` = Some("Payment On Account"),
                                                                  originalAmount = Some(12.34),
                                                                  outstandingAmount = Some(10.33),
                                                                  lastClearingDate = Some("2020-01-02"),
                                                                  lastClearingReason = Some("Payment Allocation"),
                                                                  lastClearedAmount = Some(2.01)
  )

  val minimalTransactionItemModel: TransactionItem = TransactionItem(None, None, None, None, None, None, None, None, None, None)

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

  val mtdJson = Json.parse(
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
      |         "links": [{
      |           "href": "/accounts/self-assessment/AA123456A/charges/X123456790A",
      |			      "method": "GET",
      |			      "rel": "retrieve-charge-history"
      |		      }]
      |      },
      |      {
      |         "taxYear":"2019-20",
      |         "paymentId":"081203010024-000001",
      |         "transactionDate":"2020-01-01",
      |         "type":"Payment On Account",
      |         "originalAmount":12.34,
      |         "outstandingAmount":10.33,
      |         "lastClearingDate":"2020-01-02",
      |         "lastClearingReason":"Payment Allocation",
      |         "lastClearedAmount":2.01,
      |          "links": [{
      |             "href": "/accounts/self-assessment/AA123456A/payments/081203010024-000001",
      |			        "method": "GET",
      |			        "rel": "retrieve-payment-allocations"
      |		        }]
      |      }
      |   ],
      |   "links": [
      |      {
      |        "href": "/accounts/self-assessment/AA123456A/transactions",
      |			   "method": "GET",
      |			   "rel": "self"
      |		   },
      |      {
      |        "href": "/accounts/self-assessment/AA123456A/payments",
      |			   "method": "GET",
      |			   "rel": "list-payments"
      |		   },
      |      {
      |        "href": "/accounts/self-assessment/AA123456A/charges",
      |			   "method": "GET",
      |			   "rel": "list-charges"
      |		   }
      |     ]
      |
      |}
    """.stripMargin)
}
