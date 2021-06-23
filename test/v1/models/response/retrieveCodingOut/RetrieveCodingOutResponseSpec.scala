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

import play.api.libs.json.{JsValue, Json}
import support.UnitSpec

class RetrieveCodingOutResponseSpec extends UnitSpec {

  val desResponse: JsValue = Json.parse(
    """
      |{
      |  "taxCodeComponents": {
      |    "selfAssessmentUnderpayment": [
      |      {
      |        "amount": 0,
      |        "relatedTaxYear": "2021-22",
      |        "submittedOn": "2021-08-24T14:15:22Z",
      |        "source": "HMRC HELD",
      |        "componentIdentifier": 12345678910
      |      }
      |    ],
      |    "payeUnderpayment": [
      |      {
      |        "amount": 0,
      |        "relatedTaxYear": "2021-22",
      |        "submittedOn": "2021-08-24T14:15:22Z",
      |        "source": "HMRC HELD",
      |        "componentIdentifier": 12345678910
      |      }
      |    ],
      |    "debt": [
      |      {
      |        "amount": 0,
      |        "relatedTaxYear": "2021-22",
      |        "submittedOn": "2021-08-24T14:15:22Z",
      |        "source": "HMRC HELD",
      |        "componentIdentifier": 12345678910
      |      }
      |    ],
      |    "inYearAdjustment": {
      |      "amount": 0,
      |      "relatedTaxYear": "2021-22",
      |      "submittedOn": "2021-08-24T14:15:22Z",
      |      "source": "HMRC HELD",
      |      "componentIdentifier": 12345678910
      |    }
      |  },
      |  "unmatchedCustomerSubmissions": {
      |    "selfAssessmentUnderpayment": [
      |      {
      |        "amount": 0,
      |        "submittedOn": "2021-08-24T14:15:22Z",
      |        "componentIdentifier": 12345678910
      |      }
      |    ],
      |    "payeUnderpayment": [
      |      {
      |        "amount": 0,
      |        "submittedOn": "2021-08-24T14:15:22Z",
      |        "componentIdentifier": 12345678910
      |      }
      |    ],
      |    "debt": [
      |      {
      |        "amount": 0,
      |        "submittedOn": "2021-08-24T14:15:22Z",
      |        "componentIdentifier": 12345678910
      |      }
      |    ],
      |    "inYearAdjustment": {
      |      "amount": 0,
      |      "submittedOn": "2021-08-24T14:15:22Z",
      |      "componentIdentifier": 12345678910
      |    }
      |  }
      |}
    """.stripMargin
  )

  val mtdResponse: JsValue = Json.parse(
    """
      |{
      |  "taxCodeComponents": {
      |    "selfAssessmentUnderpayment": [
      |      {
      |        "amount": 0,
      |        "relatedTaxYear": "2021-22",
      |        "submittedOn": "2021-08-24T14:15:22Z",
      |        "source": "hmrcHeld",
      |        "id": 12345678910
      |      }
      |    ],
      |    "payeUnderpayment": [
      |      {
      |        "amount": 0,
      |        "relatedTaxYear": "2021-22",
      |        "submittedOn": "2021-08-24T14:15:22Z",
      |        "source": "hmrcHeld",
      |        "id": 12345678910
      |      }
      |    ],
      |    "debt": [
      |      {
      |        "amount": 0,
      |        "relatedTaxYear": "2021-22",
      |        "submittedOn": "2021-08-24T14:15:22Z",
      |        "source": "hmrcHeld",
      |        "id": 12345678910
      |      }
      |    ],
      |    "inYearAdjustment": {
      |      "amount": 0,
      |      "relatedTaxYear": "2021-22",
      |      "submittedOn": "2021-08-24T14:15:22Z",
      |      "source": "hmrcHeld",
      |      "id": 12345678910
      |    }
      |  },
      |  "unmatchedCustomerSubmissions": {
      |    "selfAssessmentUnderpayment": [
      |      {
      |        "amount": 0,
      |        "submittedOn": "2021-08-24T14:15:22Z",
      |        "id": 12345678910
      |      }
      |    ],
      |    "payeUnderpayment": [
      |      {
      |        "amount": 0,
      |        "submittedOn": "2021-08-24T14:15:22Z",
      |        "id": 12345678910
      |      }
      |    ],
      |    "debt": [
      |      {
      |        "amount": 0,
      |        "submittedOn": "2021-08-24T14:15:22Z",
      |        "id": 12345678910
      |      }
      |    ],
      |    "inYearAdjustment": {
      |      "amount": 0,
      |      "submittedOn": "2021-08-24T14:15:22Z",
      |      "id": 12345678910
      |    }
      |  }
      |}
    """.stripMargin
  )

  val unmatchedCustomerSubmissions: UnmatchedCustomerSubmissions =
    UnmatchedCustomerSubmissions(
      0,
      "2021-08-24T14:15:22Z",
      BigInt(12345678910L)
    )

  val taxCodeComponents: TaxCodeComponents =
    TaxCodeComponents(
      0,
      Some("2021-22"),
      "2021-08-24T14:15:22Z",
      "hmrcHeld",
      BigInt(12345678910L)
    )

  val taxCodeComponentObject: TaxCodeComponentsObject =
    TaxCodeComponentsObject(
      Some(Seq(taxCodeComponents)),
      Some(Seq(taxCodeComponents)),
      Some(Seq(taxCodeComponents)),
      Some(taxCodeComponents)
    )

  val unmatchedCustomerSubmissionsObject: UnmatchedCustomerSubmissionsObject =
    UnmatchedCustomerSubmissionsObject(
      Some(Seq(unmatchedCustomerSubmissions)),
      Some(Seq(unmatchedCustomerSubmissions)),
      Some(Seq(unmatchedCustomerSubmissions)),
      Some(unmatchedCustomerSubmissions)
    )

  val responseModel: RetrieveCodingOutResponse =
    RetrieveCodingOutResponse(
      Some(taxCodeComponentObject),
      Some(unmatchedCustomerSubmissionsObject)
    )

  "RetrieveCodingOutResponse" when {
    "read from valid JSON" should {
      "produce the expected RetrieveCodingOutResponse object" in {
        desResponse.as[RetrieveCodingOutResponse] shouldBe responseModel
      }
    }

    "written to JSON" should {
      "produce the expected JSON object" in {
        Json.toJson(responseModel) shouldBe mtdResponse
      }
    }
  }
}