/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.models.response.retrieveTransactionDetails

import play.api.libs.json.{JsPath, Json, OWrites, Reads}


case class SubItem(subItemId: Option[String],
                   amount: Option[BigDecimal],
                   clearingDate: Option[String],
                   clearingReason: Option[String],
                   outgoingPaymentMethod: Option[String],
                   paymentAmount: Option[BigDecimal],
                   paymentMethod: Option[String],
                   paymentId: Option[String])

object SubItem {

  val empty: SubItem = SubItem(None, None, None, None, None, None, None, None)

  implicit val writes: OWrites[SubItem] = Json.writes[SubItem]

  implicit val reads: Reads[SubItem] = for {
    subItemId <- (JsPath \ "items" \\ "subItemId").readNullable[String]
    amount <- (JsPath \ "items" \\  "amount").readNullable[BigDecimal]
    clearingDate <- (JsPath \ "items" \\ "clearingDate").readNullable[String]
    clearingReason <- (JsPath \ "items" \\  "clearingReason").readNullable[String]
    outgoingPaymentMethod <- (JsPath \ "items" \\  "outgoingPaymentMethod").readNullable[String]
    paymentAmount <- (JsPath \ "items" \\  "paymentAmount").readNullable[BigDecimal]
    paymentMethod <- (JsPath \ "items" \\  "paymentMethod").readNullable[String]
    paymentLot <- (JsPath \ "items" \\  "paymentLot").readNullable[String]
    paymentLotItem <- (JsPath \ "items" \\  "paymentLotItem").readNullable[String]
    } yield {
    val id: Option[String] = for {
      pl <- paymentLot
      pli <- paymentLotItem
    } yield s"$pl-$pli"
    SubItem(subItemId, amount, clearingDate, clearingReason, outgoingPaymentMethod,paymentAmount,paymentMethod, id)
  }
}

