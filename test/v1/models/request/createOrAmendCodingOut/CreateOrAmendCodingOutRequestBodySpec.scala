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

package v1.models.request.createOrAmendCodingOut

import play.api.libs.json.Json
import support.UnitSpec

class CreateOrAmendCodingOutRequestBodySpec extends UnitSpec {

  private val mtdJson = createOrAmendCodingOutMtdJson

  private val desJson = createOrAmendCodingOutDesJson

  private val requestBody = createOrAmendCodingOutRequestBody

  "CreateOrAmendCodingOutRequestBody" when {
    "read from a valid Json" should {
      "produce the expected body" in {
        mtdJson.as[CreateOrAmendCodingOutRequestBody] shouldBe requestBody
      }
    }

    "written to Json" should {
      "write to a valid json" in {
        Json.toJson(requestBody) shouldBe desJson
      }
    }

    "getEmptyFieldName" should {
      "return an empty Seq" when {
        "None is provided" in {
          requestBody.getEmptyFieldName(None, "fieldOne") shouldBe Seq()
        }
        "a non-empty Seq is provided" in {
          requestBody.getEmptyFieldName(Some(Seq(TaxCodeComponent(1, 1))), "fieldOne") shouldBe Seq()
        }
      }

      "return a field" when {
        "an empty Seq is provided" in {
          requestBody.getEmptyFieldName(Some(Seq()), "fieldOne") shouldBe Seq("/taxCodeComponents/fieldOne")
        }
      }
    }

    "emptyFields" should {
      "return a field name" when {
        "taxCodeComponents is empty" in {
          CreateOrAmendCodingOutRequestBody(TaxCodeComponents(None, None, None, None)).emptyFields shouldBe Seq("/taxCodeComponents")
        }
        "taxCodeComponents.payeUnderpayment is empty" in {
          CreateOrAmendCodingOutRequestBody(TaxCodeComponents(Some(Seq()), None, None, None)).emptyFields shouldBe Seq(
            "/taxCodeComponents/payeUnderpayment")
        }
        "taxCodeComponents.selfAssessmentUnderpayment is empty" in {
          CreateOrAmendCodingOutRequestBody(TaxCodeComponents(None, Some(Seq()), None, None)).emptyFields shouldBe Seq(
            "/taxCodeComponents/selfAssessmentUnderpayment")
        }
        "taxCodeComponents.debt is empty" in {
          CreateOrAmendCodingOutRequestBody(TaxCodeComponents(None, None, Some(Seq()), None)).emptyFields shouldBe Seq("/taxCodeComponents/debt")
        }
        "multiple fields are provided and at least one is empty" in {
          CreateOrAmendCodingOutRequestBody(TaxCodeComponents(Some(Seq(TaxCodeComponent(1, 1))), Some(Seq()), Some(Seq()), None)).emptyFields shouldBe Seq(
            "/taxCodeComponents/selfAssessmentUnderpayment",
            "/taxCodeComponents/debt")
        }
      }

      "return an empty Seq" when {
        "only inYearAdjustment is provided" in {
          CreateOrAmendCodingOutRequestBody(TaxCodeComponents(None, None, None, Some(TaxCodeComponent(1, 1)))).emptyFields shouldBe Seq()
        }
        "payeUnderpayment is provided and non-empty" in {
          CreateOrAmendCodingOutRequestBody(TaxCodeComponents(Some(Seq(TaxCodeComponent(1, 1))), None, None, None)).emptyFields shouldBe Seq()
        }
        "selfAssessmentUnderpayment is provided and non-empty" in {
          CreateOrAmendCodingOutRequestBody(TaxCodeComponents(None, Some(Seq(TaxCodeComponent(1, 1))), None, None)).emptyFields shouldBe Seq()
        }
        "debt is provided and non-empty" in {
          CreateOrAmendCodingOutRequestBody(TaxCodeComponents(None, None, Some(Seq(TaxCodeComponent(1, 1))), None)).emptyFields shouldBe Seq()
        }
        "multiple fields are provided and all are non-empty" in {
          CreateOrAmendCodingOutRequestBody(TaxCodeComponents(Some(Seq(TaxCodeComponent(1, 1))),
                                                              Some(Seq(TaxCodeComponent(1, 1))),
                                                              Some(Seq(TaxCodeComponent(1, 1))),
                                                              None)).emptyFields shouldBe Seq()
        }
      }
    }
  }

}
