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
import v1.fixtures.RetrieveTransactionDetailsFixture

class SubItemSpec extends UnitSpec {

  val desResponseCharge: JsValue = Json.parse(
    """
      |{
      |  "subItemId": "001",
      |  "amount": 100.11,
      |  "clearingDate": "2021-01-31",
      |  "clearingReason": "Incoming payment",
      |  "paymentAmount": 100.11,
      |  "paymentMethod": "BACS RECEIPTS",
      |  "paymentLot": "P0101180112",
      |  "paymentLotItem": "000001"
      |}
    """.stripMargin
  )

  val desResponsePayment: JsValue = Json.parse(
    """
      |{
      |  "subItemId":"001",
      |  "clearingDate":"2021-01-31",
      |  "clearingReason":"Payment allocation",
      |  "paymentAmount": -1100.00
      |}
    """.stripMargin
  )

  val mtdResponseCharge: JsValue = Json.parse(
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

  val mtdResponsePayment: JsValue = Json.parse(
    """
      |{
      |  "subItemId":"001",
      |  "clearingDate":"2021-01-31",
      |  "clearingReason":"Payment allocation",
      |  "paymentAmount": -1100.00
      |}
    """.stripMargin
  )

  val desResponseInvalid: JsValue = Json.parse(
    """
      |{
      |  "subItemId": "001",
      |  "amount": "asdasd",
      |  "clearingDate": "2021-01-31",
      |  "clearingReason": "Incoming payment",
      |  "paymentAmount": 100.11,
      |  "paymentMethod": "BACS RECEIPTS",
      |  "paymentLot": "P0101180112",
      |  "paymentLotItem": "000001"
      |}
    """.stripMargin
  )

  val desResponseEmpty: JsValue = Json.parse("""{}""")

  "SubItem" when {
    "read from valid JSON" should {
      "produce the expected SubItem object for a charge" in {
        desResponseCharge.as[SubItem] shouldBe RetrieveTransactionDetailsFixture.subItemResponseCharge
      }
    }

    "SubItem" when {
      "read from valid JSON" should {
        "produce the expected SubItem object for a payment" in {
          desResponsePayment.as[SubItem] shouldBe RetrieveTransactionDetailsFixture.subItemResponsePayment
        }
      }

    "read from empty JSON" should {
      "produce an empty SubItem object" in {
        desResponseEmpty.as[SubItem] shouldBe SubItem.empty
      }
    }

    "read from invalid JSON" should {
      "produce an empty ChargeHistory object" in {
        desResponseInvalid.validate[SubItem] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON object for a charge" in {
        Json.toJson(RetrieveTransactionDetailsFixture.subItemResponseCharge) shouldBe mtdResponseCharge
      }
    }
  }

    "written to JSON" should {
      "produce the expected JSON object for a payment" in {
        Json.toJson(RetrieveTransactionDetailsFixture.subItemResponsePayment) shouldBe mtdResponsePayment
      }
    }
  }
}
