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

class RetrieveCodingOutResponseSpec extends UnitSpec {

  val desResponse: JsValue = Json.parse(
    """
      |{
      |  "taxCodeComponents": {
      |    "selfAssessmentUnderpayment": [
      |      {
      |        "amount": 0,
      |        "relatedTaxYear": "2019-20",
      |        "submittedOn": "2019-08-24T14:15:22Z",
      |        "source": "HMRC HELD",
      |        "componentIdentifier": 12345678910
      |      }
      |    ],
      |    "payeUnderpayment": [
      |      {
      |        "amount": 0,
      |        "relatedTaxYear": "2019-20",
      |        "submittedOn": "2019-08-24T14:15:22Z",
      |        "source": "HMRC HELD",
      |        "componentIdentifier": 12345678910
      |      }
      |    ],
      |    "debt": [
      |      {
      |        "amount": 0,
      |        "relatedTaxYear": "2019-20",
      |        "submittedOn": "2019-08-24T14:15:22Z",
      |        "source": "CUSTOMER",
      |        "componentIdentifier": 12345678910
      |      }
      |    ],
      |    "inYearAdjustment": {
      |      "amount": 0,
      |      "relatedTaxYear": "2019-20",
      |      "submittedOn": "2019-08-24T14:15:22Z",
      |      "source": "CUSTOMER",
      |      "componentIdentifier": 12345678910
      |    }
      |  },
      |  "unmatchedCustomerSubmissions": {
      |    "selfAssessmentUnderpayment": [
      |      {
      |        "amount": 0,
      |        "submittedOn": "2019-08-24T14:15:22Z",
      |        "componentIdentifier": 12345678910
      |      }
      |    ],
      |    "payeUnderpayment": [
      |      {
      |        "amount": 0,
      |        "submittedOn": "2019-08-24T14:15:22Z",
      |        "componentIdentifier": 12345678910
      |      }
      |    ],
      |    "debt": [
      |      {
      |        "amount": 0,
      |        "submittedOn": "2019-08-24T14:15:22Z",
      |        "componentIdentifier": 12345678910
      |      }
      |    ],
      |    "inYearAdjustment": {
      |      "amount": 0,
      |      "submittedOn": "2019-08-24T14:15:22Z",
      |      "componentIdentifier": 12345678910
      |    }
      |  }
      |}
      |""".stripMargin
  )

  val invalidDesResponse: JsValue = Json.parse(
    """
      |{
      |  "taxCode": {
      |    "selfAssessmentUnderpayment": [
      |      {
      |        "amount": 0,
      |        "relatedTaxYear": "2019-20",
      |        "submittedOn": "2019-08-24T14:15:22Z",
      |        "source": "HMRC-HELD",
      |        "componentIdentifier": 12345678910
      |      }
      |    ],
      |    "payeUnderpayment": [
      |      {
      |        "amount": 0,
      |        "relatedTaxYear": "2019-20",
      |        "submittedOn": "2019-08-24T14:15:22Z",
      |        "source": "HMRCHELD",
      |        "componentIdentifier": "12345678910"
      |      }
      |    ],
      |    "debt": [
      |      {
      |        "amount": 0,
      |        "relatedTaxYear": "2019-20",
      |        "submittedOn": "2019-08-24T14:15:22Z",
      |        "source": "CUSTOMER",
      |        "componentIdentifier": 12345678910
      |      }
      |    ],
      |    "inYearAdjustments": {
      |      "amount": 0,
      |      "relatedTaxYear": "2019-20",
      |      "submittedOn": "2019-08-24T14:15:22Z",
      |      "source": "CUSTOMER",
      |      "componentIdentifier": 12345678910
      |    }
      |  },
      |  "unmatchedCustomer": {
      |    "selfAssessmentUnderpayment": [
      |      {
      |        "amount": 0,
      |        "submittedOn": "2019-08-24T14:15:22Z",
      |        "componentIdentifier": 12345678910
      |      }
      |    ],
      |    "payeUnderpayment": [
      |      {
      |        "amount": 0,
      |        "submittedOn": "2019-08-24T14:15:22Z",
      |        "componentIdentifier": 12345678910
      |      }
      |    ],
      |    "debt": [
      |      {
      |        "amount": 0,
      |        "submittedOn": "2019-08-24T14:15:22Z",
      |        "componentIdentifier": 12345678910
      |      }
      |    ],
      |    "inYearAdjustments": {
      |      "amount": 0,
      |      "submittedOn": "2019-08-24T14:15:22Z",
      |      "componentIdentifier": 12345678910
      |    }
      |  }
      |}
      |""".stripMargin
  )

