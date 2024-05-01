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

import api.models.utils.JsonErrorValidators
import play.api.libs.json.{JsString, Json}
import support.UnitSpec
import v3.fixtures.retrieveBalanceAndTransactions.DocumentDetailsFixture._

class DocumentDetailsSpec extends UnitSpec with JsonErrorValidators {

  "reads" when {
    "given a valid downstream JSON document" must {
      "return a DocumentDetails object" in {
        val result = documentDetailsDownstreamResponseJson.as[DocumentDetails]
        result shouldBe documentDetails
      }
    }

    "given a valid downstream JSON document without the optional documentDueDate field" must {
      "return a DocumentDetails object" in {
        val result = documentDetailsWithoutDocDueDateDownstreamResponseJson.as[DocumentDetails]
        result shouldBe documentDetailsWithoutDocDueDate
      }
    }

    "given a valid downstreamJSON document with 9999 (meaning no tax year)" must {
      "omit the tax year in the DocumentDetails object" in {
        val json     = newDownstreamDocumentDetailsJson(taxYear = "9999", maybeDocumentDueDate = Some("2021-04-05"))
        val expected = documentDetails.copy(taxYear = None)
        val result   = json.as[DocumentDetails]
        result shouldBe expected
      }
    }

    "given a downstream JSON where MTD child objects in the model would be empty" must {
      "convert to a DocumentDetails object without these child objects" in {
        documentDetailsDownstreamResponseMinimalJson.as[DocumentDetails] shouldBe documentDetailsMinimal
      }
    }

    "converting informationCode to isCodedOut" must {
      def json(informationCode: String) = documentDetailsDownstreamResponseMinimalJson.update("informationCode", JsString(informationCode))

      "convert any non-empty string value to true" in {
        json("x").as[DocumentDetails] shouldBe
          documentDetailsMinimal.copy(isCodedOut = true)
      }

      "convert any empty string value to false" in {
        json("").as[DocumentDetails] shouldBe
          documentDetailsMinimal.copy(isCodedOut = false)
      }

      "convert an absent field to false" in {
        documentDetailsDownstreamResponseMinimalJson.removeProperty("informationCode").as[DocumentDetails] shouldBe
          documentDetailsMinimal.copy(isCodedOut = false)
      }
    }
  }

  "writes" should {
    "return the expected JSON document" when {
      "given a DocumentDetails object" in {
        val result = Json.toJson(documentDetails)
        result shouldBe documentDetailsMtdResponseJson
      }
    }
  }

}
