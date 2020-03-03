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

import play.api.libs.json.{JsValue, Json}
import v1.models.response.listCharges.{Charge, ListChargesResponse}

object ListChargesFixture {

  val fullDesChargeResponse: JsValue = Json.parse(
    """
      |{
      |  "taxYear" : "2020-12",
      |  "id" : "1234567890AB",
      |  "transactionDate" : "2019-01-01",
      |  "type" : "Charge Type",
      |  "totalAmount" : 100.23,
      |  "outstandingAmount" : 50.01
      |}
      |""".stripMargin
  )

  val minimalDesChargeResponse: JsValue = Json.parse(
    """
      |{
      |
      |}
      |""".stripMargin
  )

  val invalidDesChargeResponse: JsValue = Json.parse(
    """
      |{
      |  "taxYear" : "2020-12",
      |  "id" : "1234567890AB",
      |  "transactionDate" : "2019-01-01",
      |  "type" : 100.23,
      |  "totalAmount" : "Charge Type",
      |  "outstanding" : 50.01
      |}
      |""".stripMargin
  )

  val fullChargeModel: Charge = Charge(taxYear = Some("2020-12"),
    id = Some("1234567890AB"),
    transactionDate = Some("2019-01-01"),
    `type` = Some("Charge Type"),
    totalAmount = Some(100.23),
    outstandingAmount = Some(50.01)
  )

  val minimalChargeModel = Charge(None, None, None, None, None, None)

  val fullDesListChargesSingleResponseResponse: JsValue = Json.parse(
    s"""
       |{
       | "charges" : [$fullDesChargeResponse]
       |}
       |""".stripMargin
  )

  val fullDesListChargesMultipleResponseResponse: JsValue = Json.parse(
    s"""
       |{
       | "charges" : [$fullDesChargeResponse, $fullDesChargeResponse]
       |}
       |""".stripMargin
  )

  val minimalDesListChargesResponseResponse: JsValue = Json.parse(
    """
      |{
      |   "charges" : []
      |}
      |""".stripMargin
  )

  val invalidDesListChargesResponseResponse: JsValue = Json.parse(
    """
      |{
      |
      |}
      |""".stripMargin
  )

  val fullListSingleChargeModel: ListChargesResponse[Charge] = ListChargesResponse(Seq(fullChargeModel))

  val fullListMultipleChargeModel: ListChargesResponse[Charge] = ListChargesResponse(Seq(fullChargeModel, fullChargeModel))

  val minimalListChargeModel: ListChargesResponse[Charge] = ListChargesResponse(Seq.empty[Charge])
}
