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

package v1.models.response.retrieveTransactionDetails

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec
import v1.fixtures.RetrieveTransactionDetailsFixture._

class TransactionItemSpec extends UnitSpec {

  val desResponseCharge: JsValue = Json.parse(
    """
      |{
      |  "sapDocumentItemId": "0001",
      |  "type": "National Insurance Class 2",
      |  "taxPeriodFrom": "2019-04-06",
      |  "taxPeriodTo": "2020-04-05",
      |  "originalAmount": 100.45,
      |  "outstandingAmount": 10.23,
      |  "dueDate": "2021-01-31",
      |  "subItems": [
      |    {
      |      "subItemId": "001",
      |      "amount": 100.11,
      |      "clearingDate": "2021-01-31",
      |      "clearingReason": "Incoming payment",
      |      "paymentAmount": 100.11,
      |      "paymentMethod": "BACS RECEIPTS",
      |      "paymentLot": "P0101180112",
      |      "paymentLotItem": "000001"
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val desResponsePayment: JsValue = Json.parse(
    """
      |{
      |  "sapDocumentItemId": "0001",
      |  "type": "Payment on account",
      |  "originalAmount": -5000,
      |  "outstandingAmount": 0,
      |  "dueDate": "2021-01-31",
      |  "paymentMethod":"BACS RECEIPTS",
      |  "paymentLot": "P0101180112",
      |  "paymentLotItem": "000001",
      |  "subItems":[
      |     {
      |       "subItemId":"001",
      |       "clearingDate":"2021-01-31",
      |       "clearingReason":"Payment allocation",
      |       "paymentAmount": -1100
      |     }
      |  ]
      |}
    """.stripMargin
  )

  val mtdResponseCharge: JsValue = Json.parse(
    """
      |{
      |  "transactionItemId": "0001",
      |  "type": "National Insurance Class 2",
      |  "taxPeriodFrom": "2019-04-06",
      |  "taxPeriodTo": "2020-04-05",
      |  "originalAmount": 100.45,
      |  "outstandingAmount": 10.23,
      |  "dueDate": "2021-01-31",
      |  "subItems": [
      |    {
      |      "subItemId": "001",
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

  val mtdResponsePayment: JsValue = Json.parse(
    """
      |{
      |  "transactionItemId": "0001",
      |  "type": "Payment on account",
      |  "originalAmount": -5000.00,
      |  "outstandingAmount": 0.00,
      |  "dueDate": "2021-01-31",
      |  "paymentMethod":"BACS RECEIPTS",
      |  "paymentId":"P0101180112-000001",
      |  "subItems":[
      |     {
      |       "subItemId":"001",
      |       "clearingDate":"2021-01-31",
      |       "clearingReason":"Payment allocation",
      |       "paymentAmount": -1100.00
      |     }
      |  ]
      |}
    """.stripMargin
  )

  val desResponseEmptyTransactionItemDetailsArray: JsValue = Json.parse(
    """
      |{
      |   "sapDocumentItemId": "0001",
      |   "type": "National Insurance Class 2",
      |   "taxPeriodFrom": "2019-04-06",
      |   "taxPeriodTo": "2020-04-05",
      |   "originalAmount": 100.45,
      |   "outstandingAmount": 10.23,
      |   "dueDate": "2021-01-31",
      |   "subItems": []
      |}
    """.stripMargin
  )

  val mtdResponseEmptysubItemsArray: JsValue = Json.parse(
    """
      |{
      |   "transactionItemId": "0001",
      |   "type": "National Insurance Class 2",
      |   "taxPeriodFrom": "2019-04-06",
      |   "taxPeriodTo": "2020-04-05",
      |   "originalAmount": 100.45,
      |   "outstandingAmount": 10.23,
      |   "dueDate": "2021-01-31",
      |   "subItems": []
      |}
    """.stripMargin
  )

  val desResponseEmptyTransactionItemDetailsObject: JsValue = Json.parse(
    """
      |{
      |   "sapDocumentItemId": "0001",
      |   "type": "National Insurance Class 2",
      |   "taxPeriodFrom": "2019-04-06",
      |   "taxPeriodTo": "2020-04-05",
      |   "originalAmount": 100.45,
      |   "outstandingAmount": 10.23,
      |   "dueDate": "2021-01-31",
      |   "subItems": [
      |     {
      |     }
      |   ]
      |}
    """.stripMargin
  )

  val desResponseEmptySubItems: JsValue = Json.parse(
    """
      |{
      |   "sapDocumentItemId": "0001",
      |   "type": "National Insurance Class 2",
      |   "taxPeriodFrom": "2019-04-06",
      |   "taxPeriodTo": "2020-04-05",
      |   "originalAmount": 100.45,
      |   "outstandingAmount": 10.23,
      |   "dueDate": "2021-01-31",
      |   "subItems": []
      |}
    """.stripMargin
  )

  val mtdReponseEmptySubItems: JsValue = Json.parse(
    """
      |{
      |   "transactionItemId": "0001",
      |   "type": "National Insurance Class 2",
      |   "taxPeriodFrom": "2019-04-06",
      |   "taxPeriodTo": "2020-04-05",
      |   "originalAmount": 100.45,
      |   "outstandingAmount": 10.23,
      |   "dueDate": "2021-01-31",
      |   "subItems": []
      |}
    """.stripMargin
  )

  val desResponseEmptySubItemsObject: JsValue = Json.parse(
    """
      |{
      |   "sapDocumentItemId": "0001",
      |   "type": "National Insurance Class 2",
      |   "taxPeriodFrom": "2019-04-06",
      |   "taxPeriodTo": "2020-04-05",
      |   "originalAmount": 100.45,
      |   "outstandingAmount": 10.23,
      |   "dueDate": "2021-01-31",
      |   "subItems": [
      |     {
      |     }
      |   ]
      |}
    """.stripMargin
  )

  val desResponseEmpty: JsValue = Json.parse("""{}""")

  val TransactionItemResponseEmpty: TransactionItem =
    TransactionItem(
      transactionItemId = Some("0001"),
      `type` = Some("National Insurance Class 2"),
      taxPeriodFrom = Some("2019-04-06"),
      taxPeriodTo = Some("2020-04-05"),
      originalAmount = Some(100.45),
      outstandingAmount = Some(10.23),
      dueDate = Some("2021-01-31"),
      paymentMethod = None,
      paymentId = None,
      subItems = Some(Seq())
    )

  "RetrieveTransactionItemResponse" when {
    "read from valid JSON" should {
      "produce the expected TransactionItem object for a charge" in {
        desResponseCharge.as[TransactionItem] shouldBe transactionItemResponseCharge
      }
    }
  }

    "RetrieveTransactionItemResponse" when {
      "read from valid JSON" should {
        "produce the expected TransactionItem object for a payment" in {
          desResponsePayment.as[TransactionItem] shouldBe transactionItemResponsePayment
        }
      }
    }

    "read from valid JSON with multiple sub items" should {
      "produce the expected TransactionItem object for a charge" in {
        desResponseWithMultipleSubItemForCharge.as[TransactionItem] shouldBe retrieveSubItemsResponseChargeMultiple
      }
    }

  "read from valid JSON with multiple sub items" should {
    "produce the expected TransactionItem object for a payment" in {
      desResponseWithMultipleSubItemForPayment.as[TransactionItem] shouldBe retrieveSubItemsResponsePaymentMultiple
    }
  }

  "read from valid JSON with an empty Sub Items array" should {
    "produce the expected TransactionItem object" in {
      desResponseEmptySubItems.as[TransactionItem] shouldBe TransactionItemResponseEmpty
    }
  }

    "read from empty JSON" should {
      "produce an empty TransactionItem object" in {
        desResponseEmpty.as[TransactionItem] shouldBe TransactionItem.empty
      }
    }

  "read from valid JSON with an empty sub item" should {
    "not read empty sub items" in {
      desResponseEmptySubItemsObject.as[TransactionItem] shouldBe TransactionItemResponseEmpty
    }
  }

    "written to JSON" should {
      "produce the expected JSON object for a charge" in {
        Json.toJson(transactionItemResponseCharge) shouldBe mtdResponseCharge
      }
    }

    "written to JSON" should {
      "produce the expected JSON object for a payment" in {
        Json.toJson(transactionItemResponsePayment) shouldBe mtdResponsePayment
      }
    }

  "written to JSON (multiple sub items)" should {
    "produce the expected JSON for a charge" in {
      Json.toJson(retrieveSubItemsResponseChargeMultiple) shouldBe mtdResponseWithMultipleSubItemForCharge
    }
  }

  "written to JSON (multiple sub items)" should {
    "produce the expected JSON for a payment" in {
      Json.toJson(retrieveSubItemsResponsePaymentMultiple) shouldBe mtdResponseWithMultipleSubItemForPayment
    }
  }

  "written to JSON (empty sub items array)" should {
    "produce the expected JSON" in {
      Json.toJson(TransactionItemResponseEmpty) shouldBe mtdReponseEmptySubItems
    }
  }
}
