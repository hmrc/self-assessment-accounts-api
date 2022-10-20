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

package v2.controllers.requestParsers

import api.controllers.requestParsers.RequestParser
import api.models.domain.Nino
import v2.controllers.requestParsers.validators.RetrieveBalanceAndTransactionsValidator
import v2.models.request.retrieveBalanceAndTransactions.{RetrieveBalanceAndTransactionsRawData, RetrieveBalanceAndTransactionsRequest}

import javax.inject.Inject

class RetrieveBalanceAndTransactionsRequestParser @Inject() (val validator: RetrieveBalanceAndTransactionsValidator)
    extends RequestParser[RetrieveBalanceAndTransactionsRawData, RetrieveBalanceAndTransactionsRequest] {

  override protected def requestFor(data: RetrieveBalanceAndTransactionsRawData): RetrieveBalanceAndTransactionsRequest =
    RetrieveBalanceAndTransactionsRequest(
      nino = Nino(data.nino),
      docNumber = data.docNumber,
      fromDate = data.fromDate,
      toDate = data.toDate,
      onlyOpenItems = toBool(data.onlyOpenItems),
      includeLocks = toBool(data.includeLocks),
      calculateAccruedInterest = toBool(data.calculateAccruedInterest),
      removePOA = toBool(data.removePOA),
      customerPaymentInformation = toBool(data.customerPaymentInformation),
      includeEstimatedCharges = toBool(data.includeEstimatedCharges)
    )

  private def toBool(param: Option[String]): Boolean = param.exists(_.toBoolean)
}
