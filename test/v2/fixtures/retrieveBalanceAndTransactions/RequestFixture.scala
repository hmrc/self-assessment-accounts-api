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

import api.controllers.ControllerTestRunner.validNino
import api.models.domain.Nino
import v2.models.request.retrieveBalanceAndTransactions.{RetrieveBalanceAndTransactionsRawData, RetrieveBalanceAndTransactionsRequest}

object RequestFixture {
  val validDocNumber: String = "1234"
  val validFromDate: String  = "2022-08-15"
  val validToDate: String    = "2022-09-15"

  val inputDataDocNumber: RetrieveBalanceAndTransactionsRawData = RetrieveBalanceAndTransactionsRawData(
    nino = validNino,
    docNumber = Some(validDocNumber),
    fromDate = None,
    toDate = None,
    onlyOpenItems = None,
    includeLocks = None,
    calculateAccruedInterest = None,
    removePOA = None,
    customerPaymentInformation = None,
    includeEstimatedCharges = None
  )

  val inputDataDateRange: RetrieveBalanceAndTransactionsRawData = RetrieveBalanceAndTransactionsRawData(
    nino = validNino,
    docNumber = None,
    fromDate = Some(validFromDate),
    toDate = Some(validToDate),
    onlyOpenItems = None,
    includeLocks = None,
    calculateAccruedInterest = None,
    removePOA = None,
    customerPaymentInformation = None,
    includeEstimatedCharges = None
  )

  val inputDataEverythingTrue: RetrieveBalanceAndTransactionsRawData = RetrieveBalanceAndTransactionsRawData(
    nino = validNino,
    docNumber = Some(validDocNumber),
    fromDate = None,
    toDate = None,
    onlyOpenItems = Some("true"),
    includeLocks = Some("true"),
    calculateAccruedInterest = Some("true"),
    removePOA = Some("true"),
    customerPaymentInformation = Some("true"),
    includeEstimatedCharges = Some("true")
  )

  val inputDataEverythingFalse: RetrieveBalanceAndTransactionsRawData = RetrieveBalanceAndTransactionsRawData(
    nino = validNino,
    docNumber = Some(validDocNumber),
    fromDate = None,
    toDate = None,
    onlyOpenItems = Some("false"),
    includeLocks = Some("false"),
    calculateAccruedInterest = Some("false"),
    removePOA = Some("false"),
    customerPaymentInformation = Some("false"),
    includeEstimatedCharges = Some("false")
  )

  val requestDocNumber: RetrieveBalanceAndTransactionsRequest = RetrieveBalanceAndTransactionsRequest(
    nino = Nino(validNino),
    docNumber = Some(validDocNumber),
    fromDate = None,
    toDate = None,
    onlyOpenItems = false,
    includeLocks = false,
    calculateAccruedInterest = false,
    removePOA = false,
    customerPaymentInformation = false,
    includeEstimatedCharges = false
  )

  val requestDateRange: RetrieveBalanceAndTransactionsRequest = RetrieveBalanceAndTransactionsRequest(
    nino = Nino(validNino),
    docNumber = None,
    fromDate = Some(validFromDate),
    toDate = Some(validToDate),
    onlyOpenItems = false,
    includeLocks = false,
    calculateAccruedInterest = false,
    removePOA = false,
    customerPaymentInformation = false,
    includeEstimatedCharges = false
  )

  val requestEverythingTrue: RetrieveBalanceAndTransactionsRequest = RetrieveBalanceAndTransactionsRequest(
    nino = Nino(validNino),
    docNumber = Some(validDocNumber),
    fromDate = None,
    toDate = None,
    onlyOpenItems = true,
    includeLocks = true,
    calculateAccruedInterest = true,
    removePOA = true,
    customerPaymentInformation = true,
    includeEstimatedCharges = true
  )

}
