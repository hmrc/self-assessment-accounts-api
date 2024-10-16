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

package v3.retrieveBalanceAndTransactions.def1.model.requests

import api.models.domain.{DateRange, Nino}
import v3.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsSchema
import v3.retrieveBalanceAndTransactions.model.request.RetrieveBalanceAndTransactionsRequestData

case class Def1_RetrieveBalanceAndTransactionsRequestData(
    nino: Nino,
    docNumber: Option[String],
    fromAndToDates: Option[DateRange],
    onlyOpenItems: Boolean,
    includeLocks: Boolean,
    calculateAccruedInterest: Boolean,
    removePOA: Boolean,
    customerPaymentInformation: Boolean,
    includeEstimatedCharges: Boolean
) extends RetrieveBalanceAndTransactionsRequestData {
  val schema: RetrieveBalanceAndTransactionsSchema = RetrieveBalanceAndTransactionsSchema.Def1
}
