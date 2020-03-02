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

package v1.models.response.retrieveBalance

import play.api.libs.json.{JsError, Json}
import support.UnitSpec
import v1.fixtures.RetrieveBalanceFixture._

class RetrieveBalanceResponseSpec extends UnitSpec {

  "BalanceDetail" should {

    "return a successful Json model" when {
      "the json contains all fields" in {
        fullDesResponse.as[RetrieveBalanceResponse] shouldBe fullModel
      }

      "the json contains only mandatory fields" in {
        minimalDesResponse.as[RetrieveBalanceResponse] shouldBe minimalResponseModel
      }
    }

    "write successfully" when {
      "the model contains all fields" in {
        Json.toJson(fullModel) shouldBe fullMtdResponseJson
      }

      "the model contains only mandatory fields" in {
        Json.toJson(minimalResponseModel) shouldBe minMtdResponseJson
      }
    }

    "throw an error" when {
      "the json does not contain the mandatory field" in {
        InvalidDesResponse.validate[RetrieveBalanceResponse] shouldBe a[JsError]
      }
    }
  }
}
