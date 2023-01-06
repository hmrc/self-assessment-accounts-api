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

package v2.models.response.listPaymentsAndAllocationDetails

import play.api.libs.json.Json
import support.UnitSpec
import v2.fixtures.listPaymentsAndAllocationDetails.AllocationFixtures.allocationDownstreamJson
import v2.fixtures.listPaymentsAndAllocationDetails.ChargeDetailFixtures.{chargeDetailMtdJson, chargeDetailObject}

class ChargeDetailSpec extends UnitSpec {

  "reads" should {
    "return a valid model with properties" when {
      "valid JSON with all properties is supplied" in {
        allocationDownstreamJson.as[ChargeDetail] shouldBe chargeDetailObject
      }
    }
  }

  "writes" should {
    "passed a valid model with all properties" should {
      "return valid JSON with all properties" in {
        Json.toJson(chargeDetailObject) shouldBe chargeDetailMtdJson
      }
    }
  }

}
