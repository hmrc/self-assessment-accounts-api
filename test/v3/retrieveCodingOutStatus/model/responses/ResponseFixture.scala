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

package v3.retrieveCodingOutStatus.model.responses

import api.models.domain.TaxYear
import play.api.libs.json.{JsObject, Json}
import v3.retrieveCodingOutStatus.def1.model.response.Def1_RetrieveCodingOutStatusResponse

object ResponseFixture {

  val mtdResponseJson: JsObject = Json
    .parse(
      s"""
       |{
       |  "processingDate": "2023-12-17T09:30:47Z",
       |  "nino": "AB123456A",
       |  "taxYear": "2023-24",
       |  "optOutIndicator": true
       |}
     """.stripMargin
    )
    .as[JsObject]

  val downstreamResponseJson: JsObject = Json
    .parse(
      s"""
         |{
         |  "processingDate": "2023-12-17T09:30:47Z",
         |  "nino": "AB123456A",
         |  "taxYear": "2024",
         |  "optOutIndicator": true
         |}
     """.stripMargin
    )
    .as[JsObject]

  val downstreamOptOutOfCodingOutResponseJson: JsObject = Json
    .parse(
      s"""
         |{
         |  "processingDate": "2020-12-17T09:30:47Z"
         |}
     """.stripMargin
    )
    .as[JsObject]

  val def1ResponseModel: Def1_RetrieveCodingOutStatusResponse =
    Def1_RetrieveCodingOutStatusResponse("2023-12-17T09:30:47Z", "AB123456A", TaxYear("2024"), true)

}
