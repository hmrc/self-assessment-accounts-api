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

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, Reads, Writes}
import v1.models.domain.DownstreamSource

case class RetrieveCodingOutResponse(source: String,
                                     selfAssessmentUnderPayments: Option[Seq[ResponseItem]],
                                     payeUnderpayments: Option[Seq[ResponseItem]],
                                     debts: Option[Seq[ResponseItem]],
                                     inYearAdjustments: Option[ResponseItem])

object RetrieveCodingOutResponse {

  implicit val reads: Reads[RetrieveCodingOutResponse] = (
      (JsPath \ "source" ).read[DownstreamSource].map(_.toMtdSource) and
      (JsPath \ "taxCodeComponents" \ "selfAssessmentUnderPayments").readNullable[Seq[ResponseItem]] and
      (JsPath \ "taxCodeComponents" \ "payeUnderpayments").readNullable[Seq[ResponseItem]] and
      (JsPath \ "taxCodeComponents" \ "debts").readNullable[Seq[ResponseItem]] and
      (JsPath \ "taxCodeComponents" \ "inYearAdjustments").readNullable[ResponseItem]
  )(RetrieveCodingOutResponse.apply _)



  implicit val writes: Writes[RetrieveCodingOutResponse] = Json.writes[RetrieveCodingOutResponse]
}
