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

package v4.listPaymentsAndAllocationDetails.def1.model.response

import play.api.libs.json.{JsValue, Json}
import v4.listPaymentsAndAllocationDetails.def1.model.response.AllocationFixtures.{allocationDownstreamJson, allocationMtdJson, allocationObject}

object PaymentFixtures {

  val paymentObject: Payment = Payment(
    paymentLot = Some("081203010024"),
    paymentLotItem = Some("000001"),
    paymentReference = Some("1594"),
    paymentAmount = Some(12345678912.02),
    paymentMethod = Some("A"),
    transactionDate = Some("2010-02-27"),
    allocations = List(allocationObject)
  )

  val paymentObjectEmpty: Payment = Payment(
    paymentLot = None,
    paymentLotItem = None,
    paymentReference = None,
    paymentAmount = None,
    paymentMethod = None,
    transactionDate = None,
    allocations = List()
  )

  val paymentMtdJson: JsValue = Json.parse(
    s"""
       |{  
       |    "paymentLot": "081203010024",
       |    "paymentLotItem": "000001",
       |    "paymentReference": "1594",
       |    "paymentAmount": 12345678912.02,
       |    "paymentMethod": "A",
       |    "transactionDate": "2010-02-27",
       |    "allocations": [
       |        $allocationMtdJson
       |    ]
       |}
       |""".stripMargin
  )

  val paymentDownstreamJson: JsValue = Json.parse(
    s"""
       |{  
       |    "paymentLot": "081203010024",
       |    "paymentLotItem": "000001",
       |    "paymentReference": "1594",
       |    "paymentAmount": 12345678912.02,
       |    "paymentMethod": "A",
       |    "valueDate": "2010-02-27",
       |    "sapClearingDocsDetails": [
       |        $allocationDownstreamJson
       |    ]
       |}
       |""".stripMargin
  )

}
