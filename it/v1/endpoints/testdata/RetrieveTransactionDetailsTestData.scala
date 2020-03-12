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

package v1.endpoints.testdata

import play.api.libs.json.{JsValue, Json}

trait RetrieveTransactionDetailsTestData {

  val desJsonNoTransactions: JsValue = Json.parse(
    """
      |{
      |  "transactionItems" : []
      |}
    """.stripMargin
  )

  val desChargeJson: JsValue = Json.parse(
    """
      |{
      |  "transactionItems": [
      |    {
      |      "sapDocumentId": "0001",
      |      "type": "National Insurance Class 2",
      |      "taxPeriodFrom": "2019-04-06",
      |      "taxPeriodTo": "2020-04-05",
      |      "originalAmount": 100.45,
      |      "outstandingAmount": 10.23,
      |      "dueDate": "2021-01-31",
      |      "transactionItemDetails": [
      |        {
      |          "detailId": "001",
      |          "amount": 100.11,
      |          "clearingDate": "2021-01-31",
      |          "clearingReason": "Incoming payment",
      |          "paymentAmount": 100.11,
      |          "paymentMethod": "BACS RECEIPTS",
      |          "paymentLot": "P0101180112",
      |          "paymentLotItem": "000001"
      |        },
      |        {
      |          "detailId": "002",
      |          "amount": -10.11,
      |          "clearingDate": "2021-01-31",
      |          "clearingReason": "Outgoing payment - Paid",
      |          "outgoingPaymentMethod": "Payable Order Repayment"
      |        }
      |      ]
      |    },
      |    {
      |      "sapDocumentId": "0002",
      |      "type": "National Insurance Class 4",
      |      "taxPeriodFrom": "2019-04-06",
      |      "taxPeriodTo": "2020-04-05",
      |      "originalAmount": 100.23,
      |      "outstandingAmount": 10.45,
      |      "dueDate": "2021-01-31",
      |      "transactionItemDetails": [
      |        {
      |          "detailId": "001",
      |          "amount": 89.78,
      |          "clearingDate": "2021-01-31",
      |          "clearingReason": "Incoming payment",
      |          "paymentAmount": 89.78,
      |          "paymentMethod": "BACS RECEIPTS",
      |          "paymentLot": "P0101180112",
      |          "paymentLotItem": "000001"
      |        }
      |      ]
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val desPaymentJson: JsValue = Json.parse(
    """
      |{
      |  "transactionItems": [
      |    {
      |      "sapDocumentItemId": "0001",
      |      "type": "Payment on account",
      |      "originalAmount": -5000.00,
      |      "outstandingAmount": 0.00,
      |      "dueDate": "2021-01-31",
      |      "paymentMethod":"BACS RECEIPTS",
      |      "paymentLot":"P0101180112",
      |      "paymentLotItem": "000004",
      |      "subItems":[
      |        {
      |          "subItemId":"001",
      |          "clearingDate":"2021-01-31",
      |          "clearingReason":"Payment allocation",
      |          "paymentAmount": -1100.00
      |        },
      |        {
      |          "subItemId":"002",
      |          "clearingDate":"2021-01-31",
      |          "clearingReason":"Payment allocation",
      |          "paymentAmount":-3000.00
      |        },
      |        {
      |          "subItemId":"003",
      |          "amount":-900.00,
      |          "clearingDate":"2021-01-31",
      |          "clearingReason":"Outgoing Payment - Paid",
      |          "paymentMethod": "Payable Order Repayment"
      |        }
      |      ]
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val mtdChargeJson: JsValue = Json.parse(
    s"""
       |{
       |  "transactionItems" : [ {
       |    "type" : "National Insurance Class 2",
       |    "taxPeriodFrom" : "2019-04-06",
       |    "taxPeriodTo" : "2020-04-05",
       |    "originalAmount" : 100.45,
       |    "outstandingAmount" : 10.23,
       |    "dueDate" : "2021-01-31"
       |  }, {
       |    "type" : "National Insurance Class 4",
       |    "taxPeriodFrom" : "2019-04-06",
       |    "taxPeriodTo" : "2020-04-05",
       |    "originalAmount" : 100.23,
       |    "outstandingAmount" : 10.45,
       |    "dueDate" : "2021-01-31"
       |  } ],
       |  "links" : [ {
       |    "href" : "/accounts/self-assessment/AA123456A/transactions/1111111111",
       |    "method" : "GET",
       |    "rel" : "self"
       |  }, {
       |    "href" : "/accounts/self-assessment/AA123456A/charges/1111111111",
       |    "method" : "GET",
       |    "rel" : "self"
       |  } ]
       |}
    """.stripMargin
  )

  val mtdPaymentJson: JsValue = Json.parse(
    s"""
       |{
       |  "transactionItems" : [ {
       |    "transactionItemId" : "0001",
       |    "type" : "Payment on account",
       |    "originalAmount" : -5000,
       |    "outstandingAmount" : 0,
       |    "dueDate" : "2021-01-31",
       |    "paymentMethod" : "BACS RECEIPTS",
       |    "paymentId" : "P0101180112-000004",
       |    "subItems" : [ {
       |      "subItemId" : "001",
       |      "clearingDate" : "2021-01-31",
       |      "clearingReason" : "Payment allocation",
       |      "paymentAmount" : -1100
       |    }, {
       |      "subItemId" : "002",
       |      "clearingDate" : "2021-01-31",
       |      "clearingReason" : "Payment allocation",
       |      "paymentAmount" : -3000
       |    }, {
       |      "subItemId" : "003",
       |      "amount" : -900,
       |      "clearingDate" : "2021-01-31",
       |      "clearingReason" : "Outgoing Payment - Paid",
       |      "paymentMethod" : "Payable Order Repayment"
       |    } ]
       |  } ],
       |  "links" : [ {
       |    "href" : "/accounts/self-assessment/AA123456A/transactions/1111111111",
       |    "method" : "GET",
       |    "rel" : "self"
       |  }, {
       |    "href" : "/accounts/self-assessment/AA123456A/payments/P0101180112-000004",
       |    "method" : "GET",
       |    "rel" : "self"
       |  } ]
       |}
    """.stripMargin
  )
}
