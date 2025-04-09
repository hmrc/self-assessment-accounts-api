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

package v4.retrieveBalanceAndTransactions.def1.model.response

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class FinancialDetailsItem(itemId: Option[String],
                                dueDate: Option[String],
                                amount: Option[BigDecimal],
                                clearingDate: Option[String],
                                clearingReason: Option[String],
                                outgoingPaymentMethod: Option[String],
                                locks: Option[FinancialDetailsItemLocks],
                                isReturn: Option[Boolean],
                                paymentReference: Option[String],
                                paymentAmount: Option[BigDecimal],
                                paymentMethod: Option[String],
                                paymentLot: Option[String],
                                paymentLotItem: Option[String],
                                clearingSAPDocument: Option[String],
                                isChargeEstimate: Option[Boolean])

object FinancialDetailsItem {
  implicit val writes: Writes[FinancialDetailsItem] = Json.writes

  case class ReadLocks(value: Boolean)

  implicit def reads(implicit readLocks: ReadLocks): Reads[FinancialDetailsItem] = {

    val paymentMethodConverter: Option[String] => Option[String] =
      convertToMtd(Map("A" -> "Repayment to Card", "P" -> "Payable Order Repayment", "R" -> "BACS Payment out"))

    ((__ \ "subItem").readNullable[String] and
      (__ \ "dueDate").readNullable[String] and
      (__ \ "amount").readNullable[BigDecimal] and
      (__ \ "clearingDate").readNullable[String] and
      (__ \ "clearingReason").readNullable[String] and
      (__ \ "outgoingPaymentMethod").readNullable[String].map(paymentMethodConverter) and
      (if (readLocks.value) __.read[FinancialDetailsItemLocks].map(Some(_)) else Reads.pure(None)) and
      (__ \ "returnFlag").readNullable[Boolean]
        .orElse((__ \ "returnFlag").readNullable[String].flatMap[Option[Boolean]] {
          case Some(x) if x == "Y" => Reads.pure(Some(true))
          case Some(x) if x == "N" => Reads.pure(Some(false))
          case Some(x)             => Reads.failed(s"expected 'Y' or 'N' but was `$x`")
          case None                => Reads.pure(None)
        }) and
      (__ \ "paymentReference").readNullable[String] and
      (__ \ "paymentAmount").readNullable[BigDecimal] and
      (__ \ "paymentMethod").readNullable[String] and
      (__ \ "paymentLot").readNullable[String] and
      (__ \ "paymentLotItem").readNullable[String] and
      (__ \ "clearingSAPDocument").readNullable[String] and
      (__ \ "statisticalDocument").readNullable[String].flatMap[Option[Boolean]] {
        case Some(x) if x == "Y" || x == "G" => Reads.pure(Some(true))
        case Some(x) if x == "N"             => Reads.pure(Some(false))
        case Some(x)                         => Reads.failed(s"expected 'Y' or 'N' but was `$x`")
        case None                            => Reads.pure(None)
      })(FinancialDetailsItem.apply _)
  }

  private def convertToMtd(mapping: Map[String, String])(maybeDownstream: Option[String]): Option[String] =
    maybeDownstream.flatMap(downstream => mapping.get(downstream))

}
