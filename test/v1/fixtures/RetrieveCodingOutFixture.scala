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
       |   "source": "$source",
       |   "selfAssessmentUnderPayments": [
       |      {
       |         "amount": 87.78,
       |         "relatedTaxYear": "$taxYear",
       |         "submittedOn": "2021-07-06T09:37:17Z"
       |      }
       |   ],
       |   "payeUnderpayments": [
       |      {
       |         "amount": 12.45,
       |         "relatedTaxYear": "$taxYear",
       |         "submittedOn": "2021-07-06T09:37:17Z"
       |      }
       |   ],
       |   "debts": [
       |      {
       |         "amount": 10.01,
       |         "relatedTaxYear": "$taxYear",
       |         "submittedOn": "2021-07-06T09:37:17Z"
       |      }
       |   ],
       |   "inYearAdjustments": {
       |      "amount": 99.99,
       |      "relatedTaxYear": "$taxYear",
       |      "submittedOn": "2021-07-06T09:37:17Z"
       |   },
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