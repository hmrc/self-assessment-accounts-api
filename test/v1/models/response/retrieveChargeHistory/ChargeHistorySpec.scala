/*
 * Copyright 2019 HM Revenue & Customs
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

package v1.models.response.retrieveChargeHistory

import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec

class ChargeHistorySpec extends UnitSpec {

  val desResponse: JsValue = Json.parse(
    """
      |{
      |     "taxYear":"2020",
      |     "id":"X123456790A",
      |     "transactionDate":"2019-06-01",
      |     "type":"Balancing Charge Debit",
      |     "amount":600.01,
      |     "reversalDate":"2019-06-05",
      |     "reversalReason":"Example reason"
      |}
    """.stripMargin
  )

  val desResponseInvalid: JsValue = Json.parse(
    """
      |{
      |     "taxYear":2020,
      |     "id":"X123456790A",
      |     "transactionDate":"2019-06-01",
      |     "type":"Balancing Charge Debit",
      |     "amount":600.01,
      |     "reversalDate":"2019-06-05",
      |     "reversalReason":"Example reason"
      |}
    """.stripMargin
  )

  val mtdResponse: JsValue = Json.parse(
    """
      |{
      |     "taxYear":"2020",
      |     "id":"X123456790A",
      |     "transactionDate":"2019-06-01",
      |     "type":"Balancing Charge Debit",
      |     "amount":600.01,
      |     "reversalDate":"2019-06-05",
      |     "reversalReason":"Example reason"
      |}
    """.stripMargin
  )

  val desReponseEmpty: JsValue = Json.parse(
    """
      |{
      |}
    """.stripMargin
  )

  val chargeHistoryResponse: ChargeHistory =
    ChargeHistory(
      taxYear = Some("2020"),
      id = Some("X123456790A"),
      transactionDate = Some("2019-06-01"),
      `type` = Some("Balancing Charge Debit"),
      amount = Some(600.01),
      reversalDate = Some("2019-06-05"),
      reversalReason = Some("Example reason")
    )

  "ChargeHistory" when {
    "read from valid JSON" should {
      "produce the expected ChargeHistory object" in {
        desResponse.as[ChargeHistory] shouldBe chargeHistoryResponse
      }
    }

    "read from empty JSON" should {
      "produce an empty ChargeHistory object" in {
        desReponseEmpty.as[ChargeHistory] shouldBe ChargeHistory.empty
      }
    }

    "read from invalid JSON" should {
      "produce an empty ChargeHistory object" in {
        desResponseInvalid.validate[ChargeHistory] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON object" in {
        Json.toJson(chargeHistoryResponse) shouldBe mtdResponse
      }
    }
  }

}
