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

package v1.models.response.retrieveAllocations.detail

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class AllocationDetailSpec extends UnitSpec {

  val desJson: JsValue = Json.parse(
    """
      | {
      |   "sapDocNumber": "someID",
      |   "taxPeriodStartDate": "another date",
      |   "taxPeriodEndDate": "an even later date",
      |   "chargeType": "some type thing",
      |   "amount": 600.00,
      |   "clearedAmount": 100.00
      |   }
      |""".stripMargin)

  val desJsonWithMissing: JsValue = Json.parse(
    """
      | {
      |   "taxPeriodStartDate": "another date",
      |   "chargeType": "some type thing",
      |   "clearedAmount": 100.00
      |   }
      |""".stripMargin)

  val mtdJson: JsValue = Json.parse(
    """
      |{
      | "id": "someID",
      | "from": "another date",
      | "to": "an even later date",
      | "type": "some type thing",
      | "amount": 600.00,
      | "clearedAmount": 100.00
      |}
      |""".stripMargin
  )

  val mtdJsonWithMissing: JsValue = Json.parse(
    """
      |{
      | "from": "another date",
      | "type": "some type thing",
      | "clearedAmount": 100.00
      |}
      |""".stripMargin
  )

  val allocationDetail: AllocationDetail =
    AllocationDetail(
      Some("someID"),
      Some("another date"),
      Some("an even later date"),
      Some("some type thing"),
      Some(600.00),
      Some(100.00))

  val allocationDetailWithMissing: AllocationDetail =
    AllocationDetail(
      None,
      Some("another date"),
      None,
      Some("some type thing"),
      None,
      Some(100.00))

  "AllocationDetail" when {
    "read from valid JSON" should {
      "return the expected RetrieveAllocationResponse object" in {
        desJson.as[AllocationDetail] shouldBe allocationDetail
      }

      "written to JSON" should {
        "return the expected JSValue" in {
          Json.toJson(allocationDetail) shouldBe mtdJson
        }
      }

      "read from valid JSON with missing values" should {
        "return the expected RetrieveAllocationResponse object" in {
          desJsonWithMissing.as[AllocationDetail] shouldBe allocationDetailWithMissing
        }
      }

      "written to JSON with missing values" should {
        "return the expected JSValue" in {
          Json.toJson(allocationDetailWithMissing) shouldBe mtdJsonWithMissing
        }
      }

    }
  }
}
