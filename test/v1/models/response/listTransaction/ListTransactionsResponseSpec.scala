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

package v1.models.response.listTransaction

import play.api.libs.json.{JsError, Json}
import support.UnitSpec
import v1.fixtures.ListTransactionFixture._

class ListTransactionsResponseSpec extends UnitSpec {

  "ListTransactionsResponse" should {
    "return a successful Json model" when {
      "the json contains all fields with a document id transaction" in {
        fullDesSingleListTransactionResponse.as[ListTransactionsResponse[TransactionItem]] shouldBe fullSingleListTransactionModel
      }

      "the json contains all fields with a multiple transaction" in {
        fullDesMultipleListTransactionMultipleResponse.as[ListTransactionsResponse[TransactionItem]] shouldBe fullMultipleListTransactionModel
      }

      "the json contains minimal fields with no transaction" in {
        minimalDesListTransactionResponse.as[ListTransactionsResponse[TransactionItem]] shouldBe minimalListTransactionModel
      }
    }

    "return a successful empty Json model" when {
      "a transactionItem is present but empty" in {
        emptyItemDesListTransactionResponse.as[ListTransactionsResponse[TransactionItem]] shouldBe minimalListTransactionModel
      }
    }

    "throw an error" when {
      "there are no mandatory fields" in {
        invalidDesListTransactionResponse.validate[ListTransactionsResponse[TransactionItem]] shouldBe a[JsError]
      }
    }

    "successfully write the model to Json" when {
      "using a standard Json Owrites" in {
        Json.toJson(fullSingleListTransactionModel) shouldBe mtdListTransactionResponse
      }
    }
  }
}
