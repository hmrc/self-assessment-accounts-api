/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v1.models.domain.DownstreamSource
import play.api.libs.functional.syntax._

case class TaxCodeComponents(amount: BigDecimal, relatedTaxYear: Option[String], submittedOn: String, source: String, id: Option[BigInt])

object TaxCodeComponents {

  implicit val reads: Reads[TaxCodeComponents] = (
    (JsPath \ "amount").read[BigDecimal] and
      (JsPath \ "relatedTaxYear").readNullable[String] and
      (JsPath \ "submittedOn").read[String] and
      (JsPath \ "source").read[DownstreamSource].map(_.toMtdSource) and
      (JsPath \ "componentIdentifier").readNullable[BigInt]
  )(TaxCodeComponents.apply _)

  implicit val writes: OWrites[TaxCodeComponents] = Json.writes[TaxCodeComponents]
}
