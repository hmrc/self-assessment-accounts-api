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
package v1.models.response.retrieveSelfAssessmentChargeHistory

import play.api.libs.json.Json
import support.UnitSpec

class ChargeHistoryDetailSpec extends UnitSpec {

  val downstreamJson = Json.parse(
    """
      |{
      |  "taxYear": "2019",
      |  "documentId": "123456789",
      |  "documentDate": "2020-01-29",
      |  "documentDescription": "Balancing Charge",
      |  "totalAmount": 54321.12,
      |  "reversalDate": "2020-02-24",
      |  "reversalReason": "amended return"
      |}
      |""".stripMargin
  )

  val mtdJson = Json.parse(
    """
      |{
      |  "taxYear": "2018-19",
      |  "transactionId": "123456789",
      |  "transactionDate": "2020-01-29",
      |  "description": "Balancing Charge",
      |  "totalAmount": 54321.12,
      |  "chargeDate": "2020-02-24",
      |  "chargeReason": "amended return"
      |}
      |""".stripMargin
  )

  val validModel: ChargeHistoryDetail = ChargeHistoryDetail(
    taxYear = Some("2018-19"),
    transactionId = Some("123456789"),
    transactionDate = Some("2020-01-29"),
    description = Some("Balancing Charge"),
    totalAmount = Some(54321.12),
    chargeDate = Some("2020-02-24"),
    chargeReason = Some("amended return")
  )

  "reads" should {
    "return a valid model" when {
      "valid JSON is supplied" in {
        downstreamJson.as[ChargeHistoryDetail] shouldBe validModel
      }
    }
  }

  "writes" when {
    "passed a valid model" should {
      "return valid JSON " in {
        Json.toJson(validModel) shouldBe mtdJson
      }
    }
  }

}
