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

package v1.models.response.listTransaction

import play.api.libs.json.{JsError, Json}
import support.UnitSpec
import v1.fixtures.ListTransactionsFixture._

class ListTransactionsResponseSpec extends UnitSpec {

  "ListTransactionsResponse" should {
    "return a successful Json model" when {
      "the json contains all fields with a document id transaction" in {
        fullDesSingleListTransactionsResponse.as[ListTransactionsResponse[TransactionItem]] shouldBe fullSingleListTransactionsModel
      }

      "the json contains all fields with a multiple transaction" in {
        fullDesMultipleListTransactionsMultipleResponse.as[ListTransactionsResponse[TransactionItem]] shouldBe fullMultipleListTransactionsModel
      }

      "the json contains no transaction items" in {
        minimalDesListTransactionsResponse.as[ListTransactionsResponse[TransactionItem]] shouldBe minimalListTransactionsModel
      }
    }

    "return a successful Json model" when {
      "a transaction item is present but with only mandatory fields" in {
        minimalItemListTransactionsDesResponse.as[ListTransactionsResponse[TransactionItem]] shouldBe minimalItemListTransactionsModel
      }
    }

    "throw an error" when {
      "there are no mandatory fields" in {
        invalidDesListTransactionsResponse.validate[ListTransactionsResponse[TransactionItem]] shouldBe a[JsError]
      }
    }

    "successfully write the model to Json" when {
      "using a standard Json Owrites" in {
        Json.toJson(fullSingleListTransactionsModel) shouldBe mtdListTransactionsResponse
      }
    }
  }
}