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

package v1.fixtures

import play.api.libs.json.{JsObject, Json}

object RetrieveCodingOutFixture {

  def mtdResponseWithHateoas(nino: String, taxYear: String, source: String): JsObject = Json.parse(
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
       |  },
       |   "links": [
       |      {
       |         "href": "/accounts/self-assessment/$nino/$taxYear/collection/tax-code",
       |         "method": "PUT",
       |         "rel": "create-or-amend-coding-out-underpayments"
       |      },
       |      {
       |         "href": "/accounts/self-assessment/$nino/$taxYear/collection/tax-code",
       |         "method": "GET",
       |         "rel": "self"
       |      },
       |      {
       |         "href": "/accounts/self-assessment/$nino/$taxYear/collection/tax-code",
       |         "method": "DELETE",
       |         "rel": "delete-coding-out-underpayments"
       |      }
       |   ]
       |}
     """.stripMargin
  ).as[JsObject]

  def mtdResponseWithHateoasNoId(nino: String, taxYear: String, source: String): JsObject = Json.parse(
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
       |  },
       |   "links": [
       |      {
       |         "href": "/accounts/self-assessment/$nino/$taxYear/collection/tax-code",
       |         "method": "PUT",
       |         "rel": "create-or-amend-coding-out-underpayments"
       |      },
       |      {
       |         "href": "/accounts/self-assessment/$nino/$taxYear/collection/tax-code",
       |         "method": "GET",
       |         "rel": "self"
       |      },
       |      {
       |         "href": "/accounts/self-assessment/$nino/$taxYear/collection/tax-code",
       |         "method": "DELETE",
       |         "rel": "delete-coding-out-underpayments"
       |      }
       |   ]
       |}
     """.stripMargin
  ).as[JsObject]
}