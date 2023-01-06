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

package v2.fixtures.retrieveBalanceAndTransactions

import play.api.libs.json.{JsValue, Json}
import v2.models.response.retrieveBalanceAndTransactions.FinancialDetailsItemLocks

trait FinancialDetailsItemLocksFixture {

  val financialDetailsItemLocks: FinancialDetailsItemLocks =
    FinancialDetailsItemLocks(isChargeOnHold = true, isEstimatedChargeOnHold = true, isInterestAccrualOnHold = true, isInterestChargeOnHold = true)

  val financialDetailsItemLocksFalse: FinancialDetailsItemLocks =
    FinancialDetailsItemLocks(
      isChargeOnHold = false,
      isEstimatedChargeOnHold = false,
      isInterestAccrualOnHold = false,
      isInterestChargeOnHold = false)

  val financialDetailsItemLocksMtdJson: JsValue = Json.parse(
    """{
      |     "isChargeOnHold": true,
      |     "isEstimatedChargeOnHold": true,
      |     "isInterestAccrualOnHold": true,
      |     "isInterestChargeOnHold": true
      }""".stripMargin
  )

}
