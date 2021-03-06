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

package v1.models.response.retrieveChargeHistory

import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec
import v1.fixtures.RetrieveChargeHistoryFixture._

class RetrieveChargeHistoryResponseSpec extends UnitSpec {

  val desResponse: JsValue = Json.parse(
    """
      |{
      |   "idType":"MTDBSA",
      |   "idValue":"XQIT00000000001",
      |   "regimeType": "ITSA",
      |   "chargeHistoryDetails": [
      |      {
      |         "taxYear":"2020",
      |         "documentId":"X123456790A",
      |         "documentDate":"2019-06-01",
      |         "documentDescription":"Balancing Charge Debit",
      |         "totalAmount":600.01,
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
      |         "transactionId": "X123456790A",
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

  val desResponseEmptyHistory: JsValue = Json.parse(
    """
      |{
      |   "idType":"MTDBSA",
      |   "idValue":"XQIT00000000001",
      |   "regimeType": "ITSA",
      |   "chargeHistoryDetails": []
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
      |   "idType":"MTDBSA",
      |   "idValue":"XQIT00000000001",
      |   "regimeType": "ITSA",
      |   "chargeHistoryDetails": [
      |     {
      |     }
      |   ]
      |}
    """.stripMargin
  )

  val desReponseEmpty: JsValue = Json.parse("""{}""")

  val retrieveChargeHistoryResponseEmpty: RetrieveChargeHistoryResponse =
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
        desResponseEmptyHistory.as[RetrieveChargeHistoryResponse] shouldBe retrieveChargeHistoryResponseEmpty
      }
    }

    "read from empty JSON" should {
      "produce a JsError" in {
        desReponseEmpty.validate[RetrieveChargeHistoryResponse] shouldBe a[JsError]
      }
    }

    "read from valid JSON with an empty history item" should {
      "not read empty charge history items" in {
        desResponseEmptyHistoryObject.as[RetrieveChargeHistoryResponse] shouldBe retrieveChargeHistoryResponseEmpty
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
        Json.toJson(retrieveChargeHistoryResponseEmpty) shouldBe mtdResponseEmptyHistory
      }
    }
  }

}
