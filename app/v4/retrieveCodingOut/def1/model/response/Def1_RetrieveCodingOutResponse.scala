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

package v4.retrieveCodingOut.def1.model.response

import play.api.libs.json.{Json, Reads, Writes}
import v4.retrieveCodingOut.model.response.RetrieveCodingOutResponse

case class Def1_RetrieveCodingOutResponse(taxCodeComponents: Option[TaxCodeComponentsObject],
                                          unmatchedCustomerSubmissions: Option[UnmatchedCustomerSubmissionsObject])
    extends RetrieveCodingOutResponse

object Def1_RetrieveCodingOutResponse {

  implicit val writes: Writes[Def1_RetrieveCodingOutResponse] = Json.writes[Def1_RetrieveCodingOutResponse]

  implicit val reads: Reads[Def1_RetrieveCodingOutResponse] = Json.reads[Def1_RetrieveCodingOutResponse]

}
