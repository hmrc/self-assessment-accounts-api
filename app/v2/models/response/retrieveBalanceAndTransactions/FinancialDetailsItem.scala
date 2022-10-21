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

package v2.models.response.retrieveBalanceAndTransactions

import play.api.libs.functional.syntax._
import play.api.libs.json.{Json, Reads, Writes, __}

case class FinancialDetailsItem(itemId: Option[String],
                                dueDate: Option[String],
                                amount: Option[BigDecimal],
                                clearingDate: Option[String],
                                clearingReason: Option[String],
                                outgoingPaymentMethod: Option[String],
                                isChargeOnHold: Boolean,
                                isEstimatedChargeOnHold: Boolean,
                                isInterestAccrualOnHold: Boolean,
                                isInterestChargeOnHold: Boolean,
                                isReturn: Option[Boolean],
                                paymentReference: Option[String],
                                paymentAmount: Option[BigDecimal],
                                paymentMethod: Option[String],
                                paymentLot: Option[String],
                                paymentLotItem: Option[String],
                                clearingSAPDocument: Option[String],
                                isChargeEstimate: Boolean)

object FinancialDetailsItem {
  implicit val writes: Writes[FinancialDetailsItem] = Json.writes

  implicit val reads: Reads[FinancialDetailsItem] = {
    val clearingReasonConverter: Option[String] => Option[String] =
      convertToMtd(
        Map(
          "01" -> "Incoming Payment",
          "02" -> "Outgoing Payment",
          "05" -> "Reversal",
          "06" -> "Manual Clearing",
          "08" -> "Automatic Clearing"
        ))

    val paymentMethodConverter: Option[String] => Option[String] =
      convertToMtd(Map("A" -> "Repayment to Card", "P" -> "Payable Order Repayment", "R" -> "BACS Payment out"))

    val stringToBooleanConverter: Option[String] => Boolean = _.exists(_.nonEmpty)

    ((__ \ "subItem").readNullable[String] and
      (__ \ "dueDate").readNullable[String] and
      (__ \ "amount").readNullable[BigDecimal] and
      (__ \ "clearingDate").readNullable[String] and
      (__ \ "clearingReason").readNullable[String].map(clearingReasonConverter) and
      (__ \ "outgoingPaymentMethod").readNullable[String].map(paymentMethodConverter) and
      (__ \ "paymentLock").readNullable[String].map(stringToBooleanConverter) and
      (__ \ "clearingLock").readNullable[String].map(stringToBooleanConverter) and
      (__ \ "interestLock").readNullable[String].map(stringToBooleanConverter) and
      (__ \ "dunningLock").readNullable[String].map(stringToBooleanConverter) and
      (__ \ "returnFlag").readNullable[Boolean] and
      (__ \ "paymentReference").readNullable[String] and
      (__ \ "paymentAmount").readNullable[BigDecimal] and
      (__ \ "paymentMethod").readNullable[String] and
      (__ \ "paymentLot").readNullable[String] and
      (__ \ "paymentLotItem").readNullable[String] and
      (__ \ "clearingSAPDocument").readNullable[String] and
      (__ \ "statisticalDocument").readNullable[String].flatMap[Boolean] {
        case Some(x) if x == "G" => Reads.pure(true)
        case Some(x) if x == ""  => Reads.pure(false)
        case Some(x)             => Reads.failed(s"expected '' or 'G' but was `$x`")
        case None                => Reads.pure(false)
      })(FinancialDetailsItem.apply _)
  }

  private def convertToMtd(mapping: Map[String, String])(maybeDownstream: Option[String]): Option[String] =
    maybeDownstream.flatMap(downstream => mapping.get(downstream))

}
