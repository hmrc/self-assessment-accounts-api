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

package v3.retrieveChargeHistoryByTransactionId.model.responses

import play.api.libs.json.Json
import shared.utils.UnitSpec
import v3.retrieveChargeHistoryByTransactionId.def1.RetrieveChargeHistoryFixture._
import v3.retrieveChargeHistoryByTransactionId.model.response.RetrieveChargeHistoryResponse

class RetrieveChargeHistoryByTransactionResponseSpec extends UnitSpec {

  val validObjectSingle: RetrieveChargeHistoryResponse = RetrieveChargeHistoryResponse(List(validChargeHistoryDetailObject))
  val validObjectMultiple: RetrieveChargeHistoryResponse = validChargeHistoryResponseObject

  "Def1_RetrieveChargeHistoryResponse" when {
    "reading valid JSON" should {
      "return the expected object" in {
        downstreamResponse.as[RetrieveChargeHistoryResponse] shouldBe validObjectSingle
      }
    }
  }

  "Def1_RetrieveChargeHistoryResponse" when {
    "reading valid JSON with multiple charge history details" should {
      "return the expected object" in {
        downstreamResponseMultiple.as[RetrieveChargeHistoryResponse] shouldBe validObjectMultiple
      }
    }
  }

  "Written to JSON" should {
    "produce the expected JSON" in {
      Json.toJson(validObjectSingle) shouldBe mtdSingleResponse
    }
  }

}
