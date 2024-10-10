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

package v3.createOrAmendCodingOut.def1.models.request

import play.api.libs.json.Json
import support.UnitSpec
import v3.createOrAmendCodingOut.def1.model.request.{TaxCodeComponent, TaxCodeComponents}

class TaxCodeComponentsSpec extends UnitSpec {

  private val taxCodeComponents = TaxCodeComponents(
    payeUnderpayment = Some(List(TaxCodeComponent(id = 1, amount = 1.1))),
    selfAssessmentUnderpayment = Some(List(TaxCodeComponent(id = 2, amount = 2.2))),
    debt = None,
    inYearAdjustment = None
  )

  private val mtdJson = Json.parse("""
      | {
      |   "payeUnderpayment": [{"id":"1", "amount":1.1}],
      |   "selfAssessmentUnderpayment": [{"id":"2", "amount":2.2}]
      | }
      |""".stripMargin)

  private val downstreamJson = Json.parse("""
      | {
      |   "payeUnderpayment": [{"componentIdentifier":"1", "amount":1.1}],
      |   "selfAssessmentUnderpayment": [{"componentIdentifier":"2", "amount":2.2}]
      | }
      |""".stripMargin)

  "TaxCodeComponents" should {

    "read MTD json to the expected object" in {
      val result = mtdJson.as[TaxCodeComponents]
      result shouldBe taxCodeComponents
    }

    "write to the expected downstream JSON output" in {
      val result = Json.toJson(taxCodeComponents)
      result shouldBe downstreamJson
    }

  }

}
