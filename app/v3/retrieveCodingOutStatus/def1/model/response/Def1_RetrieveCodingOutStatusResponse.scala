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

package v3.retrieveCodingOutStatus.def1.model.response

import api.models.domain.TaxYear
import play.api.libs.json._
import v3.retrieveCodingOutStatus.model.response.RetrieveCodingOutStatusResponse

case class Def1_RetrieveCodingOutStatusResponse(processingDate: String,
                                                nino: String,
                                                taxYear: TaxYear,
                                                optOutIndicator: Boolean) extends RetrieveCodingOutStatusResponse

object Def1_RetrieveCodingOutStatusResponse {

  private implicit val downstreamIntToMtdFormat: Format[TaxYear] = Format(
    implicitly[Reads[String]].map(TaxYear.fromDownstream),
    TaxYear.toMtdWrites
  )

  implicit val reads: Reads[Def1_RetrieveCodingOutStatusResponse] = Json.reads[Def1_RetrieveCodingOutStatusResponse]

  implicit val writes: OWrites[Def1_RetrieveCodingOutStatusResponse] = Json.writes[Def1_RetrieveCodingOutStatusResponse]

}
