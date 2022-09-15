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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v2.models.response.retrieveBalanceAndTransactions.financeDetailTypes.mapping

case class FinanceDetails(taxYear: String,
                          documentId: String,
                          chargeType: Option[String],
                          mainType: Option[String],
                          taxPeriodFrom: Option[String],
                          taxPeriodTo: Option[String],
                          contractAccountCategory: Option[String],
                          contractAccount: Option[String],
                          documentNumber: Option[String],
                          documentNumberItem: Option[String],
                          chargeReference: Option[String],
                          mainTransaction: Option[String],
                          subTransaction: Option[String],
                          originalAmount: Option[BigDecimal],
                          outstandingAmount: Option[BigDecimal],
                          clearedAmount: Option[BigDecimal],
                          accruedInterest: Option[BigDecimal],
                          items: Seq[FinancialDetailsItem])

object FinanceDetails {

  implicit val reads: Reads[FinanceDetails] = (
    (JsPath \ "taxYear").read[String] and
      (JsPath \ "documentId").read[String] and
      (JsPath \ "chargeType").readNullable[String] and
      (JsPath \ "mainType").readNullable[String].map(c => getMainType(c)) and
      (JsPath \ "taxPeriodFrom").readNullable[String] and
      (JsPath \ "taxPeriodTo").readNullable[String] and
      (JsPath \ "contractAccountCategory").readNullable[String] and
      (JsPath \ "contractAccount").readNullable[String] and
      (JsPath \ "sapDocumentNumber").readNullable[String] and
      (JsPath \ "sapDocumentNumberItem").readNullable[String] and
      (JsPath \ "chargeReference").readNullable[String] and
      (JsPath \ "mainTransaction").readNullable[String] and
      (JsPath \ "subTransaction").readNullable[String] and
      (JsPath \ "originalAmount").readNullable[BigDecimal] and
      (JsPath \ "outstandingAmount").readNullable[BigDecimal] and
      (JsPath \ "clearedAmount").readNullable[BigDecimal] and
      (JsPath \ "accruedInterest").readNullable[BigDecimal] and
      (JsPath \ "items").read[Seq[FinancialDetailsItem]]
  )(FinanceDetails.apply _)

  private def getMainType(downstreamValue: Option[String]): Option[String] = {
    mapping.get(downstreamValue.get) match {
      case None    => None
      case Some(s) => Option(s)
      case _       => None
    }
  }

  implicit val writes: OWrites[FinanceDetails] = Json.writes[FinanceDetails]

}

object financeDetailTypes {

  val mapping: Map[String, String] = Map(
    ("3880" -> "Income Tax Estimate"),
    ("4900" -> "SA Income Tax Voluntary Paym."),
    ("4905" -> "SA Balancing Charge Credit"),
    ("4910" -> "SA Balancing Charge"),
    ("4915" -> "SA Balancing Charge Int."),
    ("4920" -> "SA Payment on Account 1"),
    ("4925" -> "Migrated Payment on Account 1"),
    ("4930" -> "SA Payment on Account 2"),
    ("4935" -> "Migrated Payment on Account 2"),
    ("4940" -> "SA Late Filing Fixed Penalty"),
    ("4950" -> "SA Late Filing Daily Penalty"),
    ("4955" -> "SA Late Filing Daily Pen. Int."),
    ("4960" -> "SA 6 Month Late Filing Penalty"),
    ("4965" -> "SA 6 Month Late Filing Pen Int"),
    ("4970" -> "SA 12 Month Late Filing Pen."),
    ("4975" -> "SA 12 Month Late Fil Pen. Int."),
    ("6010" -> "SA Manual Interest"),
    ("6015" -> "Migrated SA Repyt Suppl Credit"),
    ("6020" -> "SA Repayment Supplement Credit"),
    ("6030" -> "SA Manual Late Filing Penalty"),
    ("6040" -> "SA Manual Late Payment Penalty"),
    ("6050" -> "SA Migrated Late Payt Interest"),
    ("6060" -> "SA Man 30 Day Late Pyt Penalty"),
    ("6070" -> "SA Man 6 Mth Late Pyt Penalty"),
    ("6080" -> "SA Man 12 Mth Late Pyt Penalty"),
    ("6090" -> "SA Man Late Payment Interest"),
    ("6110" -> "ITSA Cutover Credits"),
    ("6120" -> "SA Man 6 Mth Late Filing Pen"),
    ("6130" -> "SA Man 12 Mth Late Filing Pen"),
    ("6140" -> "SA Man Late Payment Interest"),
    ("6150" -> "SA Manual Daily Penalty"),
    ("6160" -> "SA Manual Fixed Penalty (1st)"),
    ("6170" -> "SA Man Late Filing Penalty 1"),
    ("6180" -> "SA Freestanding Credit"),
    ("6210" -> "SA CIS Deductions")
  )

}
