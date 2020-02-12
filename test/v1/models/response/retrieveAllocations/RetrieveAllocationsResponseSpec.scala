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

package v1.models.response.retrieveAllocations

import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec
import v1.models.response.retrieveAllocations.detail.{AllocationDetail, PaymentDetails, RetrieveAllocationsResponse}

class RetrieveAllocationsResponseSpec extends UnitSpec {
  val desJson: JsValue = Json.parse(
    """
      |{
      | "paymentDetails": [
      |   {
      |    "paymentAmount": 1000.00,
      |    "paymentMethod": "buttons",
      |    "valueDate": "a date",
      |    "sapClearingDocsDetails": [
      |    {
      |      "sapDocNumber": "someID",
      |      "taxPeriodStartDate": "another date",
      |      "taxPeriodEndDate": "an even later date",
      |      "chargeType": "some type thing",
      |      "amount": 600.00,
      |      "clearedAmount": 100.00
      |      }
      |    ]
      |   }
      | ]
      |}
      |""".stripMargin)

  val invalidDesJson: JsValue = Json.parse(
    """
      |{
      | "paymentDetools": [
      |   {
      |    "paymentAmount": 1000.00,
      |    "paymentMethod": "buttons",
      |    "valueDate": "a date",
      |    "sapClearingDocsDetails": [
      |    {
      |      "sapDocNumber": "someID",
      |      "taxPeriodStartDate": "another date",
      |      "taxPeriodEndDate": "an even later date",
      |      "chargeType": "some type thing",
      |      "amount": 600.00,
      |      "clearedAmount": 100.00
      |      }
      |    ]
      |   }
      | ]
      |}
      |""".stripMargin)

  val mtdJson: JsValue = Json.parse(
    """
      |{
      | "paymentDetails": [
      |   {
      |     "amount": 1000.00,
      |     "method": "buttons",
      |     "transactionDate": "a date",
      |     "allocations": [
      |       {
      |         "id": "someID",
      |         "from": "another date",
      |         "to": "an even later date",
      |         "type": "some type thing",
      |         "amount": 600.00,
      |         "clearedAmount": 100.00
      |       }
      |     ]
      |    }
      |  ]
      |}
      |""".stripMargin
  )

  val retrieveAllocationsResponse: RetrieveAllocationsResponse =
    RetrieveAllocationsResponse(
      Seq(PaymentDetails(
        1000.00,
        "buttons",
        "a date",
        Seq(
          AllocationDetail(
            "someID",
            "another date",
            "an even later date",
            "some type thing",
            600.00,
            100.00
          )
        )
      )
      )
    )

  "RetrieveAllocationResponse" when {
    "read from valid JSON" should {
      "return the expected RetrieveAllocationResponse object" in {
        desJson.as[RetrieveAllocationsResponse] shouldBe retrieveAllocationsResponse
      }
    }

    "read from invalid JSON" should {
      "return a JSError" in {
        invalidDesJson.validate[RetrieveAllocationsResponse] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "return the expected JsValue" in {
        Json.toJson(retrieveAllocationsResponse) shouldBe mtdJson
      }
    }
  }
}
