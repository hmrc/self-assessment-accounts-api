/*
 * Copyright 2024 HM Revenue & Customs
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

package v3.retrieveCodingOut.def1.model.reponse

import play.api.libs.json.Json
import support.UnitSpec
import v3.retrieveCodingOut.def1.model.response.TaxCodeComponents

class TaxCodeComponentsSpec extends UnitSpec {

  private val taxCodeComponents = TaxCodeComponents(
    amount = 1, relatedTaxYear = Some("year"), submittedOn = "date", source = "hmrcHeld", id = Some(3)
  )

  private val mtdJson = Json.parse(s"""
                                     | {
                                     |   "amount": "1",
                                     |   "relatedTaxYear": "year",
                                     |   "submittedOn": "date",
                                     |   "source": "HMRC HELD",
                                     |   "componentIdentifier": "3"
                                     | }
                                     |""".stripMargin)

  private val downstreamJson = Json.parse(s"""
                                     | {
                                     |   "amount": 1,
                                     |   "relatedTaxYear": "year",
                                     |   "submittedOn": "date",
                                     |   "source": "hmrcHeld",
                                     |   "id": 3
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

