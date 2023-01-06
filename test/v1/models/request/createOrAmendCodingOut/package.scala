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

package v1.models.request

import play.api.libs.json.{JsValue, Json}

package object createOrAmendCodingOut {
  val taxCodeComponent: TaxCodeComponent = TaxCodeComponent(id = 12345, amount = 123.45)

  val taxCodeComponents: TaxCodeComponents = TaxCodeComponents(
    payeUnderpayment = Some(Seq(taxCodeComponent)),
    selfAssessmentUnderpayment = Some(Seq(taxCodeComponent)),
    debt = Some(Seq(taxCodeComponent)),
    inYearAdjustment = Some(taxCodeComponent)
  )

  val createOrAmendCodingOutRequestBody: CreateOrAmendCodingOutRequestBody = CreateOrAmendCodingOutRequestBody(taxCodeComponents = taxCodeComponents)

  val taxCodeComponentMtdJson: JsValue = Json.parse("""
      |{
      |  "amount": 123.45,
      |  "id": 12345
      |}
      |""".stripMargin)

  val taxCodeComponentDesJson: JsValue = Json.parse("""
      |{
      |  "componentIdentifier": "12345",
      |  "amount": 123.45
      |}
      |""".stripMargin)

  val taxCodeComponentsMtdJson: JsValue = Json.parse(s"""
      |{
      |  "payeUnderpayment": [
      |    $taxCodeComponentMtdJson
      |  ],
      |  "selfAssessmentUnderpayment": [
      |    $taxCodeComponentMtdJson
      |  ],
      |  "debt": [
      |    $taxCodeComponentMtdJson
      |  ],
      |  "inYearAdjustment": $taxCodeComponentMtdJson
      |}
      |""".stripMargin)

  val taxCodeComponentsDesJson: JsValue = Json.parse(s"""
      |{
      |  "payeUnderpayment": [
      |    $taxCodeComponentDesJson
      |  ],
      |  "selfAssessmentUnderpayment": [
      |    $taxCodeComponentDesJson
      |  ],
      |  "debt": [
      |    $taxCodeComponentDesJson
      |  ],
      |  "inYearAdjustment": $taxCodeComponentDesJson
      |}
      |""".stripMargin)

  val createOrAmendCodingOutMtdJson: JsValue = Json.parse(s"""
      |{
      |  "taxCodeComponents": $taxCodeComponentsMtdJson
      |}
      |""".stripMargin)

  val createOrAmendCodingOutDesJson: JsValue = Json.parse(s"""
      |{
      |  "taxCodeComponents": $taxCodeComponentsDesJson
      |}
      |""".stripMargin)

}
