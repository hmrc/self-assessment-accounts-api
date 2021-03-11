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

package v1.fixture

import play.api.libs.json.{JsValue, Json}

trait RetrieveTransactionDetailsFixture {

  val desJsonNoTransactions: JsValue = Json.parse(
    """
      |{
      |  "financialDetails" : []
      |}
    """.stripMargin
  )

  val desJsonNoRelevantTransactions: JsValue = Json.parse(
    """
      |{
      |   "financialDetails": [
      |   {
      |   "items":
      |       [
      |            {
      |             "taxYear": "2020",
      |             "documentId": "123",
      |             "totalAmount": 1000
      |            }
      |        ]
      |   }
      |   ]
      |}
    """.stripMargin
  )

  val desJsonCharge: JsValue = Json.parse(
    """
      |{
      |  "financialDetails": [
      |    {
      |      "sapDocumentNumberItem": "0001",
      |      "chargeType": "National Insurance Class 2",
      |      "taxPeriodFrom": "2019-04-06",
      |      "taxPeriodTo": "2020-04-05",
      |      "originalAmount": 100.45,
      |      "outstandingAmount": 10.23,
      |      "items": [
      |        {
      |          "subItem": "001",
      |          "amount": 100.11,
      |          "clearingDate": "2021-01-31",
      |          "clearingReason": "Incoming payment",
      |          "paymentAmount": 100.11,
      |          "paymentMethod": "BACS RECEIPTS",
      |          "dueDate": "2021-01-31"
      |        },
      |        {
      |          "subItem": "002",
      |          "amount": 100.11,
      |          "clearingDate": "2021-01-31",
      |          "clearingReason": "Incoming payment",
      |          "paymentAmount": 100.11,
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

  val desJsonPayment: JsValue = Json.parse(
    """
      |{
      |  "financialDetails": [
      |    {
      |      "sapDocumentNumberItem": "0002",
      |      "chargeType": "National Insurance Class 4",
      |      "taxPeriodFrom": "2019-04-06",
      |      "taxPeriodTo": "2020-04-05",
      |      "originalAmount": 100.23,
      |      "outstandingAmount": 10.45,
      |      "items":[
      |         {
      |           "subItem":"003",
      |           "clearingDate":"2021-01-31",
      |           "clearingReason":"Payment allocation",
      |           "paymentAmount": -1100.00,
      |           "dueDate": "2021-01-31",
      |           "paymentLot": "P0101180112",
      |           "paymentLotItem": "000004"
      |         },
      |        {
      |           "subItem":"004",
      |           "clearingDate":"2021-01-31",
      |           "clearingReason":"Payment allocation",
      |           "paymentAmount": -1100.00,
      |           "paymentLot": "P0101180112",
      |           "paymentLotItem": "000004"
      |         }
      |      ]
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val mtdJsonCharge: JsValue = Json.parse(
    """
      |{
      |  "transactionItems": [
      |    {
      |      "transactionItemId": "0001",
      |      "type": "National Insurance Class 2",
      |      "taxPeriodFrom": "2019-04-06",
      |      "taxPeriodTo": "2020-04-05",
      |      "originalAmount": 100.45,
      |      "outstandingAmount": 10.23,
      |      "dueDate": "2021-01-31",
      |      "paymentMethod": "BACS RECEIPTS",
      |      "subItems": [
      |        {
      |          "subItemId": "002",
      |          "amount": 100.11,
      |          "clearingDate": "2021-01-31",
      |          "clearingReason": "Incoming payment",
      |          "paymentAmount": 100.11,
      |          "paymentMethod": "BACS RECEIPTS",
      |          "paymentId": "P0101180112-000001"
      |        }
      |      ]
      |    }
      |  ],
      |  "links": [
      |      {
      |		      "href": "/accounts/self-assessment/AA123456A/transactions/1111111111",
      |		      "method": "GET",
      |		      "rel": "self"
      |	     }
      |   ]
      |}
    """.stripMargin
  )

  val mtdJsonPayment: JsValue = Json.parse(
    """
      |{
      |  "transactionItems": [
      |    {
      |      "transactionItemId": "0002",
      |      "type": "National Insurance Class 4",
      |      "taxPeriodFrom": "2019-04-06",
      |      "taxPeriodTo": "2020-04-05",
      |      "originalAmount": 100.23,
      |      "outstandingAmount": 10.45,
      |      "dueDate": "2021-01-31",
      |      "paymentId": "P0101180112-000004",
      |      "subItems":[
      |         {
      |           "subItemId":"004",
      |           "clearingDate":"2021-01-31",
      |           "clearingReason":"Payment allocation",
      |           "paymentAmount": -1100.00,
      |           "paymentId": "P0101180112-000004"
      |         }
      |      ]
      |    }
      |  ],
      |   "links": [
      |      {
      |		      "href": "/accounts/self-assessment/AA123456A/transactions/1111111111",
      |		      "method": "GET",
      |		      "rel": "self"
      |	     },
      |      {
      |		      "href": "/accounts/self-assessment/AA123456A/payments/P0101180112-000004",
      |		      "method": "GET",
      |		      "rel": "retrieve-payment-allocations"
      |	     }
      |    ]
      |}
    """.stripMargin
  )
}
