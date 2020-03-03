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

package v1.models.response.retrieveTransaction

import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v1.models.domain.DesTaxYear

case class TransactionItem(taxYear: Option[String],
                           id: Option[String],
                           transactionDate: Option[String],
                           `type`: Option[String],
                           originalAmount: Option[BigDecimal],
                           outstandingAmount: Option[BigDecimal],
                           lastClearingDate: Option[String],
                           lastClearingReason: Option[String],
                           lastClearedAmount: Option[BigDecimal])

object TransactionItem {

  val empty: TransactionItem = TransactionItem(None, None, None, None, None, None, None, None, None)

  implicit val reads: Reads[TransactionItem] = (
    for {
      taxYear <- (JsPath \ "taxYear").readNullable[String].map(_.map(taxYear => DesTaxYear.fromDesIntToString(Integer.parseInt(taxYear))))
      paymentLot <- (JsPath \ "paymentLot").readNullable[String]
      paymentLotItem <- (JsPath \ "paymentLotItem").readNullable[String]
      documentId <- (JsPath \ "documentId").readNullable[String]
      transactionDate <- (JsPath \ "transactionDate").readNullable[String]
      aType <- (JsPath \ "type").readNullable[String]
      originalAmount <- (JsPath \ "originalAmount").readNullable[BigDecimal]
      outstandingAmount <- (JsPath \ "outstandingAmount").readNullable[BigDecimal]
      lastClearingDate <- (JsPath \ "lastClearingDate").readNullable[String]
      lastClearingReason <- (JsPath \ "lastClearingReason").readNullable[String]
      lastClearedAmount <- (JsPath \ "lastClearedAmount").readNullable[BigDecimal]
    } yield {

      val id: Option[String] =
        if (paymentLot.nonEmpty && paymentLotItem.nonEmpty) Some(s"${paymentLot.get}-${paymentLotItem.get}") else documentId

      TransactionItem(taxYear, id, transactionDate, aType, originalAmount, outstandingAmount,
        lastClearingDate, lastClearingReason, lastClearedAmount)
    })

  implicit val writes: OWrites[TransactionItem] = Json.writes[TransactionItem]
}
