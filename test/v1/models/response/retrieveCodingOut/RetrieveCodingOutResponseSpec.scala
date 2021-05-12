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
import v1.models.domain.MtdSource

class RetrieveCodingOutResponseSpec extends UnitSpec {

  val desResponse: JsValue = Json.parse(
    """
      |{
      |      "source": "HMRC HELD",
      |      "taxCodeComponents": {
      |        "selfAssessmentUnderPayments": [
      |          {
      |            "amount": 87.78,
      |            "relatedTaxYear": "2020-21",
      |            "submittedOn": "2021-07-06T09:37:17Z"
      |          }
      |        ],
      |        "payeUnderpayments": [
      |          {
      |            "amount": 12.45,
      |            "relatedTaxYear": "2021-22",
      |            "submittedOn": "2021-07-06T09:37:17Z"
      |          }
      |        ],
      |        "debts": [
      |          {
      |            "amount": 10.01,
      |            "relatedTaxYear": "2021-22",
      |            "submittedOn": "2021-07-06T09:37:17Z"
      |          }
      |        ],
      |        "inYearAdjustments": {
      |          "amount": 99.99,
      |          "relatedTaxYear": "2021-22",
      |          "submittedOn": "2021-07-06T09:37:17Z"
      |        }
      |      }
      |    }
      |""".stripMargin
  )

  val invalidDesResponse: JsValue = Json.parse(
    """
      |{
      |      "source": "HMRC HELD",
      |      "taxCodeComponents": {
      |        "selfAssessmentUnderPayments": [
      |          {
      |            "amount": 87.78,
      |            "relatedTaxYear": 2020,
      |            "submittedOn": "2021-07-06T09:37:17Z"
      |          }
      |        ],
      |        "payeUnderpayments": [
      |          {
      |            "amount": 12.45,
      |            "relatedTaxYear": "2021-22",
      |            "submittedOn": "2021-07-06T09:37:17Z"
      |          }
      |        ],
      |        "debts": [
      |          {
      |            "amount": 10.01,
      |            "relatedTaxYear": "2021-22",
      |            "submittedOn": "2021-07-06T09:37:17Z"
      |          }
      |        ],
      |        "inYearAdjustments": {
      |          "amount": 99.99,
      |          "relatedTaxYear": "2021-22",
      |          "submittedOn": "2021-07-06T09:37:17Z"
      |        }
      |      }
      |    }
      |""".stripMargin
  )

  val mtdResponse: JsValue = Json.parse(
    """
      |{
      |    "source": "hmrcHeld",
      |    "taxCodeComponents": {
      |        "selfAssessmentUnderPayments": [
      |            {
      |                "amount": 87.78,
      |                "relatedTaxYear": "2020-21",
      |                "submittedOn": "2021-07-06T09:37:17Z"
      |            }
      |        ],
      |        "payeUnderpayments": [
      |            {
      |                "amount": 12.45,
      |                "relatedTaxYear": "2021-22",
      |                "submittedOn": "2021-07-06T09:37:17Z"
      |            }
      |        ],
      |        "debts": [
      |            {
      |                "amount": 10.01,
      |                "relatedTaxYear": "2021-22",
      |                "submittedOn": "2021-07-06T09:37:17Z"
      |            }
      |        ],
      |        "inYearAdjustments": {
      |            "amount": 99.99,
      |            "relatedTaxYear": "2021-22",
      |            "submittedOn": "2021-07-06T09:37:17Z"
      |        }
      |      }
      |    }
    """.stripMargin
  )


  val responseModel: RetrieveCodingOutResponse =
    RetrieveCodingOutResponse(
      MtdSource.`hmrcHeld`,
      TaxCodeComponents(
        Some(Seq(ResponseItem(87.78, "2020-21", "2021-07-06T09:37:17Z"))),
        Some(Seq(ResponseItem(12.45, "2021-22", "2021-07-06T09:37:17Z"))),
        Some(Seq(ResponseItem(10.01, "2021-22", "2021-07-06T09:37:17Z"))),
        Some(ResponseItem(99.99, "2021-22", "2021-07-06T09:37:17Z"))
      )
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
