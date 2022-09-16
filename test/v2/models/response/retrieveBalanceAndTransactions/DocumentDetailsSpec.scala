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
import v2.fixtures.retrieveBalanceAndTransactions.DocumentDetailsFixture

class DocumentDetailsSpec extends UnitSpec with DocumentDetailsFixture{

  "reads" should {
    "return a DocumentDetails object" when {
      "given a valid JSON document" in {
        val result = documentDetailsDownstreamJson.as[DocumentDetails]
        result shouldBe documentDetails
      }

      "given a valid JSON document with 9999 meaning no tax year" in {
        val json     = newDocumentDetailsDownstreamJson(taxYear = "9999")
        val expected = documentDetails.copy(taxYear = None)
        val result   = json.as[DocumentDetails]
        result shouldBe expected
      }
    }
  }

  "writes" should {
    "return the expected JSON document" when {
      "given a DocumentDetails object" in {
        val result = Json.toJson(documentDetails)
        result shouldBe documentDetailsMtdJson
      }
    }
  }

}