  val mtdResponse: JsValue = Json.parse(
    """
      |{
      |  "taxCodeComponents": {
      |    "selfAssessmentUnderpayment": [
      |      {
      |        "amount": 0,
      |        "relatedTaxYear": "2019-20",
      |        "submittedOn": "2019-08-24T14:15:22Z",
      |        "source": "HMRC-HELD",
      |        "id": 12345678910
      |      }
      |    ],
      |    "payeUnderpayment": [
      |      {
      |        "amount": 0,
      |        "relatedTaxYear": "2019-20",
      |        "submittedOn": "2019-08-24T14:15:22Z",
      |        "source": "HMRC-HELD",
      |        "id": 12345678910
      |      }
      |    ],
      |    "debt": [
      |      {
      |        "amount": 0,
      |        "relatedTaxYear": "2019-20",
      |        "submittedOn": "2019-08-24T14:15:22Z",
      |        "source": "CUSTOMER",
      |        "id": 12345678910
      |      }
      |    ],
      |    "inYearAdjustments": {
      |      "amount": 0,
      |      "relatedTaxYear": "2019-20",
      |      "submittedOn": "2019-08-24T14:15:22Z",
      |      "source": "CUSTOMER",
      |      "id": 12345678910
      |    }
      |  },
      |  "unmatchedCustomerSubmissions": {
      |    "selfAssessmentUnderpayment": [
      |      {
      |        "amount": 0,
      |        "submittedOn": "2019-08-24T14:15:22Z",
      |        "id": 12345678910
      |      }
      |    ],
      |    "payeUnderpayment": [
      |      {
      |        "amount": 0,
      |        "submittedOn": "2019-08-24T14:15:22Z",
      |        "id": 12345678910
      |      }
      |    ],
      |    "debt": [
      |      {
      |        "amount": 0,
      |        "submittedOn": "2019-08-24T14:15:22Z",
      |        "id": 12345678910
      |      }
      |    ],
      |    "inYearAdjustments": {
      |      "amount": 0,
      |      "submittedOn": "2019-08-24T14:15:22Z",
      |      "id": 12345678910
      |    }
      |  }
      |}
    """.stripMargin
  )

  val unmatchedCustomerSubmissions: UnmatchedCustomerSubmissions =
    UnmatchedCustomerSubmissions(
      BigInt(12345678910L),
      0,
      "2019-08-24T14:15:22Z"
    )

  val taxCodeComponentsHmrcHeld: TaxCodeComponents =
    TaxCodeComponents(
      BigInt(12345678910L),
      0,
      Some("2019-20"),
      "2019-08-24T14:15:22Z",
      "HMRC-HELD"
    )

  val taxCodeComponentsCustomer: TaxCodeComponents =
    TaxCodeComponents(
      BigInt(12345678910L),
      0,
      Some("2019-20"),
      "2019-08-24T14:15:22Z",
      "CUSTOMER"
    )

  val taxCodeComponentObject: TaxCodeComponentsObject =
    TaxCodeComponentsObject(
      Some(Seq(taxCodeComponentsHmrcHeld)),
      Some(Seq(taxCodeComponentsHmrcHeld)),
      Some(Seq(taxCodeComponentsCustomer)),
      Some(taxCodeComponentsCustomer)
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

    "read from invalid JSON" should {
      "produce an empty RetrieveCodingOutResponse object" in {
        invalidDesResponse.validate[RetrieveCodingOutResponse] shouldBe a[JsError]
      }
    }

    "written to JSON" should {
      "produce the expected JSON object" in {
        Json.toJson(responseModel) shouldBe mtdResponse
      }
    }
  }
}
