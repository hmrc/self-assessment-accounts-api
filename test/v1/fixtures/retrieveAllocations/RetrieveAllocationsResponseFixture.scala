/*
 * Copyright 2019 HM Revenue & Customs
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

package v1.fixtures.retrieveAllocations

import play.api.libs.json.{JsValue, Json}
import v1.models.response.retrieveAllocations.RetrieveAllocationsResponse
import v1.models.response.retrieveAllocations.detail.AllocationDetail

object RetrieveAllocationsResponseFixture {

  val desJson: JsValue = Json.parse(
    """
      |{
      | "paymentDetails": [
      |   {
      |   "paymentAmount": 1000.00,
      |   "paymentMethod": "buttons",
      |   "valueDate": "a date",
      |   "sapClearingDocsDetails": [
      |   {
      |     "sapDocNumber": "someID",
      |     "taxPeriodStartDate": "another date",
      |     "taxPeriodEndDate": "an even later date",
      |     "chargeType": "some type thing",
      |     "amount": 600.00,
      |     "clearedAmount": 100.00
      |     }
      |    ]
      |   }
      |  ]
      |}
    """.stripMargin
  )

  val mtdJson: JsValue = Json.parse(
    """
      |{
      |   "amount": 1000.00,
      |   "method": "buttons",
      |   "transactionDate": "a date",
      |   "allocations": [
      |   {
      |     "id": "someID",
      |     "from": "another date",
      |     "to": "an even later date",
      |     "type": "some type thing",
      |     "amount": 600.00,
      |     "clearedAmount": 100.00
      |   }
      | ]
      |}
    """.stripMargin
  )

  val paymentDetails: RetrieveAllocationsResponse =
    RetrieveAllocationsResponse(
      Some(1000.00),
      Some("buttons"),
      Some("a date"),
      Some(Seq(
        AllocationDetail(
          Some("someID"),
          Some("another date"),
          Some("an even later date"),
          Some("some type thing"),
          Some(600.00),
          Some(100.00)
        )
      ))
    )

}
