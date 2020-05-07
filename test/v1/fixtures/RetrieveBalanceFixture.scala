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

import play.api.libs.json.{JsArray, JsObject, JsValue, Json}
import v1.models.request.retrieveBalance.RetrieveBalanceRawRequest
import v1.models.response.retrieveBalance.RetrieveBalanceResponse

object RetrieveBalanceFixture {

  val validNino = "AA123456A"
  val invalidNino = "A12344A"

  val validRetrieveBalanceRawRequest: RetrieveBalanceRawRequest = RetrieveBalanceRawRequest(validNino)
  val invalidRetrieveBalanceRawRequest: RetrieveBalanceRawRequest = RetrieveBalanceRawRequest(invalidNino)

  val fullDesResponse: JsValue = Json.parse(
    """
      |{
      | "idType": "MTDBSA",
      | "idNumber": "XQIT00000000001",
      | "regimeType": "ITSA",
      | "financialDetails": [
      |    {
      |       "overDueAmount": 1000,
      |       "balanceDueWithin30Days": 2000,
      |       "nextPymntDateChrgsDueIn30Days": "2020-09-12",
      |       "balanceNotDueIn30Days": 1000,
      |       "nextPaymntDateBalnceNotDue": "2020-12-12",
      |       "earliestPymntDateOverDue": "2020-11-12"
      |    }
      |  ]
      |}
      |""".stripMargin
  )

  val fullMtdResponseJson: JsValue = Json.parse(
    """
      |{
      | "overdueAmount": 1000,
      | "payableAmount": 2000,
      | "payableDueDate": "2020-09-12",
      | "pendingChargeDueAmount": 1000,
      | "pendingChargeDueDate": "2020-12-12"
      |}
      |""".stripMargin
  )

  def fullMtdResponseJsonWithHateoas(nino: String): JsValue = fullMtdResponseJson.as[JsObject] ++ Json.obj(
    "links" -> JsArray(
      Seq(
        Json.obj(
          "href" -> s"/accounts/self-assessment/$nino/balance",
          "method" -> "GET",
          "rel" -> "self"
        )
      )
    )
  )

  val fullModel: RetrieveBalanceResponse = RetrieveBalanceResponse(
    overdueAmount = Some(BigDecimal(1000)),
    payableAmount = BigDecimal(2000),
    payableDueDate = Some("2020-09-12"),
    pendingChargeDueAmount = Some(BigDecimal(1000)),
    pendingChargeDueDate = Some("2020-12-12"))

  val minimalDesResponse: JsValue = Json.parse(
    """
      |{
      | "idType": "MTDBSA",
      | "idNumber": "XQIT00000000001",
      | "regimeType": "ITSA",
      | "financialDetails": [
      |   {
      |     "balanceDueWithin30Days": 2000
      |   }
      | ]
      |}
      |""".stripMargin
  )

  val minMtdResponseJson: JsValue = Json.parse(
    """
      |{
      |  "payableAmount": 2000
      |}
      |""".stripMargin
  )

  val minimalResponseModel: RetrieveBalanceResponse = RetrieveBalanceResponse(overdueAmount = None,
    payableAmount = BigDecimal(2000),
    payableDueDate = None,
    pendingChargeDueAmount = None,
    pendingChargeDueDate = None)

  val InvalidDesResponse: JsValue = Json.parse(
    """
      |{
      |
      |}
      |""".stripMargin
  )
}
