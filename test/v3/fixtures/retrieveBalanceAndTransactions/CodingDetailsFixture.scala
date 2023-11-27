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

package v3.fixtures.retrieveBalanceAndTransactions

import api.models.domain.TaxYear
import play.api.libs.json.{JsValue, Json}
import v3.models.response.retrieveBalanceAndTransactions.{Coded, CodingDetails}

object CodingDetailsFixture {

  private val taxYear: TaxYear = TaxYear("2022")

  val coded: Coded = Coded(charge = Some(123.45), initiationDate = Some("2022-10-13"))
  val codingDetails: CodingDetails =
    CodingDetails(returnTaxYear = Some(taxYear.asMtd), totalLiabilityAmount = Some(123.45), codingTaxYear = Some(taxYear.asMtd), coded = Some(coded))
  val codingDetailsDownstreamResponseJson: JsValue = Json.parse(
    s"""
       |{
       |   "taxYearReturn": "2022",
       |   "totalLiabilityAmount": 123.45,
       |   "taxYearCoding": "2022",
       |   "coded": {
       |      "amount": 123.45,
       |      "initiationDate": "2022-10-13"
       |   }
       |}
       |""".stripMargin
  )
  val codingDetailsMtdResponseJson: JsValue = Json.parse(
    s"""
       |{
       |   "returnTaxYear": "2021-22",
       |   "totalLiabilityAmount": 123.45,
       |   "codingTaxYear": "2021-22",
       |   "coded": {
       |      "charge": 123.45,
       |      "initiationDate": "2022-10-13"
       |   }
       |}
       |""".stripMargin
  )

}
