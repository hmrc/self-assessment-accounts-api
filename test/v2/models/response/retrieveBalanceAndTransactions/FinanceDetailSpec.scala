/*
 * Copyright 2022 HM Revenue & Customs
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
package v2.models.response.retrieveBalanceAndTransactions

import play.api.libs.json.Json
import support.UnitSpec
import v2.fixtures.retrieveBalanceAndTransactions.FinanceDetailFixture.{
  downstreamFinanceDetailFullJson,
  downstreamFinanceDetailMismatchedMainTypeJson,
  downstreamFinanceDetailMissingMainTypeJson,
  financeDetailFullObject,
  financeDetailNoMainTypeObject,
  mtdFinanceDetailFullJson,
  mtdFinanceDetailNoMainTypeJson
}

class FinanceDetailSpec extends UnitSpec {

  "reads" should {
    "return a valid model" when {
      "valid JSON is supplied " in {
        downstreamFinanceDetailFullJson.as[FinanceDetail] shouldBe financeDetailFullObject
      }
    }
    "return a model without mainType" when {
      "JSON without mainType is supplied " in {
        downstreamFinanceDetailMissingMainTypeJson.as[FinanceDetail] shouldBe financeDetailNoMainTypeObject
      }
    }
    "return a valid model without mainType" when {
      "JSON with invalid mainType is supplied " in {
        downstreamFinanceDetailMismatchedMainTypeJson.as[FinanceDetail] shouldBe financeDetailNoMainTypeObject
      }
    }
  }

  "writes" when {
    "passed a valid model" should {
      "return valid JSON" in {
        Json.toJson(financeDetailFullObject) shouldBe mtdFinanceDetailFullJson
      }
    }

    "passed a valid model with no mainType" should {
      "return valid JSON with without a mainType" in {
        Json.toJson(financeDetailNoMainTypeObject) shouldBe mtdFinanceDetailNoMainTypeJson
      }
    }

  }

}
