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

package v1.models.response.listPayments

import play.api.libs.json.{JsError, Json}
import support.UnitSpec

class PaymentSpec extends UnitSpec {

  private val desJson = Json.parse(
    """
      |{
      |  "paymentLot": "123",
      |  "paymentLotItem": "456",
      |  "paymentAmount": 10.25,
      |  "paymentMethod": "beans",
      |  "valueDate": "10/01/2020"
      |}
      |""".stripMargin)

  private val desJsonWithNoPL = Json.parse(
    """
      |{
      |  "paymentLotItem": "456",
      |  "paymentAmount": 10.25,
      |  "paymentMethod": "beans",
      |  "valueDate": "10/01/2020"
      |}
      |""".stripMargin)

  private val desJsonWithNoPLI = Json.parse(
    """
      |{
      |  "paymentLot": "123",
      |  "paymentAmount": 10.25,
      |  "paymentMethod": "beans",
      |  "valueDate": "10/01/2020"
      |}
      |""".stripMargin)

  private val invalidDesJson = Json.parse(
    """
      |{
      |  "paymentLot": 123,
      |  "paymentLotItem": "456",
      |  "paymentAmount": 10.25,
      |  "paymentMethod": "beans",
      |  "valueDate": "10/01/2020"
      |}
      |""".stripMargin)

  private val mtdModel = Payment(Some("123-456"), Some(10.25), Some("beans"), Some("10/01/2020"))
  private val mtdModelNoId = Payment(None, Some(10.25), Some("beans"), Some("10/01/2020"))

  private val mtdJson = Json.parse(
    """
      |{
      |  "id": "123-456",
      |  "amount": 10.25,
      |  "method": "beans",
      |  "transactionDate": "10/01/2020"
      |}
      |""".stripMargin)

  "Payment" should {
    "read to a valid model" when {
      "passed valid json" in {
        desJson.as[Payment] shouldBe mtdModel
      }
      "passed valid json with no paymentLot" in {
        desJsonWithNoPL.as[Payment] shouldBe mtdModelNoId
      }
      "passed valid json with no paymentLotItem" in {
        desJsonWithNoPLI.as[Payment] shouldBe mtdModelNoId
      }
    }
    "return a JsError" when {
      "passed invalid json" in {
        invalidDesJson.validate[Payment] shouldBe a[JsError]
      }
    }
    "write to valid json" when {
      "passed a valid model" in {
        Json.toJson(mtdModel) shouldBe mtdJson
      }
    }
  }
}
