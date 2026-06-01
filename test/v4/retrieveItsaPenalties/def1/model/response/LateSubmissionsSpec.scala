/*
 * Copyright 2026 HM Revenue & Customs
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

package v4.retrieveItsaPenalties.def1.model.response

import play.api.libs.json.*
import shared.utils.UnitSpec

class LateSubmissionsSpec extends UnitSpec {

  "LateSubmissions" should {

    val model: LateSubmissions =
      LateSubmissions(
        lateSubmissionId = "1054",
        incomeSource = Some("Income Tax Liability"),
        taxPeriod = Some("24C3"),
        taxReturnStatus = Some("fulfilled"),
        taxPeriodStartDate = Some("2024-07-01"),
        taxPeriodEndDate = Some("2024-09-30"),
        taxPeriodDueDate = Some("2024-11-07"),
        returnReceiptDate = Some("2024-11-13")
      )

    val downstreamJson: JsValue = Json.parse(
      """
          |{
          |  "lateSubmissionID": "1054",
          |  "incomeSource": "Income Tax Liability",
          |  "taxPeriod": "24C3",
          |  "taxReturnStatus": "Fulfilled",
          |  "taxPeriodStartDate": "2024-07-01",
          |  "taxPeriodEndDate": "2024-09-30",
          |  "taxPeriodDueDate": "2024-11-07",
          |  "returnReceiptDate": "2024-11-13"
          |}
          |""".stripMargin
    )

    val mtdJson: JsValue = Json.parse(
      """
          |{
          |  "lateSubmissionId": "1054",
          |  "incomeSource": "Income Tax Liability",
          |  "taxPeriod": "24C3",
          |  "taxReturnStatus": "fulfilled",
          |  "taxPeriodStartDate": "2024-07-01",
          |  "taxPeriodEndDate": "2024-09-30",
          |  "taxPeriodDueDate": "2024-11-07",
          |  "returnReceiptDate": "2024-11-13"
          |}
          |""".stripMargin
    )

    "read from json" in {
      downstreamJson.as[LateSubmissions] shouldBe model
    }

    "write to json" in {
      Json.toJson(model) shouldBe mtdJson
    }
  }

}
