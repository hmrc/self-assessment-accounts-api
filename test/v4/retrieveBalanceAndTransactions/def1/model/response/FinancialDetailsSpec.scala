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

package v4.retrieveBalanceAndTransactions.def1.model.response

import play.api.libs.json.Json
import shared.utils.UnitSpec
import v4.retrieveBalanceAndTransactions.def1.model.FinancialDetailsFixture.{downstreamFinancialDetailsFullHipJson, downstreamFinancialDetailsFullJson, financialDetailsFull, mtdFinancialDetailsFullJson}

class FinancialDetailsSpec extends UnitSpec {

  implicit val readLocks: FinancialDetailsItem.ReadLocks = FinancialDetailsItem.ReadLocks(true)

  "reads" should {
    "return a valid model with all properties" when {
      "valid JSON with all properties is supplied and the feature switch is disabled (IFS enabled)" in {
        downstreamFinancialDetailsFullJson.as[FinancialDetails] shouldBe financialDetailsFull
      }
    }
    "return a valid model with all properties" when {
      "valid JSON with all properties is supplied and the feature switch is enabled (HIP enabled)" in {
        downstreamFinancialDetailsFullHipJson.as[FinancialDetails] shouldBe financialDetailsFull
      }
    }
  }

  "writes" when {
    "passed a valid model with all properties" should {
      "return valid JSON with all properties" in {
        Json.toJson(financialDetailsFull) shouldBe mtdFinancialDetailsFullJson
      }
    }
  }

}
