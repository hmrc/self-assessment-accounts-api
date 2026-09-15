/*
 * Copyright 2026 HM Revenue & Customs
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

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

case class FinancialDetailsItemLocks(
    isChargeOnHold: Boolean,
    isEstimatedChargeOnHold: Boolean,
    isInterestAccrualOnHold: Boolean,
    isInterestChargeOnHold: Boolean,
    dunningLock: Option[String]
)

object FinancialDetailsItemLocks {
  implicit val writes: Writes[FinancialDetailsItemLocks] = Json.writes[FinancialDetailsItemLocks]

  implicit val reads: Reads[FinancialDetailsItemLocks] = {
    def readLock(fieldName: String): Reads[Boolean] = (__ \ fieldName).readNullable[String].map(_.exists(!_.isBlank))

    (
      readLock("paymentLock") and
        readLock("clearingLock") and
        readLock("interestLock") and
        (__ \ "dunningLock").readNullable[String]
    )((isChargeOnHold, isEstimatedChargeOnHold, isInterestAccrualOnHold, dunningLock) =>
      val validDunningLock: Option[String] = dunningLock.filterNot(_.isBlank)

      FinancialDetailsItemLocks(
        isChargeOnHold = isChargeOnHold,
        isEstimatedChargeOnHold = isEstimatedChargeOnHold,
        isInterestAccrualOnHold = isInterestAccrualOnHold,
        isInterestChargeOnHold = validDunningLock.isDefined,
        dunningLock = validDunningLock.map(toMtdDunningLock)
      )
    )
  }

  private def toMtdDunningLock(value: String): String = value.trim.toLowerCase match {
    case "formal stand over" => "currently suspended on appeal"
    case "stand over order"  => "collection suspended"
    case other               => other
  }

}
