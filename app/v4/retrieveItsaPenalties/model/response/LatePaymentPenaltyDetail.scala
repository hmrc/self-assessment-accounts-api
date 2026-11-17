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

package v4.retrieveItsaPenalties.model.response

import play.api.libs.json.*

case class LatePaymentPenaltyDetail(
    principalChargeReference: String,
    penaltyCategory: Option[PaymentPenaltyCategory],
    penaltyStatus: PaymentPenaltyStatus,
    penaltyAmountAccruing: BigDecimal,
    accrualsPaused: Option[Boolean],
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
    chargeClassification: Option[ChargeClassification],
    principalChargeBillingFrom: String,
    principalChargeBillingTo: String,
    principalChargeDueDate: String,
    parentChargeFormalStandover: Option[Boolean],
    parentChargeInformalStandover: Option[Boolean],
    penaltyChargeInformalStandover: Option[Boolean],
    principalChargeLatestClearing: Option[String],
    timeToPay: Option[Seq[TimeToPay]],
    timeToPayForPenaltyCharges: Option[TimeToPayForPenaltyCharges],
    timeToPayForParentCharges: Option[TimeToPayForParentCharges]
)

object LatePaymentPenaltyDetail {

  implicit val reads: Reads[LatePaymentPenaltyDetail] = for {
    principalChargeReference                       <- (JsPath \ "principalChargeReference").read[String]
    penaltyCategory                                <- (JsPath \ "penaltyCategory").readNullable[PaymentPenaltyCategory]
    penaltyStatus                                  <- (JsPath \ "penaltyStatus").read[PaymentPenaltyStatus]
    penaltyAmountAccruing                          <- (JsPath \ "penaltyAmountAccruing").read[BigDecimal]
    accrualsPaused                                 <- (JsPath \ "accrualsPaused").readNullable[Boolean]
    penaltyAmountPosted                            <- (JsPath \ "penaltyAmountPosted").read[BigDecimal]
    penaltyAmountPaid                              <- (JsPath \ "penaltyAmountPaid").readNullable[BigDecimal]
    penaltyAmountOutstanding                       <- (JsPath \ "penaltyAmountOutstanding").readNullable[BigDecimal]
    latePaymentPenalty1LowerRateCalculationAmount  <- (JsPath \ "lpp1LRCalculationAmt").readNullable[BigDecimal]
    latePaymentPenalty1LowerRatePercentage         <- (JsPath \ "lpp1LRPercentage").readNullable[BigDecimal]
    latePaymentPenalty1HigherRateCalculationAmount <- (JsPath \ "lpp1HRCalculationAmt").readNullable[BigDecimal]
    latePaymentPenalty1HigherRatePercentage        <- (JsPath \ "lpp1HRPercentage").readNullable[BigDecimal]
    latePaymentPenalty2Days                        <- (JsPath \ "lpp2Days").readNullable(Reads.StringReads.map(_.toInt))
    latePaymentPenalty2Percentage                  <- (JsPath \ "lpp2Percentage").readNullable[BigDecimal]
    penaltyChargeCreationDate                      <- (JsPath \ "penaltyChargeCreationDate").readNullable[String]
    communicationsDate                             <- (JsPath \ "communicationsDate").readNullable[String]
    penaltyChargeReference                         <- (JsPath \ "penaltyChargeReference").readNullable[String]
    penaltyChargeDueDate                           <- (JsPath \ "penaltyChargeDueDate").readNullable[String]
    appealInformation                              <- (JsPath \ "appealInformation").readNullable[Seq[AppealInformation]]
    principalChargeDocNumber                       <- (JsPath \ "principalChargeDocNumber").readNullable[String]
    chargeClassification                           <- (JsPath \ "chargeClassificationValue").readNullable[ChargeClassification]
    principalChargeBillingFrom                     <- (JsPath \ "principalChargeBillingFrom").read[String]
    principalChargeBillingTo                       <- (JsPath \ "principalChargeBillingTo").read[String]
    principalChargeDueDate                         <- (JsPath \ "principalChargeDueDate").read[String]
    parentChargeFormalStandover                    <- (JsPath \ "parentFormalStandover").readNullable[Boolean]
    parentChargeInformalStandover                  <- (JsPath \ "parentInformalStandover").readNullable[Boolean]
    penaltyChargeInformalStandover                 <- (JsPath \ "penaltyInformalStandover").readNullable[Boolean]
    principalChargeLatestClearing                  <- (JsPath \ "principalChargeLatestClearing").readNullable[String]
    timeToPay                                      <- (JsPath \ "timeToPay").readNullable[Seq[TimeToPay]]
    timeToPayForPenaltyChargesIndicator            <- (JsPath \ "ttpForPenalty").readNullable[Boolean]
    timeToPayForPenaltyStartDate                   <- (JsPath \ "ttpStartDateForPenalty").readNullable[String]
    timeToPayForPenaltyEndDate                     <- (JsPath \ "ttpEndDateForPenalty").readNullable[String]
    timeToPayForParentChargesIndicator             <- (JsPath \ "ttpForParent").readNullable[Boolean]
    timeToPayForParentStartDate                    <- (JsPath \ "ttpStartDateForParent").readNullable[String]
    timeToPayForParentEndDate                      <- (JsPath \ "ttpEndDateForParent").readNullable[String]
  } yield {
    LatePaymentPenaltyDetail(
      principalChargeReference = principalChargeReference,
      penaltyCategory = penaltyCategory,
      penaltyStatus = penaltyStatus,
      penaltyAmountAccruing = penaltyAmountAccruing,
      accrualsPaused = accrualsPaused,
      penaltyAmountPosted = penaltyAmountPosted,
      penaltyAmountPaid = penaltyAmountPaid,
      penaltyAmountOutstanding = penaltyAmountOutstanding,
      latePaymentPenalty1LowerRateCalculationAmount = latePaymentPenalty1LowerRateCalculationAmount,
      latePaymentPenalty1LowerRatePercentage = latePaymentPenalty1LowerRatePercentage,
      latePaymentPenalty1HigherRateCalculationAmount = latePaymentPenalty1HigherRateCalculationAmount,
      latePaymentPenalty1HigherRatePercentage = latePaymentPenalty1HigherRatePercentage,
      latePaymentPenalty2Days = latePaymentPenalty2Days,
      latePaymentPenalty2Percentage = latePaymentPenalty2Percentage,
      penaltyChargeCreationDate = penaltyChargeCreationDate,
      communicationsDate = communicationsDate,
      penaltyChargeReference = penaltyChargeReference,
      penaltyChargeDueDate = penaltyChargeDueDate,
      appealInformation = appealInformation,
      principalChargeDocNumber = principalChargeDocNumber,
      chargeClassification = chargeClassification,
      principalChargeBillingFrom = principalChargeBillingFrom,
      principalChargeBillingTo = principalChargeBillingTo,
      principalChargeDueDate = principalChargeDueDate,
      parentChargeFormalStandover = parentChargeFormalStandover,
      parentChargeInformalStandover = parentChargeInformalStandover,
      penaltyChargeInformalStandover = penaltyChargeInformalStandover,
      principalChargeLatestClearing = principalChargeLatestClearing,
      timeToPay = timeToPay,
      timeToPayForPenaltyCharges = (timeToPayForPenaltyChargesIndicator, timeToPayForPenaltyStartDate, timeToPayForPenaltyEndDate) match {
        case (None, None, None) => None
        case _ => Some(TimeToPayForPenaltyCharges(timeToPayForPenaltyChargesIndicator, timeToPayForPenaltyStartDate, timeToPayForPenaltyEndDate))
      },
      timeToPayForParentCharges = (timeToPayForParentChargesIndicator, timeToPayForParentStartDate, timeToPayForParentEndDate) match {
        case (None, None, None) => None
        case _ => Some(TimeToPayForParentCharges(timeToPayForParentChargesIndicator, timeToPayForParentStartDate, timeToPayForParentEndDate))
      }
    )
  }

  implicit val writes: OWrites[LatePaymentPenaltyDetail] = Json.writes[LatePaymentPenaltyDetail]
}
