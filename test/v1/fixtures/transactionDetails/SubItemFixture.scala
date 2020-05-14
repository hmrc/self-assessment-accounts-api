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

package v1.fixtures.transactionDetails

import play.api.libs.json.{JsValue, Json}
import v1.models.response.retrieveTransactionDetails.SubItem

trait SubItemFixture {

  val desJsonChargeItem: JsValue = Json.parse(
    """
      |{
      |  "subItem": "001",
      |  "amount": 100.11,
      |  "clearingDate": "2021-01-31",
      |  "clearingReason": "Incoming payment",
      |  "paymentAmount": 100.11,
      |  "dueDate": "2018-08-13",
      |  "paymentMethod": "BACS RECEIPTS",
      |  "paymentLot": "P0101180112",
      |  "paymentLotItem": "000001"
      |}
    """.stripMargin
  )

  val desJsonPaymentItem: JsValue = Json.parse(
    """
      |{
      |  "subItem":"001",
      |  "clearingDate":"2021-01-31",
      |  "clearingReason":"Payment allocation",
      |  "paymentAmount": -1100.00,
      |  "dueDate": "2018-08-13"
      |}
    """.stripMargin
  )

  val desJsonInvalid: JsValue = Json.parse(
    """
      |{
      |  "subItem": "001",
      |  "amount": "asdasd",
      |  "clearingDate": "2021-01-31",
      |  "clearingReason": "Incoming payment",
      |  "paymentAmount": 100.11,
      |  "dueDate": "2018-08-13",
      |  "paymentMethod": "BACS RECEIPTS",
      |  "paymentLot": "P0101180112",
      |  "paymentLotItem": "000001"
      |}
    """.stripMargin
  )

  val desJsonInvalidId: JsValue = Json.parse(
    """
      |{
      |  "subItem": "nonNumeric",
      |  "amount": "asdasd",
      |  "clearingDate": "2021-01-31",
      |  "clearingReason": "Incoming payment",
      |  "paymentAmount": 100.11,
      |  "dueDate": "2018-08-13",
      |  "paymentMethod": "BACS RECEIPTS",
      |  "paymentLot": "P0101180112",
      |  "paymentLotItem": "000001"
      |}
    """.stripMargin
  )

  val subItemModelPayment: SubItem = SubItem(
    subItemId = Some("001"),
    amount = None,
    clearingDate = Some("2021-01-31"),
    clearingReason = Some("Payment allocation"),
    outgoingPaymentMethod = None,
    paymentAmount = Some(-1100),
    dueDate = Some("2018-08-13"),
    paymentMethod = None,
    paymentId = None
  )

  val subItemModelCharge: SubItem = SubItem(
    subItemId = Some("001"),
    amount = Some(100.11),
    clearingDate = Some("2021-01-31"),
    clearingReason = Some("Incoming payment"),
    outgoingPaymentMethod = None,
    paymentAmount = Some(100.11),
    dueDate = Some("2018-08-13"),
    paymentMethod = Some("BACS RECEIPTS"),
    paymentId = Some("P0101180112-000001")
  )

  val mtdJsonChargeItem: JsValue = Json.parse(
    """
      |{
      |  "subItemId": "001",
      |  "amount": 100.11,
      |  "clearingDate": "2021-01-31",
      |  "clearingReason": "Incoming payment",
      |  "paymentAmount": 100.11,
      |  "paymentMethod": "BACS RECEIPTS",
      |  "paymentId": "P0101180112-000001"
      |}
    """.stripMargin
  )

  val mtdJsonPaymentItem: JsValue = Json.parse(
    """
      |{
      |  "subItemId":"001",
      |  "clearingDate":"2021-01-31",
      |  "clearingReason":"Payment allocation",
      |  "paymentAmount": -1100.00
      |}
    """.stripMargin
  )
}
