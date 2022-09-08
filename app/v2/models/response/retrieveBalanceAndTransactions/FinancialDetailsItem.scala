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

case class FinancialDetailsItem(subItem: Option[String],
                                dueDate: Option[String],
                                amount: Option[BigDecimal],
                                clearingDate: Option[String],
                                clearingReason: Option[String],
                                outgoingPaymentMethod: Option[String],
                                paymentLock: Option[String],
                                clearingLock: Option[String],
                                interestLock: Option[String],
                                dunningLock: Option[String],
                                isReturn: Option[Boolean],
                                paymentReference: Option[String],
                                paymentAmount: Option[BigDecimal],
                                paymentMethod: Option[String],
                                paymentLot: Option[String],
                                paymentLotItem: Option[String],
                                isStatistical: Option[Boolean],
                                returnReason: Option[String])

object FinancialDetailsItem {
  implicit val writes: Writes[FinancialDetailsItem] = Json.writes

  implicit val reads: Reads[FinancialDetailsItem] = {
    val paymentMethodConverter: Option[String] => Option[String] =
      convertToMtd(Map("A" -> "Repayment to Card", "P" -> "Payable Order Repayment", "R" -> "BACS Payment out"))

    val paymentLockConverter: Option[String] => Option[String] = convertToMtd(Map("K" -> "Additional Security Checks"))

    val clearingLockConverter: Option[String] => Option[String] = convertToMtd(Map("0" -> "No Reallocation"))

    ((__ \ "subItem").readNullable[String] and
      (__ \ "dueDate").readNullable[String] and
      (__ \ "amount").readNullable[BigDecimal] and
      (__ \ "clearingDate").readNullable[String] and
      (__ \ "clearingReason").readNullable[String] and
      (__ \ "outgoingPaymentMethod").readNullable[String].map(paymentMethodConverter) and
      (__ \ "paymentLock").readNullable[String].map(paymentLockConverter) and
      (__ \ "clearingLock").readNullable[String].map(clearingLockConverter) and
      (__ \ "interestLock").readNullable[String] and
      (__ \ "dunningLock").readNullable[String] and
      (__ \ "returnFlag").readNullable[Boolean] and
      (__ \ "paymentReference").readNullable[String] and
      (__ \ "paymentAmount").readNullable[BigDecimal] and
      (__ \ "paymentMethod").readNullable[String] and
      (__ \ "paymentLot").readNullable[String] and
      (__ \ "paymentLotItem").readNullable[String] and
      (__ \ "statisticalDocument").readNullable[String].map(_.map(_ == "G")) and
      (__ \ "returnReason").readNullable[String])(FinancialDetailsItem.apply _)
  }

  private def convertToMtd(mapping: Map[String, String])(maybeDownstream: Option[String]): Option[String] =
    maybeDownstream.flatMap(downstream => mapping.get(downstream))

}
