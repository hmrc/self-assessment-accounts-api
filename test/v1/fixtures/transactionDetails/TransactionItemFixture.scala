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
import v1.models.response.retrieveTransactionDetails.{SubItem, TransactionItem}

trait TransactionItemFixture {

  val desJsonCharge: JsValue = Json.parse(
    """
      |{
      |  "sapDocumentNumberItem": "0001",
      |  "chargeType": "National Insurance Class 2",
      |  "taxPeriodFrom": "2019-04-06",
      |  "taxPeriodTo": "2020-04-05",
      |  "originalAmount": 100.45,
      |  "outstandingAmount": 10.23,
      |  "items": [
      |    {
      |      "subItem": "001",
      |      "amount": 100.11,
      |      "clearingDate": "2021-01-31",
      |      "clearingReason": "Incoming payment",
      |      "paymentAmount": 100.11,
      |      "dueDate": "2018-08-13",
      |      "paymentMethod": "BACS RECEIPTS"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val desJsonPayment: JsValue = Json.parse(
    """
      |{
      |  "sapDocumentNumberItem": "0001",
      |  "chargeType": "Payment on account",
      |  "originalAmount": -5000,
      |  "outstandingAmount": 0,
      |  "items":[
      |     {
      |       "subItem":"001",
      |       "clearingDate":"2021-01-31",
      |       "clearingReason":"Payment allocation",
      |       "paymentAmount": -1100,
      |       "dueDate": "2018-08-13",
      |       "paymentLot": "P0101180112",
      |       "paymentLotItem": "000001"
      |     }
      |  ]
      |}
    """.stripMargin
  )

  val desJsonChargeMultiple: JsValue = Json.parse(
    """
      |{
      |  "sapDocumentNumberItem": "0001",
      |  "chargeType": "National Insurance Class 2",
      |  "taxPeriodFrom": "2019-04-06",
      |  "taxPeriodTo": "2020-04-05",
      |  "originalAmount": 100.45,
      |  "outstandingAmount": 10.23,
      |  "items": [
      |    {
      |      "subItem": "001",
      |      "amount": 100.11,
      |      "clearingDate": "2021-01-31",
      |      "clearingReason": "Incoming payment",
      |      "paymentAmount": 100.11,
      |      "dueDate": "2018-08-13",
      |      "paymentMethod": "BACS RECEIPTS"
      |    },
      |    {
      |      "subItem": "002",
      |      "amount": 100.11,
      |      "clearingDate": "2021-01-31",
      |      "clearingReason": "Incoming payment",
      |      "paymentAmount": 100.11,
      |      "dueDate": "2018-08-13",
      |      "paymentMethod": "BACS RECEIPTS",
      |      "paymentLot": "P0101180112",
      |      "paymentLotItem": "000001"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val desJsonPaymentMultiple: JsValue = Json.parse(
    """
      |{
      |  "sapDocumentNumberItem": "0001",
      |  "chargeType": "Payment on account",
      |  "originalAmount": -5000.00,
      |  "outstandingAmount": 0.00,
      |  "items":[
      |       {
      |       "subItem":"003",
      |       "clearingDate":"2021-01-31",
      |       "clearingReason":"Payment allocation",
      |       "paymentAmount": -1100.00,
      |       "dueDate": "2018-08-13",
      |       "paymentLot": "P0101180112",
      |       "paymentLotItem": "000001"
      |     },
      |     {
      |       "subItem":"001",
      |       "clearingDate":"2021-01-31",
      |       "clearingReason":"Payment allocation",
      |       "paymentAmount": -1100.00,
      |       "dueDate": "2018-08-13",
      |       "paymentLot": "P0101180112",
      |       "paymentLotItem": "000001"
      |     }
      |  ]
      |}
    """.stripMargin
  )

  val desJsonNoItemId: JsValue = Json.parse(
    """
      |{
      |  "sapDocumentNumberItem": "0001",
      |  "chargeType": "National Insurance Class 2",
      |  "taxPeriodFrom": "2019-04-06",
      |  "taxPeriodTo": "2020-04-05",
      |  "originalAmount": 100.45,
      |  "outstandingAmount": 10.23,
      |  "items": [
      |    {
      |      "amount": 100.11,
      |      "clearingDate": "2021-01-31",
      |      "clearingReason": "Incoming payment",
      |      "paymentAmount": 100.11,
      |      "dueDate": "2018-08-13",
      |      "paymentMethod": "BACS RECEIPTS"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val transactionItemModelCharge: TransactionItem = TransactionItem(
    transactionItemId = Some("0001"),
    `type` = Some("National Insurance Class 2"),
    taxPeriodFrom = Some("2019-04-06"),
    taxPeriodTo = Some("2020-04-05"),
    originalAmount = Some(100.45),
    outstandingAmount = Some(10.23),
    dueDate = Some("2018-08-13"),
    paymentMethod = Some("BACS RECEIPTS"),
    paymentId = None,
    subItems = Seq.empty[SubItem]
  )

  val transactionItemModelPayment: TransactionItem = TransactionItem(
    transactionItemId = Some("0001"),
    `type` = Some("Payment on account"),
    taxPeriodFrom = None,
    taxPeriodTo = None,
    originalAmount = Some(-5000),
    outstandingAmount = Some(0),
    dueDate = Some("2018-08-13"),
    paymentMethod = None,
    paymentId = Some("P0101180112-000001"),
    subItems = Seq.empty[SubItem]
  )

  val transactionItemModelChargeMultiple: TransactionItem = transactionItemModelCharge.copy(
    subItems = Seq(
      SubItem(
        subItemId = Some("002"),
        amount = Some(100.11),
        clearingDate = Some("2021-01-31"),
        clearingReason = Some("Incoming payment"),
        outgoingPaymentMethod = None,
        paymentAmount = Some(100.11),
        dueDate = Some("2018-08-13"),
        paymentMethod = Some("BACS RECEIPTS"),
        paymentId = Some("P0101180112-000001")
      )
    )
  )

  val transactionItemModelPaymentMultiple: TransactionItem = transactionItemModelPayment.copy(
    subItems = Seq(
      SubItem(
        subItemId = Some("003"),
        amount = None,
        clearingDate = Some("2021-01-31"),
        clearingReason = Some("Payment allocation"),
        outgoingPaymentMethod = None,
        paymentAmount = Some(-1100),
        dueDate = Some("2018-08-13"),
        paymentMethod = None,
        paymentId = Some("P0101180112-000001")
      )
    )
  )

  val transactionItemModelNoSubItems: TransactionItem = TransactionItem(
    transactionItemId = Some("0001"),
    `type` = Some("National Insurance Class 2"),
    taxPeriodFrom = Some("2019-04-06"),
    taxPeriodTo = Some("2020-04-05"),
    originalAmount = Some(100.45),
    outstandingAmount = Some(10.23),
    dueDate = None,
    paymentMethod = None,
    paymentId = None,
    subItems = Seq(SubItem(None,
      Some(100.11),
      Some("2021-01-31"),
      Some("Incoming payment"),
      None,
      Some(100.11),
      Some("2018-08-13"),
      Some("BACS RECEIPTS"),
      None))
  )

  val mtdJsonCharge: JsValue = Json.parse(
    """
      |{
      |  "transactionItemId": "0001",
      |  "type": "National Insurance Class 2",
      |  "taxPeriodFrom": "2019-04-06",
      |  "taxPeriodTo": "2020-04-05",
      |  "originalAmount": 100.45,
      |  "outstandingAmount": 10.23,
      |  "paymentMethod": "BACS RECEIPTS",
      |  "dueDate": "2018-08-13",
      |  "subItems": [
      |  ]
      |}
    """.stripMargin
  )

  val mtdJsonPayment: JsValue = Json.parse(
    """
      |{
      |  "transactionItemId": "0001",
      |  "type": "Payment on account",
      |  "originalAmount": -5000.00,
      |  "outstandingAmount": 0.00,
      |  "dueDate": "2018-08-13",
      |  "paymentId": "P0101180112-000001",
      |  "subItems":[
      |  ]
      |}
    """.stripMargin
  )

  val mtdJsonChargeMultiple: JsValue = Json.parse(
    """
      |{
      |  "transactionItemId": "0001",
      |  "type": "National Insurance Class 2",
      |  "taxPeriodFrom": "2019-04-06",
      |  "taxPeriodTo": "2020-04-05",
      |  "originalAmount": 100.45,
      |  "outstandingAmount": 10.23,
      |  "paymentMethod":"BACS RECEIPTS",
      |  "dueDate": "2018-08-13",
      |  "subItems": [
      |    {
      |      "subItemId": "002",
      |      "amount": 100.11,
      |      "clearingDate": "2021-01-31",
      |      "clearingReason": "Incoming payment",
      |      "paymentAmount": 100.11,
      |      "paymentMethod": "BACS RECEIPTS",
      |      "paymentId": "P0101180112-000001"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val mtdJsonPaymentMultiple: JsValue = Json.parse(
    """
      |{
      |  "transactionItemId": "0001",
      |  "type": "Payment on account",
      |  "originalAmount": -5000.00,
      |  "outstandingAmount": 0.00,
      |  "dueDate": "2018-08-13",
      |  "paymentId": "P0101180112-000001",
      |  "subItems":[
      |     {
      |       "subItemId":"003",
      |       "clearingDate":"2021-01-31",
      |       "clearingReason":"Payment allocation",
      |       "paymentAmount": -1100.00,
      |       "paymentId": "P0101180112-000001"
      |     }
      |  ]
      |}
    """.stripMargin
  )

  val mtdJsonNoSubItems: JsValue = Json.parse(
    """
      |{
      |   "transactionItemId": "0001",
      |   "type": "National Insurance Class 2",
      |   "taxPeriodFrom": "2019-04-06",
      |   "taxPeriodTo": "2020-04-05",
      |   "originalAmount": 100.45,
      |   "outstandingAmount": 10.23,
      |   "subItems": [{
      |			"amount": 100.11,
      |			"clearingReason": "Incoming payment",
      |			"clearingDate": "2021-01-31",
      |			"paymentMethod": "BACS RECEIPTS",
      |			"paymentAmount": 100.11
      |		}]
      |}
    """.stripMargin
  )
}
