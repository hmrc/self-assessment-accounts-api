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


case class TransactionItem(transactionItemId: Option[String],
                          `chargeType`: Option[String],
                           taxPeriodFrom: Option[String],
                           taxPeriodTo: Option[String],
                           originalAmount: Option[BigDecimal],
                           outstandingAmount: Option[BigDecimal],
                           dueDate: Option[String],
                           paymentMethod: Option[String],
                           paymentId: Option[String],
                           subItems: Option[Seq[SubItem]])

object TransactionItem {

  val empty: TransactionItem = TransactionItem(None, None, None, None, None, None, None, None, None, None)

  implicit val writes: OWrites[TransactionItem] = Json.writes[TransactionItem]

  implicit val reads: Reads[TransactionItem] = for {
    sapDocumentNumberItem <- (JsPath \ "sapDocumentNumberItem").readNullable[String]
    transactionType <- (JsPath \ "chargeType").readNullable[String]
    taxPeriodFrom <- (JsPath \ "taxPeriodFrom").readNullable[String]
    taxPeriodTo <- (JsPath \ "taxPeriodTo").readNullable[String]
    originalAmount <- (JsPath \ "originalAmount").readNullable[BigDecimal]
    outstandingAmount <- (JsPath \ "outstandingAmount").readNullable[BigDecimal]
    dueDate <- (JsPath \ "items" \\ "dueDate").readNullable[String]
    paymentMethod <- (JsPath \ "items" \\ "paymentMethod").readNullable[String]
    paymentLot <- (JsPath \ "items" \\ "paymentLot").readNullable[String]
    paymentLotItem <- (JsPath \ "items" \\ "paymentLotItem").readNullable[String]
    subItems <- (JsPath \ "items" \\ "subItems").readNullable[Seq[SubItem]].map(_.map(_.filterNot(item => item == SubItem.empty)))
  } yield{
    val id: Option[String] = for {
      pl <- paymentLot
      pli <- paymentLotItem
    } yield s"$pl-$pli"
    TransactionItem(sapDocumentNumberItem, transactionType, taxPeriodFrom, taxPeriodTo, originalAmount, outstandingAmount, dueDate, paymentMethod, id, subItems)
  }
}
