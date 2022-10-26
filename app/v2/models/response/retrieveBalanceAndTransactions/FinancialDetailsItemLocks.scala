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

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class FinancialDetailsItemLocks(
    isChargeOnHold: Boolean,
    isEstimatedChargeOnHold: Boolean,
    isInterestAccrualOnHold: Boolean,
    isInterestChargeOnHold: Boolean
)

object FinancialDetailsItemLocks {
  implicit val writes: Writes[FinancialDetailsItemLocks] = Json.writes

  implicit val reads: Reads[FinancialDetailsItemLocks] = {
    def bool(fieldName: String): Reads[Boolean] =
      (__ \ fieldName).readNullable[String].map(_.exists(_.nonEmpty))

    (bool("paymentLock") and
      bool("clearingLock") and
      bool("interestLock") and
      bool("dunningLock"))(FinancialDetailsItemLocks.apply _)
  }

}
