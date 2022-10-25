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
import v2.fixtures.listPaymentsAndAllocationDetails.ChargeDetailFixtures.{chargeDetailMtdJson, chargeDetailObject}
import v2.models.response.listPaymentsAndAllocationDetails.Allocation

object AllocationFixtures {

  val allocationObject: Allocation = Allocation(
    chargeReference = "XM002610011594",
    periodKey = Some("16RL"),
    periodKeyDescription = Some("2016/17 month 12 RTI"),
    startDate = Some("2010-03-27"),
    endDate = Some("2010-06-27"),
    dueDate = Some("2010-07-27"),
    chargeDetail = Some(chargeDetailObject),
    amount = Some(12345678912.02),
    clearedAmount = Some(345678912.02),
    contractAccount = Some("ABC")
  )

  val allocationMtdJson: JsValue = Json.parse(
    s"""
       |{
       |    "chargeReference": "XM002610011594",
       |    "periodKey": "16RL",
       |    "periodKeyDescription": "2016/17 month 12 RTI",
       |    "startDate": "2010-03-27",
       |    "endDate": "2010-06-27",
       |    "dueDate": "2010-07-27",
       |    "chargeDetail": $chargeDetailMtdJson,
       |    "amount": 12345678912.02,
       |    "clearedAmount": 345678912.02,
       |    "contractAccount": "ABC"
       |}
       |""".stripMargin
  )

  val allocationDownstreamJson: JsValue = Json.parse(
    s"""
       |{
       |    "chargeReference": "XM002610011594",
       |    "sapDocNumber": "1040000872",
       |    "sapDocItem": "0026",
       |    "sapDocSubItem": "1",
       |    "periodKey": "16RL",
       |    "periodKeyDescription": "2016/17 month 12 RTI",
       |    "taxPeriodStartDate": "2010-03-27",
       |    "taxPeriodEndDate": "2010-06-27",
       |    "dueDate": "2010-07-27",
       |    "chargeType": "Income Tax Estimate - ITSA",
       |    "mainType": "Income Tax Estimate",
       |    "amount": 12345678912.02,
       |    "clearedAmount": 345678912.02,
       |    "mainTransaction": "1234",
       |    "subTransaction": "5678",
       |    "contractAccountCategory": "02",
       |    "contractAccount": "ABC",
       |    "contractObjectType": "ABCD",
       |    "contractObject": "00000003000000002757"
       |}
       |""".stripMargin
  )

}
