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

import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec
import v1.fixtures.RetrieveTransactionDetailsFixture._

class RetrieveTransactionDetailsResponseSpec extends UnitSpec {

  val desResponseCharge: JsValue = Json.parse(
    """
      |{
      |  "transactionItems": [
      |    {
      |      "sapDocumentItemId": "0001",
      |      "type": "National Insurance Class 2",
      |      "taxPeriodFrom": "2019-04-06",
      |      "taxPeriodTo": "2020-04-05",
      |      "originalAmount": 100.45,
      |      "outstandingAmount": 10.23,
      |      "dueDate": "2021-01-31",
      |      "subItems": [
      |        {
      |          "subItemId": "001",
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

  val desResponsePayment: JsValue = Json.parse(
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
      |      "paymentLot": "P0101180112",
      |      "paymentLotItem": "000001",
      |      "subItems":[
      |         {
      |           "subItemId":"001",
      |           "clearingDate":"2021-01-31",
      |           "clearingReason":"Payment allocation",
      |           "paymentAmount": -1100.00
      |         }
      |      ]
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val mtdResponseCharge: JsValue = Json.parse(
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
      |      "subItems": [
      |        {
      |          "subItemId": "001",
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

  val mtdResponsePayment: JsValue = Json.parse(
    """
      |{
      |  "transactionItems": [
      |    {
      |      "transactionItemId": "0001",
      |      "type": "Payment on account",
      |      "originalAmount": -5000.00,
      |      "outstandingAmount": 0.00,
      |      "dueDate": "2021-01-31",
      |      "paymentMethod":"BACS RECEIPTS",
      |      "paymentId":"P0101180112-000001",
      |      "subItems":[
      |         {
      |           "subItemId":"001",
      |           "clearingDate":"2021-01-31",
      |           "clearingReason":"Payment allocation",
      |           "paymentAmount": -1100.00
      |         }
      |      ]
      |    }
      |  ]
      |}
    """.stripMargin
  )

  val desResponseEmptyTransactionItems: JsValue = Json.parse(
    """
      |{
      |   "transactionItems": []
      |}
    """.stripMargin
  )

  val mtdResponseEmptyTransactionItems: JsValue = Json.parse(
    """
      |{
      |   "transactionItems": []
      |}
    """.stripMargin
  )

  val desResponseEmptyTransactionItemObject: JsValue = Json.parse(
    """
      |{
      |   "transactionItems": [
      |     {
      |     }
      |   ]
      |}
    """.stripMargin
  )

  val desReponseEmpty: JsValue = Json.parse("""{}""")

  val retrieveTransactionDetailsResponseEmpty: RetrieveTransactionDetailsResponse =
  RetrieveTransactionDetailsResponse(
    transactionItems = Seq()
  )

  "RetrieveTransactionDetailsChargeResponse" when {
    "read from valid JSON" should {
      "produce the expected RetrieveTransactionDetailsResponse object for a charge" in {
        desResponseCharge.as[RetrieveTransactionDetailsResponse] shouldBe retrieveTransactionDetailsResponseCharge
      }
    }
  }

    "RetrieveTransactionDetailsPaymentResponse" when {
      "read from valid JSON" should {
        "produce the expected RetrieveTransactionDetailsResponse object for a payment" in {
          desResponsePayment.as[RetrieveTransactionDetailsResponse] shouldBe retrieveTransactionDetailsResponsePayment
        }
      }
    }

      "read from valid JSON with multiple transaction item" should {
        "produce the expected RetrieveTransactionDetailsResponse object for a charge" in {
          desResponseWithMultipleTransactionItemForCharges.as[RetrieveTransactionDetailsResponse] shouldBe retrieveTransactionDetailsResponseChargeMultiple
        }
      }

    "read from valid JSON with an empty Transaction Items array" should {
      "produce the expected RetrieveTransactionDetailsResponse object" in {
        desResponseEmptyTransactionItems.as[RetrieveTransactionDetailsResponse] shouldBe retrieveTransactionDetailsResponseEmpty
      }
    }

    "read from empty JSON" should {
      "produce a JsError" in {
        desReponseEmpty.validate[RetrieveTransactionDetailsResponse] shouldBe a[JsError]
      }
    }

    "read from valid JSON with an empty transaction item" should {
      "not read empty transaction details items" in {
        desResponseEmptyTransactionItemObject.as[RetrieveTransactionDetailsResponse] shouldBe retrieveTransactionDetailsResponseEmpty
      }
    }

    "written to JSON" should {
      "produce the expected JSON for a charge" in {
        Json.toJson(retrieveTransactionDetailsResponseCharge) shouldBe mtdResponseCharge
      }
    }

    "written to JSON" should {
      "produce the expected JSON for a payment" in {
        Json.toJson(retrieveTransactionDetailsResponsePayment) shouldBe mtdResponsePayment
      }
    }

    "written to JSON (multiple transaction items)" should {
      "produce the expected JSON for a charge" in {
        Json.toJson(retrieveTransactionDetailsResponseChargeMultiple) shouldBe mtdResponseWithMultipleTransactionItemForCharges
      }
    }

    "written to JSON (empty transaction items array)" should {
      "produce the expected JSON" in {
        Json.toJson(retrieveTransactionDetailsResponseEmpty) shouldBe mtdResponseEmptyTransactionItems
      }
    }
}
