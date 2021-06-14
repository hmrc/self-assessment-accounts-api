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

class UnmatchedCustomerSubmissionsSpec extends UnitSpec {

  val desResponse: JsValue = Json.parse(
    """
      |{
      |    "amount": 0,
      |    "submittedOn": "2019-08-24T14:15:22Z",
      |    "componentIdentifier": 12345678910
      |}
      |""".stripMargin
  )

  val invalidDesResponse: JsValue = Json.parse(
    """
      |{
      |    "amounts": 0,
      |    "submit": "2019-08-24T14:15:22Z",
      |    "componentIdentifier": 12345678910
      |}
      |""".stripMargin
  )

  val mtdResponse: JsValue = Json.parse(
    """
      |{
      |    "amount": 0,
      |    "submittedOn": "2019-08-24T14:15:22Z",
      |    "id": 12345678910
      |}
    """.stripMargin
  )

  val responseModel: UnmatchedCustomerSubmissions =
    UnmatchedCustomerSubmissions(
      0,
      "2019-08-24T14:15:22Z",
      BigInt(12345678910L)
    )

  "UnmatchedCustomerSubmissions" when {
    "read from valid JSON" should {
      "produce the expected UnmatchedCustomerSubmissions object" in {
        desResponse.as[UnmatchedCustomerSubmissions] shouldBe responseModel
      }
    }

    "read from invalid JSON" should {
      "produce an empty UnmatchedCustomerSubmissions object" in {
        invalidDesResponse.validate[UnmatchedCustomerSubmissions] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON object" in {
        Json.toJson(responseModel) shouldBe mtdResponse
      }
    }
  }
}
