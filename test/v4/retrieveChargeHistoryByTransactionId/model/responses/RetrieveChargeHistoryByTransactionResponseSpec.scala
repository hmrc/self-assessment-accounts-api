/*
 * Copyright 2025 HM Revenue & Customs
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

package v4.retrieveChargeHistoryByTransactionId.model.responses

import play.api.Configuration
import play.api.libs.json.{JsObject, Json}
import shared.config.MockSharedAppConfig
import shared.utils.UnitSpec
import v4.retrieveChargeHistoryByTransactionId.def1.RetrieveChargeHistoryFixture.*
import v4.retrieveChargeHistoryByTransactionId.model.response.RetrieveChargeHistoryResponse

class RetrieveChargeHistoryByTransactionResponseSpec extends UnitSpec with MockSharedAppConfig {

  val validObjectSingle: RetrieveChargeHistoryResponse   = RetrieveChargeHistoryResponse(List(validChargeHistoryDetailObject))
  val validObjectMultiple: RetrieveChargeHistoryResponse = validChargeHistoryResponseObject

  "RetrieveChargeHistoryResponse" when {
    "read from valid JSON" should {
      "return the expected object" in {
        downstreamResponse.as[RetrieveChargeHistoryResponse] shouldBe validObjectSingle
      }
    }

    "read from valid JSON with multiple charge history details" should {
      "return the expected object" in {
        downstreamResponseMultiple.as[RetrieveChargeHistoryResponse] shouldBe validObjectMultiple
      }
    }

    "written to JSON" should {
      "produce the expected JSON when feature switch is disabled" in {
        MockedSharedAppConfig.featureSwitchConfig returns Configuration("ifs_hip_migration_1554.enabled" -> false)
        Json.toJson(validObjectSingle) shouldBe Json.obj(
          "chargeHistoryDetails" -> Json.arr(
            mtdSingleJson.as[JsObject] - "changeTimestamp"
          )
        )
      }

      "produce the expected JSON when feature switch is enabled" in {
        MockedSharedAppConfig.featureSwitchConfig returns Configuration("ifs_hip_migration_1554.enabled" -> true)
        Json.toJson(validObjectSingle) shouldBe mtdSingleResponse
      }
    }
  }

}
