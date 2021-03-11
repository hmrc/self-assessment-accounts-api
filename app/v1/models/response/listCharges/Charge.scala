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

package v1.models.response.listCharges

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v1.models.domain.DesTaxYear

case class Charge(taxYear: String,
                  transactionId: String,
                  transactionDate: String,
                  `type`: Option[String],
                  totalAmount: BigDecimal,
                  outstandingAmount: BigDecimal)

object Charge {
  implicit val reads: Reads[Charge] = (
    (JsPath \ "taxYear").read[String].map(taxYear => DesTaxYear.fromDesIntToString(Integer.parseInt(taxYear))) and
      (JsPath \ "documentId").read[String] and
      (JsPath \ "documentDate").read[String] and
      (JsPath \ "documentDescription").readNullable[String] and
      (JsPath \ "totalAmount").read[BigDecimal] and
      (JsPath \ "documentOutstandingAmount").read[BigDecimal]
    ) (Charge.apply _)

  implicit val writes: OWrites[Charge] = Json.writes[Charge]

}
