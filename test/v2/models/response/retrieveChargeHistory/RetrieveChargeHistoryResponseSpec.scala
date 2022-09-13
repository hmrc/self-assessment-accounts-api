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

package v2.models.response.retrieveChargeHistory

import play.api.libs.json.Json
import support.UnitSpec
import v2.fixtures.retrieveChargeHistory.RetrieveChargeHistoryFixture._

class RetrieveChargeHistoryResponseSpec extends UnitSpec {

  val validObjectSingle: RetrieveChargeHistoryResponse = RetrieveChargeHistoryResponse(
    Seq(validChargeHistoryDetailObject))

  val validObjectMultiple: RetrieveChargeHistoryResponse = validChargeHistoryResponseObject

  "RetrieveSelfAssessmentChargeHistoryResponse" when {
    "reading valid JSON" should {
      "return the expected object" in {
        downstreamResponse.as[RetrieveChargeHistoryResponse] shouldBe validObjectSingle
      }
    }
  }

  "RetrieveSelfAssessmentChargeHistoryResponse" when {
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
