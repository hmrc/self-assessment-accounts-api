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

import api.models.domain.TaxYear
import play.api.libs.json.{JsValue, Json}
import v2.models.response.retrieveBalanceAndTransactions.{Coded, CodingDetails}

object CodingDetailsFixture {

  private val taxYear: TaxYear = TaxYear("2022")

  val coded: Coded = Coded(charge = Some(123.45), initiationDate = Some("2022-10-13"))

  val codingDetails: CodingDetails =
    CodingDetails(returnTaxYear = Some(taxYear.asMtd), totalLiabilityAmount = Some(123.45), codingTaxYear = Some(taxYear.asMtd), coded = Some(coded))

  val codingDetailsDownstreamResponseJson: JsValue = Json.parse(
    s"""
       |{
       |   "taxYearReturn": "${taxYear.asDownstream}",
       |   "totalLiabilityAmount": ${codingDetails.totalLiabilityAmount.get},
       |   "taxYearCoding": "${taxYear.asDownstream}",
       |   "coded": {
       |      "amount": 123.45,
       |      "initiationDate": "${coded.initiationDate.get}"
       |   }
       |}
       |""".stripMargin
  )

  val codingDetailsMtdResponseJson: JsValue = Json.parse(
    s"""
       |{
       |   "returnTaxYear": "${codingDetails.returnTaxYear.get}",
       |   "totalLiabilityAmount": ${codingDetails.totalLiabilityAmount.get},
       |   "codingTaxYear": "${codingDetails.codingTaxYear.get}",
       |   "coded": {
       |      "charge": ${coded.charge.get},
       |      "initiationDate": "${coded.initiationDate.get}"
       |   }
       |}
       |""".stripMargin
  )

}
