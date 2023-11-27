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

package v3.models.response.retrieveBalanceAndTransactions

import play.api.libs.json.{JsObject, JsValue, Json}
import support.UnitSpec
import v3.fixtures.retrieveBalanceAndTransactions.FinancialDetailsItemFixture

class FinancialDetailsItemLocksSpec extends UnitSpec with FinancialDetailsItemFixture {

  "FinancialDetailsItemLocks" when {

    "written to MTD JSON" must {
      "work" in {
        Json.toJson(financialDetailsItemLocks) shouldBe financialDetailsItemLocksMtdJson
      }
    }

    "read from downstream" when {
      "all fields present" must {
        "work" in {
          financialDetailsItemDownstreamJson.as[FinancialDetailsItemLocks] shouldBe financialDetailsItemLocks
        }

        "converting paymentLock to isChargeOnHold" must {
          def json(value: String): JsValue = Json.parse(s"""{ "paymentLock": "$value" }""")

          def model(value: Boolean): FinancialDetailsItemLocks = financialDetailsItemLocksFalse.copy(isChargeOnHold = value)

          "convert non-empty string to true" in {
            json("ANYTHING").as[FinancialDetailsItemLocks] shouldBe model(true)
          }

          "convert empty string to false" in {
            json("").as[FinancialDetailsItemLocks] shouldBe model(false)
          }

          "convert absent field to false" in {
            JsObject.empty.as[FinancialDetailsItemLocks] shouldBe model(false)
          }
        }

        "converting clearingLock to isEstimatedChargeOnHold" must {
          def json(value: String): JsValue = Json.parse(s"""{ "clearingLock": "$value" }""")

          def model(value: Boolean): FinancialDetailsItemLocks = financialDetailsItemLocksFalse.copy(isEstimatedChargeOnHold = value)

          "convert non-empty string to true" in {
            json("ANYTHING").as[FinancialDetailsItemLocks] shouldBe model(true)
          }

          "convert empty string to false" in {
            json("").as[FinancialDetailsItemLocks] shouldBe model(false)
          }

          "convert absent field to false" in {
            JsObject.empty.as[FinancialDetailsItemLocks] shouldBe model(false)
          }
        }

        "converting interestLock to isInterestAccrualOnHold" must {
          def json(value: String): JsValue = Json.parse(s"""{ "interestLock": "$value" }""")

          def model(value: Boolean): FinancialDetailsItemLocks = financialDetailsItemLocksFalse.copy(isInterestAccrualOnHold = value)

          "convert non-empty string to true" in {
            json("ANYTHING").as[FinancialDetailsItemLocks] shouldBe model(true)
          }

          "convert empty string to false" in {
            json("").as[FinancialDetailsItemLocks] shouldBe model(false)
          }

          "convert absent field to false" in {
            JsObject.empty.as[FinancialDetailsItemLocks] shouldBe model(false)
          }
        }

        "converting dunningLock to isInterestChargeOnHold" must {
          def json(value: String): JsValue = Json.parse(s"""{ "dunningLock": "$value" }""")

          def model(value: Boolean): FinancialDetailsItemLocks = financialDetailsItemLocksFalse.copy(isInterestChargeOnHold = value)

          "convert non-empty string to true" in {
            json("ANYTHING").as[FinancialDetailsItemLocks] shouldBe model(true)
          }

          "convert empty string to false" in {
            json("").as[FinancialDetailsItemLocks] shouldBe model(false)
          }

          "convert absent field to false" in {
            JsObject.empty.as[FinancialDetailsItemLocks] shouldBe model(false)
          }
        }
      }
    }
  }

}
