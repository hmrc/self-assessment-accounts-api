/*
 * Copyright 2026 HM Revenue & Customs
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

package v4.retrieveItsaPenalties.def1.model.response

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

case class LateSubmissionPenaltyDetail(
    penaltyNumber: String,
    penaltyOrder: Option[String],
    penaltyCategory: Option[PenaltyCategory],
    penaltyStatus: Option[String],
    frequencyAdjustmentPointIndicator: Option[Boolean],
    penaltyCreationDate: String,
    penaltyExpiryDate: String,
    expiryReason: Option[ExpiryReason],
    communicationsDate: Option[String],
    lateSubmissions: Option[Seq[LateSubmissions]],
    appealInformation: Option[Seq[AppealInformation]],
    chargeReference: Option[String],
    chargeAmount: Option[BigDecimal],
    chargeOutstandingAmount: Option[BigDecimal],
    chargeDueDate: Option[String]
)

object LateSubmissionPenaltyDetail {

  implicit val reads: Reads[LateSubmissionPenaltyDetail] = (
    (JsPath \ "penaltyNumber").read[String] and
      (JsPath \ "penaltyOrder").readNullable[String] and
      (JsPath \ "penaltyCategory").readNullable[PenaltyCategory] and
      (JsPath \ "penaltyStatus")
        .readNullable[String]
        .map {
          case Some("ACTIVE")   => Some("active")
          case Some("INACTIVE") => Some("inactive")
          case other            => other.map(_.toLowerCase)
        } and
      (JsPath \ "fapIndicator")
        .readNullable[String]
        .map(_.map(_ == "X")) and
      (JsPath \ "penaltyCreationDate").read[String] and
      (JsPath \ "penaltyExpiryDate").read[String] and
      (JsPath \ "expiryReason").readNullable[ExpiryReason] and
      (JsPath \ "communicationsDate").readNullable[String] and
      (JsPath \ "lateSubmissions").readNullable[Seq[LateSubmissions]] and
      (JsPath \ "appealInformation").readNullable[Seq[AppealInformation]] and
      (JsPath \ "chargeReference").readNullable[String] and
      (JsPath \ "chargeAmount").readNullable[BigDecimal] and
      (JsPath \ "chargeOutstandingAmount").readNullable[BigDecimal] and
      (JsPath \ "chargeDueDate").readNullable[String]
  )(LateSubmissionPenaltyDetail.apply)

  implicit val writes: OWrites[LateSubmissionPenaltyDetail] = Json.writes[LateSubmissionPenaltyDetail]
}
