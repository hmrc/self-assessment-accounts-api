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

class RetrieveTransactionsResponseSpec extends UnitSpec {

  "RetrieveTransactionsResponse" should {
    "return a successful Json model" when {
      "the json contains all fields with a document id transaction" in {
        fullDesSingleRetreiveTransactionResponse.as[RetrieveTransactionsResponse[TransactionItem]] shouldBe fullSingleRetreiveTransactionModel
      }

      "the json contains all fields with a multiple transaction" in {
        fullDesMultipleRetreiveTransactionMultipleResponse.as[RetrieveTransactionsResponse[TransactionItem]] shouldBe fullMultipleRetreiveTransactionModel
      }

      "the json contains minimal fields with no transaction" in {
        minimalDesRetreiveTransactionResponse.as[RetrieveTransactionsResponse[TransactionItem]] shouldBe minimalRetreiveTransactionModel
      }
    }

    "return a successful empty Json model" when {
      "a transactionItem is present but empty" in {
        emptyItemDesRetrieveTransactionResponse.as[RetrieveTransactionsResponse[TransactionItem]] shouldBe minimalRetreiveTransactionModel
      }
    }

    "throw an error" when {
      "there are no mandatory fields" in {
        invalidDesRetrieveTransactionResponse.validate[RetrieveTransactionsResponse[TransactionItem]] shouldBe a[JsError]
      }
    }

    "successfully write the model to Json" when {
      "using a standard Json Owrites" in {
        Json.toJson(fullSingleRetreiveTransactionModel) shouldBe mtdRetrievetransactionResponse
      }
    }
  }
}