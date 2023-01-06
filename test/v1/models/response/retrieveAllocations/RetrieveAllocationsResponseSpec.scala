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

package v1.models.response.retrieveAllocations

import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec
import v1.fixtures.retrieveAllocations.RetrieveAllocationsResponseFixture
import v1.models.response.retrieveAllocations.detail.AllocationDetail

class RetrieveAllocationsResponseSpec extends UnitSpec {

  val desJson: JsValue = RetrieveAllocationsResponseFixture.desJson

  val invalidDesJson: JsValue = Json.parse(
    """
      |{
      | "paymentDetails": [
      |   {
      |   "paymentAmount": "amount"
      |   }
      |  ]
      |}
    """.stripMargin
  )

  val desJsonWithoutAllocations: JsValue = Json.parse(
    """
      |{
      | "paymentDetails": [
      |   {
      |   "paymentAmount": 1000.00,
      |   "paymentMethod": "buttons",
      |   "valueDate": "a date"
      |   }
      |  ]
      |}
    """.stripMargin
  )

  val desJsonWithEmptyAllocations: JsValue = Json.parse(
    """
      |{
      | "paymentDetails": [
      |   {
      |   "paymentAmount": 1000.00,
      |   "paymentMethod": "buttons",
      |   "valueDate": "a date",
      |   "sapClearingDocsDetails": [
      |     {
      |
      |     }
      |    ]
      |   }
      |  ]
      |}
    """.stripMargin
  )

  val paymentDetails: RetrieveAllocationsResponse[AllocationDetail] = RetrieveAllocationsResponseFixture.paymentDetails

  val paymentDetailsWithoutAllocations: RetrieveAllocationsResponse[AllocationDetail] =
    RetrieveAllocationsResponse(
      Some(1000.00),
      Some("buttons"),
      Some("a date"),
      Seq.empty[AllocationDetail]
    )

  val mtdJson: JsValue = RetrieveAllocationsResponseFixture.mtdJson

  val mtdJsonWithoutAllocations: JsValue = Json.parse(
    """
      |{
      |   "amount": 1000.00,
      |   "method": "buttons",
      |   "transactionDate": "a date",
      |   "allocations" : []
      |}
    """.stripMargin
  )

  "RetrieveAllocationsResponse" when {
    "read from valid JSON" should {
      "return the expected RetrieveAllocationResponse object" in {
        desJson.as[RetrieveAllocationsResponse[AllocationDetail]] shouldBe paymentDetails
      }
    }

    "read from invalid JSON" should {
      "return a JsError" in {
        invalidDesJson.validate[RetrieveAllocationsResponse[AllocationDetail]] shouldBe a[JsError]
      }
    }

    "read from valid JSON without allocations" should {
      "return the expected object" in {
        desJsonWithoutAllocations.as[RetrieveAllocationsResponse[AllocationDetail]] shouldBe paymentDetailsWithoutAllocations
      }
    }

    "read from valid JSON with empty allocations" should {
      "return the expected object" in {
        desJsonWithEmptyAllocations.as[RetrieveAllocationsResponse[AllocationDetail]] shouldBe paymentDetailsWithoutAllocations
      }
    }

    "written to JSON" should {
      "return the expected JSValue" in {
        Json.toJson(paymentDetails) shouldBe mtdJson
      }
    }

    "written from valid JSON without allocations" should {
      "return the JSValue without allocations" in {
        Json.toJson(paymentDetailsWithoutAllocations) shouldBe mtdJsonWithoutAllocations
      }
    }
  }

}
