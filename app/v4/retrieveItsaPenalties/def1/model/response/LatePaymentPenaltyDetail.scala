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

private case class DownstreamLatePaymentPenaltyDetail(
    principalChargeReference: String,
    penaltyCategory: Option[String],
    penaltyStatus: String,
    penaltyAmountAccruing: BigDecimal,
    penaltyAmountPosted: BigDecimal,
    penaltyAmountPaid: Option[BigDecimal],
    penaltyAmountOutstanding: Option[BigDecimal],
    lpp1LRCalculationAmt: Option[BigDecimal],
    lpp1LRPercentage: Option[BigDecimal],
    lpp1HRCalculationAmt: Option[BigDecimal],
    lpp1HRPercentage: Option[BigDecimal],
    lpp2Days: Option[String],
    lpp2Percentage: Option[BigDecimal],
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

private object DownstreamLatePaymentPenaltyDetail {

  implicit val reads: Reads[DownstreamLatePaymentPenaltyDetail] =
    Json.reads[DownstreamLatePaymentPenaltyDetail]

}

private def toDomain(downstream: DownstreamLatePaymentPenaltyDetail): LatePaymentPenaltyDetail =
  LatePaymentPenaltyDetail(
    principalChargeReference = downstream.principalChargeReference,
    penaltyCategory = downstream.penaltyCategory.map {
      case "LPP1" => "lpp1"
      case "LPP2" => "lpp2"
      case other  => other.toLowerCase
    },
    penaltyStatus = downstream.penaltyStatus match {
      case "A"   => "accruing"
      case "P"   => "posted"
      case other => other.toLowerCase
    },
    penaltyAmountAccruing = downstream.penaltyAmountAccruing,
    penaltyAmountPosted = downstream.penaltyAmountPosted,
    penaltyAmountPaid = downstream.penaltyAmountPaid,
    penaltyAmountOutstanding = downstream.penaltyAmountOutstanding,
    latePaymentPenalty1LowerRateCalculationAmount = downstream.lpp1LRCalculationAmt,
    latePaymentPenalty1LowerRatePercentage = downstream.lpp1LRPercentage,
    latePaymentPenalty1HigherRateCalculationAmount = downstream.lpp1HRCalculationAmt,
    latePaymentPenalty1HigherRatePercentage = downstream.lpp1HRPercentage,
    latePaymentPenalty2Days = downstream.lpp2Days.map(_.toInt),
    latePaymentPenalty2Percentage = downstream.lpp2Percentage,
    penaltyChargeCreationDate = downstream.penaltyChargeCreationDate,
    communicationsDate = downstream.communicationsDate,
    penaltyChargeReference = downstream.penaltyChargeReference,
    penaltyChargeDueDate = downstream.penaltyChargeDueDate,
    appealInformation = downstream.appealInformation,
    principalChargeDocNumber = downstream.principalChargeDocNumber,
    principalChargeBillingFrom = downstream.principalChargeBillingFrom,
    principalChargeBillingTo = downstream.principalChargeBillingTo,
    principalChargeDueDate = downstream.principalChargeDueDate,
    principalChargeLatestClearing = downstream.principalChargeLatestClearing,
    timeToPay = downstream.timeToPay
  )

object LatePaymentPenaltyDetail {

  implicit val reads: Reads[LatePaymentPenaltyDetail] =
    DownstreamLatePaymentPenaltyDetail.reads.map(toDomain)

  implicit val writes: OWrites[LatePaymentPenaltyDetail] =
    Json.writes[LatePaymentPenaltyDetail]

}
