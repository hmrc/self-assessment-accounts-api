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

package v1.models.response.retrieveCodingOut

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class UnmatchedCustomerSubmissionsObjectSpec extends UnitSpec {

  val desResponse: JsValue = Json.parse(
    """
      |{
      |    "selfAssessmentUnderpayment": [
      |      {
      |        "amount": 0,
      |        "submittedOn": "2019-08-24T14:15:22Z",
      |        "componentIdentifier": "12345678910"
      |      }
      |    ],
      |    "payeUnderpayment": [
      |      {
      |        "amount": 0,
      |        "submittedOn": "2019-08-24T14:15:22Z",
      |        "componentIdentifier": "12345678910"
      |      }
      |    ],
      |    "debt": [
      |      {
      |        "amount": 0,
      |        "submittedOn": "2019-08-24T14:15:22Z",
      |        "componentIdentifier": "12345678910"
      |      }
      |    ],
      |    "inYearAdjustment": {
      |      "amount": 0,
      |      "submittedOn": "2019-08-24T14:15:22Z",
      |      "componentIdentifier": "12345678910"
      |  }
      |}
      |""".stripMargin
  )

  val mtdResponse: JsValue = Json.parse(
    """
      |{
      |    "selfAssessmentUnderpayment": [
      |      {
      |        "id": 12345678910,
      |        "amount": 0,
      |        "submittedOn": "2019-08-24T14:15:22Z"
      |      }
      |    ],
      |    "payeUnderpayment": [
      |      {
      |        "id": 12345678910,
      |        "amount": 0,
      |        "submittedOn": "2019-08-24T14:15:22Z"
      |      }
      |    ],
      |    "debt": [
      |      {
      |        "id": 12345678910,
      |        "amount": 0,
      |        "submittedOn": "2019-08-24T14:15:22Z"
      |      }
      |    ],
      |    "inYearAdjustment": {
      |        "id": 12345678910,
      |        "amount": 0,
      |        "submittedOn": "2019-08-24T14:15:22Z"
      |  }
      |}
    """.stripMargin
  )

  val unmatchedCustomerSubmissions: UnmatchedCustomerSubmissions =
    UnmatchedCustomerSubmissions(
      0,
      "2019-08-24T14:15:22Z",
      Some(BigInt(12345678910L))
    )

  val responseModel: UnmatchedCustomerSubmissionsObject =
    UnmatchedCustomerSubmissionsObject(
      Some(Seq(unmatchedCustomerSubmissions)),
      Some(Seq(unmatchedCustomerSubmissions)),
      Some(Seq(unmatchedCustomerSubmissions)),
      Some(unmatchedCustomerSubmissions)
    )

  "UnmatchedCustomerSubmissionsObject" when {
    "read from valid JSON" should {
      "produce the expected UnmatchedCustomerSubmissionsObject object" in {
        desResponse.as[UnmatchedCustomerSubmissionsObject] shouldBe responseModel
      }
    }

    "written to JSON" should {
      "produce the expected JSON object" in {
        Json.toJson(responseModel) shouldBe mtdResponse
      }
    }
  }

}
