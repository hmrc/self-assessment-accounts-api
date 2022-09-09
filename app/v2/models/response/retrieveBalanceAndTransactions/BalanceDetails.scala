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
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class BalanceDetails(payableAmount: BigDecimal,
                          payableDueDate: Option[String],
                          pendingChargeDueAmount: BigDecimal,
                          pendingChargeDueDate: Option[String],
                          overdueAmount: BigDecimal,
                          earliestPaymentDateOverDue: Option[String],
                          totalBalance: BigDecimal,
                          amountCodedOut: Option[BigDecimal],
                          bcdBalancePerYear: Option[Seq[BalancePerYear]],
                          totalBcdBalance: Option[BigDecimal],
                          unallocatedCredit: Option[BigDecimal],
                          allocatedCredit: Option[BigDecimal],
                          totalCredit: Option[BigDecimal],
                          firstPendingAmountRequested: Option[BigDecimal],
                          secondPendingAmountRequested: Option[BigDecimal],
                          availableCredit: Option[BigDecimal])

object BalanceDetails {

  implicit val reads: Reads[BalanceDetails] =
    ((JsPath \ "balanceDueWithin30Days").read[BigDecimal] and
      (JsPath \ "nextPaymentDateForChargesDueIn30Days").readNullable[String] and
      (JsPath \ "balanceNotDueIn30Days").read[BigDecimal] and
      (JsPath \ "nextPaymentDateBalanceNotDue").readNullable[String] and
      (JsPath \ "overDueAmount").read[BigDecimal] and
      (JsPath \ "earliestPaymentDateOverDue").readNullable[String] and
      (JsPath \ "totalBalance").read[BigDecimal] and
      (JsPath \ "amountCodedOut").readNullable[BigDecimal] and
      (JsPath \ "bcdBalancePerYear").readNullable[Seq[BalancePerYear]] and
      (JsPath \ "totalBCDBalance").readNullable[BigDecimal] and
      (JsPath \ "unallocatedCredit").readNullable[BigDecimal] and
      (JsPath \ "allocatedCredit").readNullable[BigDecimal] and
      (JsPath \ "totalCredit").readNullable[BigDecimal] and
      (JsPath \ "firstPendingAmountRequested").readNullable[BigDecimal] and
      (JsPath \ "secondPendingAmountRequested").readNullable[BigDecimal] and
      (JsPath \ "availableCredit").readNullable[BigDecimal])(BalanceDetails.apply _)

  implicit val writes: OWrites[BalanceDetails] = Json.writes[BalanceDetails]
}
