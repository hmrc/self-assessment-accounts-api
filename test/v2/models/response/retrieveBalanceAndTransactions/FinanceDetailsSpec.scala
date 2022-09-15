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
import v2.fixtures.retrieveBalanceAndTransactions.FinanceDetailsFixture.{
  downstreamFinanceDetailFullJson,
  downstreamFinanceDetailMismatchedMainTypeJson,
  downstreamFinanceDetailMissingMainTypeJson,
  financeDetailsFullObject,
  financeDetailsNoMainTypeObject,
  mtdFinanceDetailFullJson,
  mtdFinanceDetailNoMainTypeJson
}

class FinanceDetailsSpec extends UnitSpec {

  "reads" should {
    "return a valid model with all properties" when {
      "valid JSON with all properties is supplied " in {
        downstreamFinanceDetailFullJson.as[FinanceDetails] shouldBe financeDetailsFullObject
      }
    }
    "return a valid model without a mainType" when {
      "JSON without mainType is supplied " in {
        downstreamFinanceDetailMissingMainTypeJson.as[FinanceDetails] shouldBe financeDetailsNoMainTypeObject
      }
      "JSON with invalid mainType is supplied " in {
        downstreamFinanceDetailMismatchedMainTypeJson.as[FinanceDetails] shouldBe financeDetailsNoMainTypeObject
      }
    }
  }

  "writes" when {
    "passed a valid model with all properties" should {
      "return valid JSON with all properties" in {
        Json.toJson(financeDetailsFullObject) shouldBe mtdFinanceDetailFullJson
      }
    }

    "passed a valid model with no mainType" should {
      "return valid JSON with no mainType" in {
        Json.toJson(financeDetailsNoMainTypeObject) shouldBe mtdFinanceDetailNoMainTypeJson
      }
    }

  }

}
