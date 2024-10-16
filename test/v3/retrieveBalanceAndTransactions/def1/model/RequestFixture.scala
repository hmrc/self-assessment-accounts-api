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

package v3.retrieveBalanceAndTransactions.def1.model

import api.controllers.ControllerTestRunner.validNino
import api.models.domain.{DateRange, Nino}
import v3.retrieveBalanceAndTransactions.model.request.RetrieveBalanceAndTransactionsRequestData

import java.time.LocalDate

object RequestFixture {
  val validDocNumber = "1234"
  val validFromDate  = "2022-08-15"
  val validToDate    = "2022-09-15"

  val requestDocNumber: RetrieveBalanceAndTransactionsRequestData = RetrieveBalanceAndTransactionsRequestData(
    nino = Nino(validNino),
    docNumber = Some(validDocNumber),
    fromAndToDates = None,
    onlyOpenItems = false,
    includeLocks = false,
    calculateAccruedInterest = false,
    removePOA = false,
    customerPaymentInformation = false,
    includeEstimatedCharges = false
  )

  val requestDateRange: RetrieveBalanceAndTransactionsRequestData = RetrieveBalanceAndTransactionsRequestData(
    nino = Nino(validNino),
    docNumber = None,
    fromAndToDates = Some(DateRange(LocalDate.parse(validFromDate), LocalDate.parse(validToDate))),
    onlyOpenItems = false,
    includeLocks = false,
    calculateAccruedInterest = false,
    removePOA = false,
    customerPaymentInformation = false,
    includeEstimatedCharges = false
  )

  val requestEverythingTrue: RetrieveBalanceAndTransactionsRequestData = RetrieveBalanceAndTransactionsRequestData(
    nino = Nino(validNino),
    docNumber = Some(validDocNumber),
    fromAndToDates = None,
    onlyOpenItems = true,
    includeLocks = true,
    calculateAccruedInterest = true,
    removePOA = true,
    customerPaymentInformation = true,
    includeEstimatedCharges = true
  )

}
