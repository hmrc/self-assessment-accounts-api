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

package v1.models.response.detail

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class PaymentDetailsSpec extends UnitSpec{

  val desJson: JsValue = Json.parse(
    """
      |{
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
      |   ]
      |}
      |""".stripMargin)

  val paymentDetails: PaymentDetails =
    PaymentDetails(
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

  "PaymentDetails" when {
    "read from valid JSON" should {
      "return the expected RetrieveAllocationResponse object" in {
        desJson.as[PaymentDetails] shouldBe paymentDetails
      }
    }
  }
}
