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

package v3.fixtures.retrieveAutocodingStatus

import api.models.domain.TaxYear
import play.api.libs.json.{JsObject, Json}

object ResponseFixture {

  def mtdResponse(nino: String, taxYear: String): JsObject = Json
    .parse(
      s"""
       |{
       |  "processingDate": "2023-12-17T09:30:47Z",
       |  "nino": "$nino",
       |  "taxYear": "$taxYear",
       |  "optOutIndicator": true
       |}
     """.stripMargin
    )
    .as[JsObject]

  def downstreamResponse(nino: String, taxYear: String): JsObject = Json
    .parse(
      s"""
         |{
         |  "processingDate": "2023-12-17T09:30:47Z",
         |  "nino": "$nino",
         |  "taxYear": ${TaxYear.fromMtd(taxYear).year},
         |  "optOutIndicator": true
         |}
     """.stripMargin
    )
    .as[JsObject]

}
