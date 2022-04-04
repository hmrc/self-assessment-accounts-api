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

package v1.models.response.retrieveTransactionDetails

import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class TransactionItem(transactionItemId: Option[String],
                           `type`: Option[String],
                           taxPeriodFrom: Option[String],
                           taxPeriodTo: Option[String],
                           originalAmount: Option[BigDecimal],
                           outstandingAmount: Option[BigDecimal],
                           dueDate: Option[String],
                           paymentMethod: Option[String],
                           paymentId: Option[String],
                           subItems: Seq[SubItem])

object TransactionItem {

  val empty: TransactionItem = TransactionItem(None, None, None, None, None, None, None, None, None, Seq.empty[SubItem])

  implicit val writes: OWrites[TransactionItem] = Json.writes[TransactionItem]

  implicit val reads: Reads[TransactionItem] = for {
    sapDocumentItemId <- (JsPath \ "sapDocumentNumberItem").readNullable[String]
    transactionType   <- (JsPath \ "chargeType").readNullable[String]
    taxPeriodFrom     <- (JsPath \ "taxPeriodFrom").readNullable[String]
    taxPeriodTo       <- (JsPath \ "taxPeriodTo").readNullable[String]
    originalAmount    <- (JsPath \ "originalAmount").readNullable[BigDecimal]
    outstandingAmount <- (JsPath \ "outstandingAmount").readNullable[BigDecimal]
    subItems          <- (JsPath \ "items").read[Seq[SubItem]].map(_.filterNot(item => item == SubItem.empty || item.subItemId.isEmpty))
  } yield {

    lazy val lowestNumberedSubItem: SubItem = subItems.foldLeft(SubItem.empty)(returnLowestNumberedItem)

    TransactionItem(
      sapDocumentItemId,
      transactionType,
      taxPeriodFrom,
      taxPeriodTo,
      originalAmount,
      outstandingAmount,
      lowestNumberedSubItem.dueDate,
      lowestNumberedSubItem.paymentMethod,
      lowestNumberedSubItem.paymentId,
      subItems.filterNot(_ == lowestNumberedSubItem)
    )
  }

  val returnLowestNumberedItem: (SubItem, SubItem) => SubItem = (item1: SubItem, item2: SubItem) => {
    (item1.subItemId, item2.subItemId) match {
      case (Some(id1), Some(id2)) => if (id1.toInt < id2.toInt) item1 else item2
      case (Some(_), None)        => item1
      case (None, Some(_))        => item2
      case _                      => SubItem.empty
    }
  }

}
