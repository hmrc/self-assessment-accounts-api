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

  implicit val reads: Reads[LatePaymentPenaltyDetail] = for {
    principalChargeReference                       <- (JsPath \ "principalChargeReference").read[String]
    penaltyCategory                                <- (JsPath \ "penaltyCategory").readNullable[PaymentPenaltyCategory]
    penaltyStatus                                  <- (JsPath \ "penaltyStatus").read[PaymentPenaltyStatus]
    penaltyAmountAccruing                          <- (JsPath \ "penaltyAmountAccruing").read[BigDecimal]
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
    principalChargeBillingFrom                     <- (JsPath \ "principalChargeBillingFrom").read[String]
    principalChargeBillingTo                       <- (JsPath \ "principalChargeBillingTo").read[String]
    principalChargeDueDate                         <- (JsPath \ "principalChargeDueDate").read[String]
    principalChargeLatestClearing                  <- (JsPath \ "principalChargeLatestClearing").readNullable[String]
    timeToPay                                      <- (JsPath \ "timeToPay").readNullable[Seq[TimeToPay]]
  } yield {
    LatePaymentPenaltyDetail(
      principalChargeReference = principalChargeReference,
      penaltyCategory = penaltyCategory,
      penaltyStatus = penaltyStatus,
      penaltyAmountAccruing = penaltyAmountAccruing,
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
      principalChargeBillingFrom = principalChargeBillingFrom,
      principalChargeBillingTo = principalChargeBillingTo,
      principalChargeDueDate = principalChargeDueDate,
      principalChargeLatestClearing = principalChargeLatestClearing,
      timeToPay = timeToPay
    )
  }

  implicit val writes: OWrites[LatePaymentPenaltyDetail] = Json.writes[LatePaymentPenaltyDetail]
}
