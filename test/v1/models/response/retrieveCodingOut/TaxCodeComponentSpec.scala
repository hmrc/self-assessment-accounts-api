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

package v1.models.response.retrieveCodingOut

import play.api.libs.json.{JsError, JsValue, Json}
import support.UnitSpec

class TaxCodeComponentSpec extends UnitSpec {

  val desResponse: JsValue = Json.parse(
    """
      |{
      |  "amount": 87.78,
      |  "relatedTaxYear": "2021-22",
      |  "submittedOn": "2021-07-06T09:37:17Z"
      |}
      |""".stripMargin
  )

  val invalidDesResponse: JsValue = Json.parse(
    """
      |{
      |  "amount": 87.78,
      |  "relatedTaxYear": 2021,
      |  "submittedOn": "2021-07-06T09:37:17Z"
      |}
      |""".stripMargin
  )

  val mtdResponse: JsValue = Json.parse(
    """
      |{
      |  "amount": 87.78,
      |  "relatedTaxYear": "2021-22",
      |  "submittedOn": "2021-07-06T09:37:17Z"
      |}
    """.stripMargin
  )

  val responseItemModel: TaxCodeComponent =
    TaxCodeComponent(
      87.78,
      "2021-22",
      "2021-07-06T09:37:17Z"
    )

  "ResponseItem" when {
    "read from valid JSON" should {
      "produce the expected ResponseItem object" in {
        desResponse.as[TaxCodeComponent] shouldBe responseItemModel
      }
    }

    "read from invalid JSON" should {
      "produce an empty ResponseItem object" in {
        invalidDesResponse.validate[TaxCodeComponent] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON object" in {
        Json.toJson(responseItemModel) shouldBe mtdResponse
      }
    }
  }
}
