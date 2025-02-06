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

package v4.retrieveBalanceAndTransactions

import shared.controllers.validators.Validator
import v4.retrieveBalanceAndTransactions.def1.Def1_RetrieveBalanceAndTransactionsValidator
import v4.retrieveBalanceAndTransactions.model.request.RetrieveBalanceAndTransactionsRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class RetrieveBalanceAndTransactionsValidatorFactory @Inject() {

  def validator(nino: String,
                docNumber: Option[String],
                fromDate: Option[String],
                toDate: Option[String],
                onlyOpenItems: Option[String],
                includeLocks: Option[String],
                calculateAccruedInterest: Option[String],
                removePOA: Option[String],
                customerPaymentInformation: Option[String],
                includeEstimatedCharges: Option[String]): Validator[RetrieveBalanceAndTransactionsRequestData] =
    new Def1_RetrieveBalanceAndTransactionsValidator(
      nino, docNumber, fromDate, toDate, onlyOpenItems, includeLocks, calculateAccruedInterest, removePOA, customerPaymentInformation, includeEstimatedCharges)

}
