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

package v2.fixtures

import play.api.libs.json.{JsValue, Json}
import v2.models.response.listCharges.{Charge, ListChargesResponse}

object ListChargesFixture {

  val fullDesChargeResponse: JsValue = Json.parse(
    """
      |{
      |  "taxYear" : "2020",
      |  "documentId" : "1234567890AB",
      |  "documentDate" : "2020-02-01",
      |  "documentDescription" : "Charge Type",
      |  "totalAmount" : 11.23,
      |  "documentOutstandingAmount" : 4.56
      |}
      |""".stripMargin
  )

  val invalidDesChargeResponse: JsValue = Json.parse(
    """
      |{
      |  "taxYear" : "2020",
      |  "documentId" : "1234567890AB",
      |  "transactionDate" : "2019-01-01",
      |  "type" : 100.23,
      |  "totalAmount" : "Charge Type",
      |  "outstanding" : 50.01
      |}
      |""".stripMargin
  )

  val fullCharge: Charge = Charge(
    taxYear = "2019-20",
    transactionId = "1234567890AB",
    transactionDate = "2020-02-01",
    `type` = Some("Charge Type"),
    totalAmount = 11.23,
    outstandingAmount = 4.56
  )

  val listChargesDesJson: JsValue = Json.parse(
    s"""
       |{
       |  "documentDetails": [$fullDesChargeResponse]
       |}
      """.stripMargin
  )

  val fullDesListChargesMultipleResponse: JsValue = Json.parse(
    s"""
       |{
       | "documentDetails" : [$fullDesChargeResponse, $fullDesChargeResponse]
       |}
      """.stripMargin
  )

  val invalidDesListChargesResponse: JsValue = Json.parse(
    """
      |{
      |
      |}
    """.stripMargin
  )

  val fullChargeMtdResponse: JsValue = Json.parse(
    """
      |{
      |   "taxYear": "2019-20",
      |   "transactionId": "1234567890AB",
      |   "transactionDate": "2020-02-01",
      |   "type": "Charge Type",
      |   "totalAmount": 11.23,
      |   "outstandingAmount": 4.56
      |}
      |""".stripMargin
  )

  val fullListSingleCharge: ListChargesResponse[Charge] = ListChargesResponse(Seq(fullCharge))

  val fullListMultipleCharges: ListChargesResponse[Charge] = ListChargesResponse(Seq(fullCharge, fullCharge))

  val listChargesMtdResponse: JsValue = Json.parse(
    s"""
       |{
       | "charges" : [$fullChargeMtdResponse]
       |}
      """.stripMargin
  )

  val ListChargesMtdResponseWithHateoas: JsValue = Json.parse("""
                                                                |{
                                                                |  "charges":[
                                                                |    {
                                                                |      "taxYear":"2019-20",
                                                                |      "transactionId":"1234567890AB",
                                                                |      "transactionDate":"2020-02-01",
                                                                |      "type":"Charge Type",
                                                                |      "totalAmount":11.23,
                                                                |      "outstandingAmount":4.56,
                                                                |      "links": [
                                                                |        {
                                                                |          "href": "/accounts/self-assessment/AA123456A/transactions/1234567890AB",
                                                                |          "method": "GET",
                                                                |          "rel": "retrieve-transaction-details"
                                                                |        }
                                                                |      ]
                                                                |    },
                                                                |    {
                                                                |      "taxYear":"2019-20",
                                                                |      "transactionId":"1234567890AB",
                                                                |      "transactionDate":"2020-02-01",
                                                                |      "type":"Charge Type",
                                                                |      "totalAmount":11.23,
                                                                |      "outstandingAmount":4.56,
                                                                |      "links": [
                                                                |        {
                                                                |          "href": "/accounts/self-assessment/AA123456A/transactions/1234567890AB",
                                                                |          "method": "GET",
                                                                |          "rel": "retrieve-transaction-details"
                                                                |        }
                                                                |      ]
                                                                |    }
                                                                |  ],
                                                                |  "links": [
                                                                |    {
                                                                |      "href": "/accounts/self-assessment/AA123456A/charges?from=2018-10-01&to=2019-10-01",
                                                                |      "method": "GET",
                                                                |      "rel": "self"
                                                                |    },
                                                                |    {
                                                                |      "href": "/accounts/self-assessment/AA123456A/transactions?from=2018-10-01&to=2019-10-01",
                                                                |      "method": "GET",
                                                                |      "rel": "list-transactions"
                                                                |    }
                                                                |  ]
                                                                |}""".stripMargin)

  val mtdResponseObj: ListChargesResponse[Charge] = ListChargesResponse(charges = Seq(fullCharge, fullCharge))

}