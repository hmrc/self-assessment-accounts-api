/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.libs.json.Json
import support.UnitSpec
import v1.fixtures.transactionDetails.TransactionItemFixture

class TransactionItemSpec extends UnitSpec with TransactionItemFixture {

  "RetrieveTransactionItemResponse" when {
    "read from valid JSON (single charge)" should {
      "produce the expected TransactionItem object" in {
        desJsonCharge.as[TransactionItem] shouldBe transactionItemModelCharge
      }
    }

    "read from valid JSON (single payment)" should {
      "produce the expected TransactionItem object" in {
        desJsonPayment.as[TransactionItem] shouldBe transactionItemModelPayment
      }
    }

    "read from valid JSON (multiple charges)" should {
      "produce the expected TransactionItem object" in {
        desJsonChargeMultiple.as[TransactionItem] shouldBe transactionItemModelChargeMultiple
      }
    }

    "read from valid JSON (multiple payments)" should {
      "produce the expected TransactionItem object" in {
        desJsonPaymentMultiple.as[TransactionItem] shouldBe transactionItemModelPaymentMultiple
      }
    }

    "read from valid JSON with an empty subItems array" should {
      "produce the expected TransactionItem object" in {
        desJsonNoSubItems.as[TransactionItem] shouldBe transactionItemModelNoSubItems.copy(subItems = Seq.empty[SubItem])
      }
    }

    "read from empty JSON" should {
      "produce an empty TransactionItem object" in {
        desJsonEmptyTransactionItem.as[TransactionItem] shouldBe TransactionItem.empty
      }
    }

    "read from valid JSON without SubItem IDs" should {
      "produce the expected TransactionItem object" in {
        desJsonNoItemId.as[TransactionItem] shouldBe transactionItemModelNoSubItems.copy(subItems = Seq.empty[SubItem])
      }
    }

    "read from valid JSON with an empty sub item" should {
      "not read empty sub items" in {
        desJsonEmptySubItem.as[TransactionItem] shouldBe transactionItemModelNoSubItems.copy(subItems = Seq.empty[SubItem])
      }
    }

    "written to JSON (single charge)" should {
      "produce the expected JSON object" in {
        Json.toJson(transactionItemModelCharge) shouldBe mtdJsonCharge
      }
    }

    "written to JSON (single payment)" should {
      "produce the expected JSON object" in {
        Json.toJson(transactionItemModelPayment) shouldBe mtdJsonPayment
      }
    }

    "written to JSON (multiple charge sub items)" should {
      "produce the expected JSON" in {
        Json.toJson(transactionItemModelChargeMultiple) shouldBe mtdJsonChargeMultiple
      }
    }

    "written to JSON (multiple payment sub items)" should {
      "produce the expected JSON" in {
        Json.toJson(transactionItemModelPaymentMultiple) shouldBe mtdJsonPaymentMultiple
      }
    }

    "written to JSON (empty sub items array)" should {
      "produce the expected JSON" in {
        Json.toJson(transactionItemModelNoSubItems) shouldBe mtdJsonNoSubItems
      }
    }

    "returnLowestNumberedItem" should {
      "return an empty item when neither supplied SubItem has an id" in {
        TransactionItem.returnLowestNumberedItem(SubItem.empty, SubItem.empty) shouldBe SubItem.empty
      }

      "return the lowest numbered SubItem when both have an id" in {

        val lowestNumberedItem = TransactionItem.returnLowestNumberedItem(
          SubItem.empty.copy(subItemId = Some("001")),
          SubItem.empty.copy(subItemId = Some("002"))
        )

        lowestNumberedItem shouldBe SubItem.empty.copy(subItemId = Some("001"))
      }

      "return the subItem which has an id when the other one does not" in {

        val lowestNumberedItem1 = TransactionItem.returnLowestNumberedItem(
          SubItem.empty,
          SubItem.empty.copy(subItemId = Some("001"))
        )

        val lowestNumberedItem2 = TransactionItem.returnLowestNumberedItem(
          SubItem.empty.copy(subItemId = Some("001")),
          SubItem.empty
        )

        lowestNumberedItem1 shouldBe SubItem.empty.copy(subItemId = Some("001"))
        lowestNumberedItem2 shouldBe SubItem.empty.copy(subItemId = Some("001"))
      }
    }
  }

}
