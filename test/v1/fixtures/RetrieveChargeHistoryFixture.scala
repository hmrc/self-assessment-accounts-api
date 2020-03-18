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

package v1.fixtures

import play.api.libs.json.{JsObject, JsValue, Json}
import v1.models.request.retrieveChargeHistory.RetrieveChargeHistoryRawRequest
import v1.models.response.retrieveChargeHistory.{ChargeHistory, RetrieveChargeHistoryResponse}

object RetrieveChargeHistoryFixture {

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
      |         "transactionId":"X123456790A",
      |         "transactionDate":"2019-06-01",
      |         "type":"Balancing Charge Debit",
      |         "amount":600.01,
      |         "reversalDate":"2019-06-05",
      |         "reversalReason":"Example reason"
      |      },
      |      {
      |         "taxYear":"2019-20",
      |         "transactionId":"X123456790A",
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

  def mtdResponseMultipleWithHateoas(nino: String, chargeId: String): JsObject = mtdResponseWithMultipleHHistory.as[JsObject] ++ Json.parse(
    s"""
      |{
      |   "links":[
      |      {
      |         "href":"/accounts/self-assessment/$nino/charges/$chargeId",
      |         "method":"GET",
      |         "rel":"self"
      |      },
      |      {
      |         "href":"/accounts/self-assessment/$nino/transactions",
      |         "method":"GET",
      |         "rel":"retrieve-transactions"
      |      }
      |   ]
      |}
    """.stripMargin
  ).as[JsObject]

  val validNino = "AA123456A"
  val validChargeId = "ABC123"
  val invalidNino = "A12344A"
  val invalidChargeId = "123456789012345678901234567890123456" // too long

  val validRetrieveChargeHistoryRawRequest: RetrieveChargeHistoryRawRequest =
    RetrieveChargeHistoryRawRequest(validNino, validChargeId)
  val invalidRetrieveChargeHistoryRawRequestInvalidNino: RetrieveChargeHistoryRawRequest =
    RetrieveChargeHistoryRawRequest(invalidNino, validChargeId)
  val invalidRetrieveChargeHistoryRawRequestInvalidChargeId: RetrieveChargeHistoryRawRequest =
    RetrieveChargeHistoryRawRequest(validNino, invalidChargeId)
  val invalidRetrieveChargeHistoryRawRequestInvalidNinoAndChargeId: RetrieveChargeHistoryRawRequest =
    RetrieveChargeHistoryRawRequest(invalidNino, invalidChargeId)


  val chargeHistoryResponse: ChargeHistory =
    ChargeHistory(
      taxYear = Some("2019-20"),
      transactionId = Some("X123456790A"),
      transactionDate = Some("2019-06-01"),
      `type` = Some("Balancing Charge Debit"),
      amount = Some(600.01),
      reversalDate = Some("2019-06-05"),
      reversalReason = Some("Example reason")
    )

  val retrieveChargeHistoryResponse: RetrieveChargeHistoryResponse =
    RetrieveChargeHistoryResponse(
      history = Seq(chargeHistoryResponse)
    )

  val chargeHistoryResponse2: ChargeHistory =
    ChargeHistory(
      taxYear = Some("2019-20"),
      transactionId = Some("X123456790A"),
      transactionDate = Some("2019-06-01"),
      `type` = Some("Balancing Charge Debit"),
      amount = Some(600.01),
      reversalDate = Some("2019-06-07"),
      reversalReason = Some("Example reason 2")
    )

  val retrieveChargeHistoryResponseMultiple: RetrieveChargeHistoryResponse =
    RetrieveChargeHistoryResponse(
      history = Seq(
        chargeHistoryResponse,
        chargeHistoryResponse2
      )
    )
}
