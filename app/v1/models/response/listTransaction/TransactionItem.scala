/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.models.response.listTransaction

import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v1.models.domain.DesTaxYear

case class TransactionItem(taxYear: String,
                           transactionId: String,
                           paymentId: Option[String],
                           transactionDate: String,
                           `type`: Option[String],
                           originalAmount: BigDecimal,
                           outstandingAmount: BigDecimal,
                           lastClearingDate: Option[String],
                           lastClearingReason: Option[String],
                           lastClearedAmount: Option[BigDecimal],
                           accruingInterestAmount: Option[BigDecimal],
                           interestRate: Option[BigDecimal],
                           interestFromDate: Option[String],
                           interestEndDate: Option[String],
                           latePaymentInterestAmount: Option[BigDecimal],
                           interestOutstandingAmount: Option[BigDecimal])

object TransactionItem {

  implicit val reads: Reads[TransactionItem] =
    for {
      taxYear <- (JsPath \ "taxYear").read[String].map(taxYear => DesTaxYear.fromDesIntToString(Integer.parseInt(taxYear)))
      transactionId <- (JsPath \ "documentId").read[String]
      paymentLot <- (JsPath \ "paymentLot").readNullable[String]
      paymentLotItem <- (JsPath \ "paymentLotItem").readNullable[String]
      transactionDate <- (JsPath \ "documentDate").read[String]
      aType <- (JsPath \ "documentDescription").readNullable[String]
      originalAmount <- (JsPath \ "totalAmount").read[BigDecimal]
      outstandingAmount <- (JsPath \ "documentOutstandingAmount").read[BigDecimal]
      lastClearingDate <- (JsPath \ "lastClearingDate").readNullable[String]
      lastClearingReason <- (JsPath \ "lastClearingReason").readNullable[String]
      lastClearedAmount <- (JsPath \ "lastClearedAmount").readNullable[BigDecimal]
      accruingInterestAmount <- (JsPath \ "accruingInterestAmount").readNullable[BigDecimal]
      interestRate <- (JsPath \ "interestRate").readNullable[BigDecimal]
      interestFromDate <- (JsPath \ "interestFromDate").readNullable[String]
      interestEndDate <- (JsPath \ "interestEndDate").readNullable[String]
      latePaymentInterestAmount <- (JsPath \ "latePaymentInterestAmount").readNullable[BigDecimal]
      interestOutstandingAmount <- (JsPath \ "interestOutstandingAmount").readNullable[BigDecimal]
    } yield {

      val paymentId = paymentLot.map(a => s"${a}-${paymentLotItem.get}")

      TransactionItem(taxYear, transactionId, paymentId, transactionDate, aType, originalAmount, outstandingAmount,
        lastClearingDate, lastClearingReason, lastClearedAmount, accruingInterestAmount, interestRate, interestFromDate,
        interestEndDate, latePaymentInterestAmount, interestOutstandingAmount)
    }

  implicit val writes: OWrites[TransactionItem] = Json.writes[TransactionItem]
}