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

package v1.models.response.retrieveChargeHistory

import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec

class RetrieveChargeHistoryResponseSpec extends UnitSpec {

  val desResponse: JsValue = Json.parse(
    """
      |{
      |   "history": [
      |      {
      |         "taxYear":2020,
      |         "id":"X123456790A",
      |         "transactionDate":"2019-06-01",
      |         "type":"Balancing Charge Debit",
      |         "amount":600.01,
      |         "reversalDate":"2019-06-05",
      |         "reversalReason":"Example reason"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val mtdResponse: JsValue = Json.parse(
    """
      |{
      |   "history": [
      |      {
      |         "taxYear": "2019-20",
      |         "id": "X123456790A",
      |         "transactionDate": "2019-06-01",
      |         "type": "Balancing Charge Debit",
      |         "amount": 600.01,
      |         "reversalDate": "2019-06-05",
      |         "reversalReason": "Example reason"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val desResponseWithMultipleHHistory: JsValue = Json.parse(
    """
      |{
      |   "history": [
      |      {
      |         "taxYear": 2020,
      |         "id": "X123456790A",
      |         "transactionDate": "2019-06-01",
      |         "type": "Balancing Charge Debit",
      |         "amount": 600.01,
      |         "reversalDate": "2019-06-05",
      |         "reversalReason": "Example reason"
      |      },
      |      {
      |         "taxYear": 2020,
      |         "id": "X123456790A",
      |         "transactionDate": "2019-06-01",
      |         "type": "Balancing Charge Debit",
      |         "amount": 600.01,
      |         "reversalDate": "2019-06-07",
      |         "reversalReason": "Example reason 2"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val mtdResponseWithMultipleHHistory: JsValue = Json.parse(
    """
      |{
      |   "history": [
      |      {
      |         "taxYear":"2019-20",
      |         "id":"X123456790A",
      |         "transactionDate":"2019-06-01",
      |         "type":"Balancing Charge Debit",
      |         "amount":600.01,
      |         "reversalDate":"2019-06-05",
      |         "reversalReason":"Example reason"
      |      },
      |      {
      |         "taxYear":"2019-20",
      |         "id":"X123456790A",
      |         "transactionDate":"2019-06-01",
      |         "type":"Balancing Charge Debit",
      |         "amount":600.01,
      |         "reversalDate":"2019-06-07",
      |         "reversalReason":"Example reason 2"
      |      }
      |   ]
      |}
    """.stripMargin
  )

  val desResponseEmptyHistory: JsValue = Json.parse(
    """
      |{
      |   "history": []
      |}
    """.stripMargin
  )

  val mtdResponseEmptyHistory: JsValue = Json.parse(
    """
      |{
      |   "history": []
      |}
    """.stripMargin
  )

  val desResponseEmptyHistoryObject: JsValue = Json.parse(
    """
      |{
      |   "history": [
      |     {
      |     }
      |   ]
      |}
    """.stripMargin
  )

  val desReponseEmpty: JsValue = Json.parse("""{}""")

  val chargeHistoryResponse: ChargeHistory =
    ChargeHistory(
      taxYear = Some("2019-20"),
      id = Some("X123456790A"),
      transactionDate = Some("2019-06-01"),
      `type` = Some("Balancing Charge Debit"),
      amount = Some(600.01),
      reversalDate = Some("2019-06-05"),
      reversalReason = Some("Example reason")
    )

  val chargeHistoryResponse2: ChargeHistory =
    ChargeHistory(
      taxYear = Some("2019-20"),
      id = Some("X123456790A"),
      transactionDate = Some("2019-06-01"),
      `type` = Some("Balancing Charge Debit"),
      amount = Some(600.01),
      reversalDate = Some("2019-06-07"),
      reversalReason = Some("Example reason 2")
    )

  val retrieveChargeHistoryResponse: RetrieveChargeHistoryResponse =
    RetrieveChargeHistoryResponse(
      history = Seq(chargeHistoryResponse)
    )

  val retrieveChargeHistoryResponseMultiple: RetrieveChargeHistoryResponse =
    RetrieveChargeHistoryResponse(
      history = Seq(
        chargeHistoryResponse,
        chargeHistoryResponse2
      )
    )

  val retrieveChargeHistoryResponseEmptyItem: RetrieveChargeHistoryResponse =
    RetrieveChargeHistoryResponse(
      history = Seq()
    )

  "RetrieveChargeHistoryResponse" when {
    "read from valid JSON" should {
      "produce the expected RetrieveChargeHistoryResponse object" in {
        desResponse.as[RetrieveChargeHistoryResponse] shouldBe retrieveChargeHistoryResponse
      }
    }

    "read from valid JSON with multiple charge history items" should {
      "produce the expected RetrieveChargeHistoryResponse object" in {
        desResponseWithMultipleHHistory.as[RetrieveChargeHistoryResponse] shouldBe retrieveChargeHistoryResponseMultiple
      }
    }

    "read from valid JSON with an empty History array" should {
      "produce the expected RetrieveChargeHistoryResponse object" in {
        desResponseEmptyHistory.as[RetrieveChargeHistoryResponse] shouldBe RetrieveChargeHistoryResponse(Seq())
      }
    }

    "read from empty JSON" should {
      "produce a JsError" in {
        desReponseEmpty.validate[RetrieveChargeHistoryResponse] shouldBe a[JsError]
      }
    }

    "read from valid JSON with an empty history item" should {
      "produce the expected RetrieveChargeHistoryResponse object" in {
        desResponseEmptyHistoryObject.as[RetrieveChargeHistoryResponse] shouldBe retrieveChargeHistoryResponseEmptyItem
      }
    }

    "written to JSON" should {
      "produce the expected JSON" in {
        Json.toJson(retrieveChargeHistoryResponse) shouldBe mtdResponse
      }
    }

    "written to JSON (multiple history items)" should {
      "produce the expected JSON" in {
        Json.toJson(retrieveChargeHistoryResponseMultiple) shouldBe mtdResponseWithMultipleHHistory
      }
    }

    "written to JSON (empty history array)" should {
      "produce the expected JSON" in {
        Json.toJson(RetrieveChargeHistoryResponse(Seq())) shouldBe mtdResponseEmptyHistory
      }
    }

    "written to JSON (empty history item)" should {
      "not write empty history items" in {
        Json.toJson(retrieveChargeHistoryResponseEmptyItem) shouldBe mtdResponseEmptyHistory
      }
    }
  }

}
