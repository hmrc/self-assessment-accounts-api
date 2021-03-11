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

package v1.fixtures.transactionDetails

import play.api.libs.json.{JsValue, Json}
import v1.models.response.retrieveTransactionDetails.{RetrieveTransactionDetailsResponse, SubItem, TransactionItem}

trait RetrieveTransactionDetailsResponseFixture {

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
      |           "paymentLotItem": "000001"
      |         },
      |        {
      |           "subItem":"004",
      |           "clearingDate":"2021-01-31",
      |           "clearingReason":"Payment allocation",
      |           "paymentAmount": -1100.00,
      |           "paymentLot": "P0101180112",
      |           "paymentLotItem": "000001"
      |         }
      |      ]
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val desJsonMultiple: JsValue = Json.parse(
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
      |    },
      |    {
      |      "sapDocumentNumberItem": "0002",
      |      "chargeType": "National Insurance Class 4",
      |      "taxPeriodFrom": "2019-04-06",
      |      "taxPeriodTo": "2020-04-05",
      |      "originalAmount": 100.23,
      |      "outstandingAmount": 10.45,
      |      "items": [
      |        {
      |           "subItem":"003",
      |           "clearingDate":"2021-01-31",
      |           "clearingReason":"Payment allocation",
      |           "paymentAmount": -1100.00,
      |           "dueDate": "2021-01-31",
      |           "paymentLot": "P0101180112",
      |           "paymentLotItem": "000001"
      |         },
      |        {
      |           "subItem":"004",
      |           "clearingDate":"2021-01-31",
      |           "clearingReason":"Payment allocation",
      |           "paymentAmount": -1100.00,
      |           "paymentLot": "P0101180112",
      |           "paymentLotItem": "000001"
      |         }
      |      ]
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val desJsonNoTransactions: JsValue = Json.parse(
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

  val desJsonEmptyTransaction: JsValue = Json.parse(
    """
      |{
      |   "financialDetails": [
      |     {
      |     }
      |   ]
      |}
    """.stripMargin
  )

  val responseModelCharge: RetrieveTransactionDetailsResponse = RetrieveTransactionDetailsResponse(
    transactionItems = Seq(
      TransactionItem(
        transactionItemId = Some("0001"),
        `type` = Some("National Insurance Class 2"),
        taxPeriodFrom = Some("2019-04-06"),
        taxPeriodTo = Some("2020-04-05"),
        originalAmount = Some(100.45),
        outstandingAmount = Some(10.23),
        dueDate = Some("2021-01-31"),
        paymentMethod = Some("BACS RECEIPTS"),
        paymentId = None,
        subItems = Seq(
          SubItem(
            subItemId = Some("002"),
            amount = Some(100.11),
            clearingDate = Some("2021-01-31"),
            clearingReason = Some("Incoming payment"),
            outgoingPaymentMethod = None,
            paymentAmount = Some(100.11),
            dueDate = None,
            paymentMethod = Some("BACS RECEIPTS"),
            paymentId = Some("P0101180112-000001")
          )
        )
      )
    )
  )

  val responseModelPayment: RetrieveTransactionDetailsResponse = RetrieveTransactionDetailsResponse(
    transactionItems = Seq(
      TransactionItem(
        transactionItemId = Some("0002"),
        `type` = Some("National Insurance Class 4"),
        taxPeriodFrom = Some("2019-04-06"),
        taxPeriodTo = Some("2020-04-05"),
        originalAmount = Some(100.23),
        outstandingAmount = Some(10.45),
        dueDate = Some("2021-01-31"),
        paymentMethod = None,
        paymentId = Some("P0101180112-000001"),
        subItems = Seq(
          SubItem(
            subItemId = Some("004"),
            amount = None,
            clearingDate = Some("2021-01-31"),
            clearingReason = Some("Payment allocation"),
            outgoingPaymentMethod = None,
            paymentAmount = Some(-1100),
            dueDate = None,
            paymentMethod = None,
            paymentId = Some("P0101180112-000001")
          )
        )
      )
    )
  )

  val responseModelMultiple: RetrieveTransactionDetailsResponse = RetrieveTransactionDetailsResponse(
    transactionItems =
      responseModelCharge.transactionItems ++
        responseModelPayment.transactionItems
  )

  val responseModelNoTransactions: RetrieveTransactionDetailsResponse = RetrieveTransactionDetailsResponse(
    transactionItems = Seq.empty[TransactionItem]
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
      |      "paymentMethod":"BACS RECEIPTS",
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
      |  ]
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
      |      "paymentId":"P0101180112-000001",
      |      "subItems":[
      |         {
      |           "subItemId":"004",
      |           "clearingDate":"2021-01-31",
      |           "clearingReason":"Payment allocation",
      |           "paymentAmount": -1100.00,
      |           "paymentId":"P0101180112-000001"
      |         }
      |      ]
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val mtdJsonNoTransactions: JsValue = Json.parse(
    """
      |{
      |   "transactionItems": []
      |}
    """.stripMargin
  )

  val mtdJsonMultiple: JsValue = Json.parse(
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
      |    },
      |    {
      |      "transactionItemId": "0002",
      |      "type": "National Insurance Class 4",
      |      "taxPeriodFrom": "2019-04-06",
      |      "taxPeriodTo": "2020-04-05",
      |      "originalAmount": 100.23,
      |      "outstandingAmount": 10.45,
      |      "dueDate": "2021-01-31",
      |      "paymentId": "P0101180112-000001",
      |      "subItems": [
      |        {
      |          "subItemId": "004",
      |          "clearingDate": "2021-01-31",
      |          "clearingReason": "Payment allocation",
      |          "paymentAmount": -1100,
      |          "paymentId": "P0101180112-000001"
      |        }
      |      ]
      |    }
      |  ]
      |}
    """.stripMargin
  )
}
