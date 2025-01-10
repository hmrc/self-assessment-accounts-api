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

package v3.retrieveChargeHistoryByChargeReference.def1.model.response

import play.api.libs.functional.syntax._
import play.api.libs.json._
import shared.models.domain.TaxYear

case class ChargeHistoryDetail(taxYear: Option[String],
                               transactionId: String,
                               transactionDate: String,
                               description: String,
                               totalAmount: BigDecimal,
                               changeDate: String,
                               changeReason: String,
                               poaAdjustmentReason: Option[String])

object ChargeHistoryDetail {

  implicit val reads: Reads[ChargeHistoryDetail] =
    ((JsPath \ "taxYear").readNullable[String].map(_.map(TaxYear.fromDownstream(_).asMtd)) and
      (JsPath \ "documentId").read[String] and
      (JsPath \ "documentDate").read[String] and
      (JsPath \ "documentDescription").read[String] and
      (JsPath \ "totalAmount").read[BigDecimal] and
      (JsPath \ "reversalDate").read[String] and
      (JsPath \ "reversalReason").read[String] and
      (JsPath \ "poaAdjustmentReason").readNullable[String])(ChargeHistoryDetail.apply _)

  implicit val writes: OWrites[ChargeHistoryDetail] = Json.writes[ChargeHistoryDetail]
}
