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

package v3.retrieveChargeHistoryByTransactionId.def1

import play.api.libs.json.{JsObject, JsValue, Json}
import v3.retrieveChargeHistoryByTransactionId.def1.models.response.ChargeHistoryDetail
import v3.retrieveChargeHistoryByTransactionId.model.response.RetrieveChargeHistoryResponse


object RetrieveChargeHistoryFixture {

  val validChargeHistoryDetailObject: ChargeHistoryDetail = ChargeHistoryDetail(
    taxYear = Some("2018-19"),
    transactionId = "123456789",
    transactionDate = "2020-01-29",
    description = "Balancing Charge",
    totalAmount = 54321.12,
    changeDate = "2020-02-24",
    changeReason = "amended return",
    poaAdjustmentReason = Some("001")
  )

  val validChargeHistoryResponseObject: RetrieveChargeHistoryResponse = RetrieveChargeHistoryResponse(
    List(validChargeHistoryDetailObject, validChargeHistoryDetailObject))

  val downstreamDetailSingleJson: JsValue = Json.parse(
    s"""
      |{
      |  "taxYear": "2019",
      |  "documentId": "${validChargeHistoryDetailObject.transactionId}",
      |  "documentDate":"${validChargeHistoryDetailObject.transactionDate}",
      |  "documentDescription": "${validChargeHistoryDetailObject.description}",
      |  "totalAmount": ${validChargeHistoryDetailObject.totalAmount},
      |  "reversalDate": "${validChargeHistoryDetailObject.changeDate}",
      |  "reversalReason": "${validChargeHistoryDetailObject.changeReason}",
      |  "poaAdjustmentReason": "001"
      |}
      |""".stripMargin
  )

  val downstreamResponse: JsValue = Json
    .parse(s"""
       |{
       |    "idType": "MTDBSA",
       |    "idValue": "XQIT00000000001",
       |    "regimeType": "ITSA",
       |    "chargeHistoryDetails": [  $downstreamDetailSingleJson ]
       |}
       |""".stripMargin)

  val downstreamResponseMultiple: JsValue = Json
    .parse(s"""
      |{
      |    "idType": "MTDBSA",
      |    "idValue": "XQIT00000000001",
      |    "regimeType": "ITSA",
      |    "chargeHistoryDetails": [ $downstreamDetailSingleJson ,  $downstreamDetailSingleJson
      |   ]
      |}
      |""".stripMargin)

  val mtdSingleJson: JsValue = Json
    .parse(
      s"""
      |{
      |  "taxYear": "${validChargeHistoryDetailObject.taxYear.get}",
      |  "transactionId": "${validChargeHistoryDetailObject.transactionId}",
      |  "transactionDate": "${validChargeHistoryDetailObject.transactionDate}",
      |  "description": "${validChargeHistoryDetailObject.description}",
      |  "totalAmount": ${validChargeHistoryDetailObject.totalAmount},
      |  "changeDate": "${validChargeHistoryDetailObject.changeDate}",
      |  "changeReason": "${validChargeHistoryDetailObject.changeReason}",
      |  "poaAdjustmentReason": "001"
      |}
      |""".stripMargin
    )

  val mtdSingleResponse: JsValue = Json
    .parse(s"""
      |{
      |   "chargeHistoryDetails": [
      |      $mtdSingleJson
      |  ]
      | }
      |""".stripMargin)

  val mtdMultipleResponse: JsValue = Json.parse(s"""
      |{
      |   "chargeHistoryDetails": [
      |      $mtdSingleJson,
      |      $mtdSingleJson
      |  ]
      | }
      |""".stripMargin)

  def mtdMultipleResponseWithHateoas(nino: String, transactionId: String): JsObject = Json
    .parse(s"""
       |{
       |   "chargeHistoryDetails": [
       |      $mtdSingleJson,
       |      $mtdSingleJson
       |  ],
       |   "links":[
       |      {
       |         "href":"/accounts/self-assessment/$nino/charges/$transactionId",
       |         "method":"GET",
       |         "rel":"self"
       |      },
       |      {
       |         "href":"/accounts/self-assessment/$nino/transactions/$transactionId",
       |         "method":"GET",
       |         "rel":"retrieve-transaction-details"
       |      }
       |   ]
       | }
       |""".stripMargin)
    .as[JsObject]

}
