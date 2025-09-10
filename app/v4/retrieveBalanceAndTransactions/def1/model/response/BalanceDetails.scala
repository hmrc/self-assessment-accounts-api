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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import shared.models.domain.TaxYear

case class BalancePerYear(bcdAmount: BigDecimal, taxYear: String)

object BalancePerYear {

  implicit val writes: OWrites[BalancePerYear] =
    Json.writes[BalancePerYear]

}

case class BalanceDetails(payableAmount: BigDecimal,
                          payableDueDate: Option[String],
                          pendingChargeDueAmount: BigDecimal,
                          pendingChargeDueDate: Option[String],
                          overdueAmount: BigDecimal,
                          bcdBalancePerYear: Seq[BalancePerYear],
                          earliestPaymentDateOverdue: Option[String],
                          totalBalance: BigDecimal,
                          amountCodedOut: Option[BigDecimal],
                          totalBcdBalance: Option[BigDecimal],
                          unallocatedCredit: Option[BigDecimal],
                          allocatedCredit: Option[BigDecimal],
                          totalCredit: Option[BigDecimal],
                          firstPendingAmountRequested: Option[BigDecimal],
                          secondPendingAmountRequested: Option[BigDecimal],
                          availableCredit: Option[BigDecimal])

object BalanceDetails {

  // Downstream fields 'should' both be present but this is not in the spec -
  // so use this to enable safe transformation to mandatory MTD fields
  private case class DownstreamBalancePerYear(amount: Option[BigDecimal], taxYear: Option[String])

  private object DownstreamBalancePerYear {
    implicit val reads: Reads[DownstreamBalancePerYear] = Json.reads

    def asMtd: PartialFunction[DownstreamBalancePerYear, BalancePerYear] = { case DownstreamBalancePerYear(Some(amount), Some(taxYear)) =>
      BalancePerYear(amount, TaxYear.fromDownstream(taxYear).asMtd)
    }

  }

  implicit val reads: Reads[BalanceDetails] = (
    (JsPath \ "balanceDueWithin30Days").read[BigDecimal].orElse((JsPath \ "balanceDueWithin30days").read[BigDecimal]) and
      (JsPath \ "nextPaymentDateForChargesDueIn30Days")
        .read[String]
        .map(s => Option(s))
        .orElse((JsPath \ "nxtPymntDateChrgsDueIn30Days").readNullable[String]) and
      (JsPath \ "balanceNotDueIn30Days")
        .read[BigDecimal]
        .orElse((JsPath \ "balanceNotDuein30Days").read[BigDecimal]) and
      (JsPath \ "nextPaymentDateBalanceNotDue")
        .read[String]
        .map(s => Option(s))
        .orElse((JsPath \ "nextPaymntDateBalnceNotDue").readNullable[String]) and
      (JsPath \ "overDueAmount").read[BigDecimal] and
      (JsPath \ "bcdBalancePerYear").readNullable[Seq[DownstreamBalancePerYear]].map {
        case Some(bs) => bs.collect(DownstreamBalancePerYear.asMtd)
        case None     => Nil
      } and
      (JsPath \ "earliestPaymentDateOverDue")
        .read[String]
        .map(s => Option(s))
        .orElse((JsPath \ "earlistPymntDateOverDue").readNullable[String]) and
      (JsPath \ "totalBalance").read[BigDecimal] and
      (JsPath \ "amountCodedOut").readNullable[BigDecimal] and
      (JsPath \ "totalBCDBalance").readNullable[BigDecimal] and
      (JsPath \ "unallocatedCredit").readNullable[BigDecimal] and
      (JsPath \ "allocatedCredit").readNullable[BigDecimal] and
      (JsPath \ "totalCredit").readNullable[BigDecimal] and
      (JsPath \ "firstPendingAmountRequested").readNullable[BigDecimal] and
      (JsPath \ "secondPendingAmountRequested").readNullable[BigDecimal] and
      (JsPath \ "availableCredit").readNullable[BigDecimal]
  )(BalanceDetails.apply)

  implicit val writes: OWrites[BalanceDetails] = Json.writes[BalanceDetails]

}
