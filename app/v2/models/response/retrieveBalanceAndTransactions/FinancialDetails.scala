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

package v2.models.response.retrieveBalanceAndTransactions

import api.models.domain.TaxYear
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class FinancialDetails(taxYear: String,
                            chargeDetail: ChargeDetail,
                            taxPeriodFrom: Option[String],
                            taxPeriodTo: Option[String],
                            contractAccount: Option[String],
                            documentNumber: Option[String],
                            documentNumberItem: Option[String],
                            chargeReference: Option[String],
                            originalAmount: Option[BigDecimal],
                            outstandingAmount: Option[BigDecimal],
                            clearedAmount: Option[BigDecimal],
                            accruedInterest: Option[BigDecimal],
                            items: Seq[FinancialDetailsItem])

object FinancialDetails {
  implicit val writes: Writes[FinancialDetails] = Json.writes[FinancialDetails]

  implicit def reads(implicit readLocks: FinancialDetailsItem.ReadLocks): Reads[FinancialDetails] = (
    (JsPath \ "taxYear").read[String].map(TaxYear.fromDownstream(_).asMtd) and
      JsPath.read[ChargeDetail] and
      (JsPath \ "taxPeriodFrom").readNullable[String] and
      (JsPath \ "taxPeriodTo").readNullable[String] and
      (JsPath \ "contractAccount").readNullable[String] and
      (JsPath \ "sapDocumentNumber").readNullable[String] and
      (JsPath \ "sapDocumentNumberItem").readNullable[String] and
      (JsPath \ "chargeReference").readNullable[String] and
      (JsPath \ "originalAmount").readNullable[BigDecimal] and
      (JsPath \ "outstandingAmount").readNullable[BigDecimal] and
      (JsPath \ "clearedAmount").readNullable[BigDecimal] and
      (JsPath \ "accruedInterest").readNullable[BigDecimal] and
      (JsPath \ "items").read[Seq[FinancialDetailsItem]]
  )(FinancialDetails.apply _)

}
