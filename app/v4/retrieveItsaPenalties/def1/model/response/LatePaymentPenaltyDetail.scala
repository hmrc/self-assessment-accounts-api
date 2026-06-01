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

import play.api.libs.json.*

case class LatePaymentPenaltyDetail(
    principalChargeReference: String,
    penaltyCategory: Option[String],
    penaltyStatus: String,
    penaltyAmountAccruing: BigDecimal,
    penaltyAmountPosted: BigDecimal,
    penaltyAmountPaid: Option[BigDecimal],
    penaltyAmountOutstanding: Option[BigDecimal],
    latePaymentPenalty1LowerRateCalculationAmount: Option[BigDecimal],
    latePaymentPenalty1LowerRatePercentage: Option[BigDecimal],
    latePaymentPenalty1HigherRateCalculationAmount: Option[BigDecimal],
    latePaymentPenalty1HigherRatePercentage: Option[BigDecimal],
    latePaymentPenalty2Days: Option[Int],
    latePaymentPenalty2Percentage: Option[BigDecimal],
    penaltyChargeCreationDate: Option[String],
    communicationsDate: Option[String],
    penaltyChargeReference: Option[String],
    penaltyChargeDueDate: Option[String],
    appealInformation: Option[Seq[AppealInformation]],
    principalChargeDocNumber: Option[String],
    principalChargeBillingFrom: String,
    principalChargeBillingTo: String,
    principalChargeDueDate: String,
    principalChargeLatestClearing: Option[String],
    timeToPay: Option[Seq[TimeToPay]]
)

object LatePaymentPenaltyDetail {

  private val penaltyCategoryMap: Map[String, String] = Map(
    "LPP1" -> "lpp1",
    "LPP2" -> "lpp2"
  )

  private val penaltyStatusMap: Map[String, String] = Map(
    "A" -> "accruing",
    "P" -> "posted"
  )

  private val transformReads: Reads[JsObject] =
    __.json
      .update(
        (__ \ "penaltyCategory").json.copyFrom(
          (__ \ "penaltyCategory")
            .readNullable[String]
            .map {
              case Some(v) => JsString(penaltyCategoryMap.getOrElse(v, v))
              case None    => JsNull
            }
        )
      )
      .andThen(
        __.json.update(
          (__ \ "penaltyStatus").json.copyFrom(
            (__ \ "penaltyStatus")
              .read[String]
              .map(v => JsString(penaltyStatusMap.getOrElse(v, v)))
          )
        )
      )
      .andThen(
        __.json.update(
          (__ \ "latePaymentPenalty1LowerRateCalculationAmount").json.copyFrom((__ \ "lpp1LRCalculationAmt").json.pick)
        )
      )
      .andThen(
        __.json.update(
          (__ \ "latePaymentPenalty1LowerRatePercentage").json.copyFrom((__ \ "lpp1LRPercentage").json.pick)
        )
      )
      .andThen(
        __.json.update(
          (__ \ "latePaymentPenalty1HigherRateCalculationAmount").json.copyFrom((__ \ "lpp1HRCalculationAmt").json.pick)
        )
      )
      .andThen(
        __.json.update(
          (__ \ "latePaymentPenalty1HigherRatePercentage").json.copyFrom((__ \ "lpp1HRPercentage").json.pick)
        )
      )
      .andThen(
        __.json.update(
          (__ \ "latePaymentPenalty2Days").json.copyFrom(
            (__ \ "lpp2Days").read[String].map(v => JsNumber(v.toInt))
          )
        )
      )
      .andThen(
        __.json.update(
          (__ \ "latePaymentPenalty2Percentage").json.copyFrom((__ \ "lpp2Percentage").json.pick)
        )
      )

  implicit val reads: Reads[LatePaymentPenaltyDetail] =
    transformReads.andThen(Json.reads[LatePaymentPenaltyDetail])

  implicit val writes: OWrites[LatePaymentPenaltyDetail] =
    Json.writes[LatePaymentPenaltyDetail]

}
