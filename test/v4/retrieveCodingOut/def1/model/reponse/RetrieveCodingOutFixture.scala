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

package v4.retrieveCodingOut.def1.model.reponse

import play.api.libs.json.{JsObject, Json}
import v4.retrieveCodingOut.def1.model.response.{Def1_RetrieveCodingOutResponse, TaxCodeComponents, TaxCodeComponentsObject, UnmatchedCustomerSubmissions, UnmatchedCustomerSubmissionsObject}

object RetrieveCodingOutFixture {

  def retrieveCodingOutMtdResponse(taxYear: String, source: String): JsObject = Json
    .parse(
      s"""
         |{
         |  "taxCodeComponents": {
         |    "selfAssessmentUnderpayment": [
         |      {
         |        "amount": 0,
         |        "relatedTaxYear": "$taxYear",
         |        "submittedOn": "2021-08-24T14:15:22Z",
         |        "source": "$source",
         |        "id": 12345678910
         |      }
         |    ],
         |    "payeUnderpayment": [
         |      {
         |        "amount": 0,
         |        "relatedTaxYear": "$taxYear",
         |        "submittedOn": "2021-08-24T14:15:22Z",
         |        "source": "$source",
         |        "id": 12345678910
         |      }
         |    ],
         |    "debt": [
         |      {
         |        "amount": 0,
         |        "relatedTaxYear": "$taxYear",
         |        "submittedOn": "2021-08-24T14:15:22Z",
         |        "source": "$source",
         |        "id": 12345678910
         |      }
         |    ],
         |    "inYearAdjustment": {
         |      "amount": 0,
         |      "relatedTaxYear": "$taxYear",
         |      "submittedOn": "2021-08-24T14:15:22Z",
         |      "source": "$source",
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
    .as[JsObject]

  def retrieveCodingOutMtdResponseNoId(taxYear: String, source: String): JsObject = Json
    .parse(
      s"""
         |{
         |  "taxCodeComponents": {
         |    "selfAssessmentUnderpayment": [
         |      {
         |        "amount": 0,
         |        "relatedTaxYear": "$taxYear",
         |        "submittedOn": "2021-08-24T14:15:22Z",
         |        "source": "$source"
         |      }
         |    ],
         |    "payeUnderpayment": [
         |      {
         |        "amount": 0,
         |        "relatedTaxYear": "$taxYear",
         |        "submittedOn": "2021-08-24T14:15:22Z",
         |        "source": "$source"
         |      }
         |    ],
         |    "debt": [
         |      {
         |        "amount": 0,
         |        "relatedTaxYear": "$taxYear",
         |        "submittedOn": "2021-08-24T14:15:22Z",
         |        "source": "$source"
         |      }
         |    ],
         |    "inYearAdjustment": {
         |      "amount": 0,
         |      "relatedTaxYear": "$taxYear",
         |      "submittedOn": "2021-08-24T14:15:22Z",
         |      "source": "$source"
         |    }
         |  },
         |  "unmatchedCustomerSubmissions": {
         |    "selfAssessmentUnderpayment": [
         |      {
         |        "amount": 0,
         |        "submittedOn": "2021-08-24T14:15:22Z"
         |      }
         |    ],
         |    "payeUnderpayment": [
         |      {
         |        "amount": 0,
         |        "submittedOn": "2021-08-24T14:15:22Z"
         |      }
         |    ],
         |    "debt": [
         |      {
         |        "amount": 0,
         |        "submittedOn": "2021-08-24T14:15:22Z"
         |      }
         |    ],
         |    "inYearAdjustment": {
         |      "amount": 0,
         |      "submittedOn": "2021-08-24T14:15:22Z"
         |    }
         |  }
         |}
     """.stripMargin
    )
    .as[JsObject]

  val unmatchedCustomerSubmissions: UnmatchedCustomerSubmissions =
    UnmatchedCustomerSubmissions(
      0,
      "2021-08-24T14:15:22Z",
      Some(BigInt(12345678910L))
    )

  val taxCodeComponents: TaxCodeComponents =
    TaxCodeComponents(
      0,
      Some("2021-22"),
      "2021-08-24T14:15:22Z",
      "hmrcHeld",
      Some(BigInt(12345678910L))
    )

  val taxCodeComponentObject: TaxCodeComponentsObject =
    TaxCodeComponentsObject(
      Some(List(taxCodeComponents)),
      Some(List(taxCodeComponents)),
      Some(List(taxCodeComponents)),
      Some(taxCodeComponents)
    )

  val unmatchedCustomerSubmissionsObject: UnmatchedCustomerSubmissionsObject =
    UnmatchedCustomerSubmissionsObject(
      Some(List(unmatchedCustomerSubmissions)),
      Some(List(unmatchedCustomerSubmissions)),
      Some(List(unmatchedCustomerSubmissions)),
      Some(unmatchedCustomerSubmissions)
    )

  val retrieveCodingOutResponse: Def1_RetrieveCodingOutResponse =
    Def1_RetrieveCodingOutResponse(
      Some(taxCodeComponentObject),
      Some(unmatchedCustomerSubmissionsObject)
    )

}
