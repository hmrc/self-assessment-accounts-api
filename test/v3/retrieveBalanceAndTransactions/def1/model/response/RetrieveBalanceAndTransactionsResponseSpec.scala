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

package v3.retrieveBalanceAndTransactions.def1.model.response

import play.api.libs.json.Json
import shared.utils.UnitSpec
import v3.retrieveBalanceAndTransactions.def1.model.ResponseFixture._
import v3.retrieveBalanceAndTransactions.model.response.RetrieveBalanceAndTransactionsResponse

class RetrieveBalanceAndTransactionsResponseSpec extends UnitSpec {

  "RetrieveBalanceAndTransactionsResponse.reads" when {
    "the feature switch is disabled (IFS enabled)" when {
      "locks are included" when {
        implicit val readLocks: FinancialDetailsItem.ReadLocks = FinancialDetailsItem.ReadLocks(true)

        "passed a valid JSON document" should {
          "return a fully populated object" in {
            downstreamResponseJson.as[RetrieveBalanceAndTransactionsResponse] shouldBe response
          }

          "return a minimally populated object" in {
            minimalDownstreamResponseJson.as[RetrieveBalanceAndTransactionsResponse] shouldBe minimalResponse
          }
        }
      }

      "locks are included" should {
        implicit val readLocks: FinancialDetailsItem.ReadLocks = FinancialDetailsItem.ReadLocks(false)

        "exclude locks" should {
          "return the object without locks" in {
            downstreamResponseJson.as[RetrieveBalanceAndTransactionsResponse] shouldBe responseWithoutLocks
          }
        }
      }
    }
    "the feature switch is enabled (HIP enabled)" when {
      "locks are included" when {
        implicit val readLocks: FinancialDetailsItem.ReadLocks = FinancialDetailsItem.ReadLocks(true)

        "passed a valid JSON document" should {
          "return a fully populated object" in {
            downstreamResponseHipJson.as[RetrieveBalanceAndTransactionsResponse] shouldBe response
          }

          "return a minimally populated object" in {
            minimalDownstreamResponseHipJson.as[RetrieveBalanceAndTransactionsResponse] shouldBe minimalResponse
          }
        }
      }

      "locks are included" should {
        implicit val readLocks: FinancialDetailsItem.ReadLocks = FinancialDetailsItem.ReadLocks(false)

        "exclude locks" should {
          "return the object without locks" in {
            downstreamResponseHipJson.as[RetrieveBalanceAndTransactionsResponse] shouldBe responseWithoutLocks
          }
        }
      }
    }
  }

  "RetrieveBalanceAndTransactionsResponse.writes" should {
    "produce the expected JSON" in {
      Json.toJson(response) shouldBe mtdResponseJson
    }
  }

}
