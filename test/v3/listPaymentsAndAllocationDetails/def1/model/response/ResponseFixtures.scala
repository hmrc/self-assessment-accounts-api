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

package v3.listPaymentsAndAllocationDetails.def1.model.response

import play.api.libs.json.{JsValue, Json}
import v3.listPaymentsAndAllocationDetails.def1.model.response.PaymentFixtures._
import v3.listPaymentsAndAllocationDetails.model.response.ListPaymentsAndAllocationDetailsResponse

object ResponseFixtures {

  val responseObject: ListPaymentsAndAllocationDetailsResponse = Def1_ListPaymentsAndAllocationDetailsResponse(
    payments = List(paymentObject)
  )

  val mtdResponseJson: JsValue = Json.parse(
    s"""
       |{
       |    "payments": [
       |        $paymentMtdJson
       |    ]
       |}
       |""".stripMargin
  )

  val responseDownstreamJson: JsValue = Json.parse(
    s"""
       |{
       |    "idType": "NINO",
       |    "idValue": "NS345678A",
       |    "regimeType": "VATC",
       |    "businessPartner": "1122334455",
       |    "paymentDetails": [
       |        $paymentDownstreamJson
       |    ]
       |}
       |""".stripMargin
  )

}
