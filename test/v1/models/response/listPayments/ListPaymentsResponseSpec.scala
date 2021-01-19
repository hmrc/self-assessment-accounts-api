/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.models.response.listPayments

import play.api.libs.json.{JsError, Json}
import support.UnitSpec

class ListPaymentsResponseSpec extends UnitSpec {

  private val desJson = Json.parse(
    """
      |{
      |  "paymentDetails": [
      |    {
      |      "paymentLot": "123",
      |      "paymentLotItem": "456",
      |      "paymentAmount": 10.25,
      |      "paymentMethod": "beans",
      |      "valueDate": "10/01/2020"
      |    }
      |  ]
      |}
      |""".stripMargin)

  private val invalidDesJson = Json.parse(
    """
      |{
      |  "notPaymentDetails": [
      |    {
      |      "paymentLot": "123",
      |      "paymentLotItem": "456",
      |      "paymentAmount": 10.25,
      |      "paymentMethod": "beans",
      |      "valueDate": "10/01/2020"
      |    }
      |  ]
      |}
      |""".stripMargin)

  private val mtdModel = ListPaymentsResponse(Seq(Payment(Some("123-456"), Some(10.25), Some("beans"), Some("10/01/2020"))))

  private val mtdJson = Json.parse(
    """
      |{
      |  "payments": [
      |    {
      |      "paymentId": "123-456",
      |      "amount": 10.25,
      |      "method": "beans",
      |      "transactionDate": "10/01/2020"
      |    }
      |  ]
      |}
      |""".stripMargin)

  "ListPaymentsResponse" when {
    "passed valid json" should {
      "read to a valid model" in {
        desJson.as[ListPaymentsResponse[Payment]] shouldBe mtdModel
      }
    }
    "passed invalid json" should {
      "return a JsError" in {
        invalidDesJson.validate[ListPaymentsResponse[Payment]] shouldBe a[JsError]
      }
    }
    "passed a valid model" should {
      "write to valid json" in {
        Json.toJson(mtdModel) shouldBe mtdJson
      }
    }
  }
}
