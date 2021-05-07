/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.models.request.createOrAmendCodingOut

import play.api.libs.json.Json
import support.UnitSpec

class CreateOrAmendCodingOutRequestBodySpec extends UnitSpec {

  private val mtdJson = Json.parse(
    """
      |{
      |   "payeUnderpayments": 2000.99,
      |   "selfAssessmentUnderPayments": 2000.99,
      |   "debts": 2000.99,
      |   "inYearAdjustments": 5000.99
      |}
      |""".stripMargin)

  private val desJson = Json.parse(
    """
      |{
      |   "taxCodeComponents":
      |     {
      |        "payeUnderpayments": 2000.99,
      |        "selfAssessmentUnderPayments": 2000.99,
      |        "debts": 2000.99,
      |        "inYearAdjustments": 5000.99
      |     }
      |}
      |""".stripMargin)

  private val requestBody = CreateOrAmendCodingOutRequestBody(
    Some(2000.99),
    Some(2000.99),
    Some(2000.99),
    Some(5000.99)
  )


  "CreateOrAmendCodingOutRequestBody" when {
    "read from a valid Json" should {
      "produce the expected body" in {
        mtdJson.as[CreateOrAmendCodingOutRequestBody] shouldBe requestBody
      }
    }

    "written to Json" should {
      "write to a valid json" in {
        Json.toJson(requestBody) shouldBe desJson
      }
    }
  }

}
