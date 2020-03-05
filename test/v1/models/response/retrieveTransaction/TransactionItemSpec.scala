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

package v1.models.response.retrieveTransaction

import play.api.libs.json.{JsError, Json}
import support.UnitSpec
import v1.fixtures.RetrieveTransactionFixture._

class TransactionItemSpec extends UnitSpec {

  "Transaction model" should {
    "return a successful Json model" when {
      "the json contains all fields" in {
        fullDesTransactionItemResponse.as[TransactionItem] shouldBe fullTransactionItemModel
      }

      "the json contains exclusively document Id" in {
        fullDesDocIdTransactionItemResponse.as[TransactionItem] shouldBe fullDocIdTransactionItemModel
      }

      "the json contains exclusively payment lot and payment lot item" in {
        fullDesPaymentLotIdTransactionItemResponse.as[TransactionItem] shouldBe fullTransactionItemModel
      }

      "the json contains only mandatory fields" in {
        minimalDesTransactionItemResponse.as[TransactionItem] shouldBe minimalTransactionItemModel
      }
    }

    "throw an error" when {
      "the json contains a incorrect type" in {
        invalidDesTransactionItemResponse.validate[TransactionItem] shouldBe a[JsError]
      }
    }
  }

  "successfully write the model to Json" when {
    "using a standard Json Owrites" in {
      Json.toJson(fullTransactionItemModel) shouldBe mtdRetrieveTransactionItemResponse
    }
  }
}
