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

package v2.fixtures.retrieveBalanceAndTransactions

import play.api.libs.json.{JsValue, Json}
import v2.models.response.retrieveBalanceAndTransactions.{Coded, CodingDetails}

object CodingDetailsFixture {

  val coded: Coded = Coded(charge = Some(123.45),
    initiationDate = Some("2022-10-13"))

  val codingDetailsObject: CodingDetails = CodingDetails(taxYearReturn = Some("2022"),
    totalLiabilityAmount = Some(123.45),
    taxYearCoding = Some("2022"),
    coded = Some(coded))

  val codingDetailsDownstreamDetailsJson: JsValue = Json.parse(
    s"""
       |{
       |   "taxYearReturn": "${codingDetailsObject.taxYearReturn.get}",
       |   "totalLiabilityAmount": ${codingDetailsObject.totalLiabilityAmount.get},
       |   "taxYearCoding": "${codingDetailsObject.taxYearCoding.get}",
       |   "coded": {
       |      "charge": 123.45,
       |      "initiationDate": "${coded.initiationDate.get}"
       |   }
       |}
       |""".stripMargin
  )

  val codingDetailsMtdJson: JsValue = Json.parse(
    s"""
       |{
       |   "taxYearReturn": "${codingDetailsObject.taxYearReturn.get}",
       |   "totalLiabilityAmount": ${codingDetailsObject.totalLiabilityAmount.get},
       |   "taxYearCoding": "${codingDetailsObject.taxYearCoding.get}",
       |   "coded": {
       |      "charge": ${coded.charge.get},
       |      "initiationDate": "${coded.initiationDate.get}"
       |   }
       |}
       |""".stripMargin
  )
}
