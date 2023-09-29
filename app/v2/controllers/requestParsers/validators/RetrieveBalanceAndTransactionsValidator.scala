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

package v2.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.controllers.requestParsers.validators.validations._
import api.models.errors._
import v2.models.request.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsRawData

class RetrieveBalanceAndTransactionsValidator() extends Validator[RetrieveBalanceAndTransactionsRawData] {

  private val dateFormatValidation: DateFormatValidation = new DateFormatValidation(minYear = 1900, maxYear = 2100)
  private val validationSet = List(parameterFormatValidation, parameterRuleValidation)

  override def validate(data: RetrieveBalanceAndTransactionsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: RetrieveBalanceAndTransactionsRawData => List[List[MtdError]] =
    (data: RetrieveBalanceAndTransactionsRawData) => {
      List(
        NinoValidation.validate(data.nino),
        DocNumberValidation.validate(data.docNumber),
        dateFormatValidation.validate(data.fromDate, isFromDate = true, FromDateFormatError),
        dateFormatValidation.validate(data.toDate, isFromDate = false, ToDateFormatError),
        BooleanValidation.validate(data.onlyOpenItems, OnlyOpenItemsFormatError),
        BooleanValidation.validate(data.includeLocks, IncludeLocksFormatError),
        BooleanValidation.validate(data.calculateAccruedInterest, CalculateAccruedInterestFormatError),
        BooleanValidation.validate(data.removePOA, RemovePaymentOnAccountFormatError),
        BooleanValidation.validate(data.customerPaymentInformation, CustomerPaymentInformationFormatError),
        BooleanValidation.validate(data.includeEstimatedCharges, IncludeEstimatedChargesFormatError)
      )
    }

  private def parameterRuleValidation: RetrieveBalanceAndTransactionsRawData => List[List[MtdError]] =
    (data: RetrieveBalanceAndTransactionsRawData) => {
      List(
        DateRangeValidationV2.validate(data.fromDate, data.toDate),
        OnlyOpenItemsValidation.validate(data.onlyOpenItems, data.docNumber, data.toDate, data.fromDate)
      )
    }

}
