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

package v2.fixtures.listPaymentsAndAllocationDetails

import play.api.libs.json.{JsValue, Json}
import v2.models.response.listPaymentsAndAllocationDetails.ChargeDetail

object ChargeDetailFixtures {

  val chargeDetailObject: ChargeDetail =
    ChargeDetail(
      documentId = Some("1040000872"),
      documentType = Some("1234"),
      documentTypeDescription = Some("Income Tax Estimate"),
      chargeType = Some("5678"),
      chargeTypeDescription = Some("Income Tax Estimate - ITSA")
    )

  val chargeDetailMtdJson: JsValue = Json.parse(
    s"""
       |{
       |   "documentId": "1040000872",
       |   "documentType": "1234",
       |   "documentTypeDescription": "Income Tax Estimate",
       |   "chargeType": "5678",
       |   "chargeTypeDescription": "Income Tax Estimate - ITSA"
       |}
       |""".stripMargin
  )

}
