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

package v2.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.controllers.requestParsers.validators.validations.{
  BooleanValidation,
  DateFormatValidation,
  DateRangeValidationV2,
  DocNumberValidation,
  NinoValidation
}
import api.models.errors.{
  InvalidCalculateAccruedInterestError,
  InvalidCustomerPaymentInformationError,
  InvalidDateFromError,
  InvalidDateToError,
  InvalidIncludeLocksError,
  InvalidIncludeChargeEstimateError,
  InvalidOnlyOpenItemsError,
  InvalidRemovePaymentOnAccountError,
  MtdError
}
import config.AppConfig
import v2.models.request.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsRawData

import javax.inject.Inject

class RetrieveBalanceAndTransactionsValidator @Inject() (appConfig: AppConfig) extends Validator[RetrieveBalanceAndTransactionsRawData] {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation)

  private def parameterFormatValidation: RetrieveBalanceAndTransactionsRawData => List[List[MtdError]] =
    (data: RetrieveBalanceAndTransactionsRawData) => {
      List(
        NinoValidation.validate(data.nino),
        DocNumberValidation.validate(data.docNumber),
        DateFormatValidation.validate(data.fromDate, InvalidDateFromError),
        DateFormatValidation.validate(data.toDate, InvalidDateToError),
        BooleanValidation.validate(data.onlyOpenItems, InvalidOnlyOpenItemsError),
        BooleanValidation.validate(data.includeLocks, InvalidIncludeLocksError),
        BooleanValidation.validate(data.calculateAccruedInterest, InvalidCalculateAccruedInterestError),
        BooleanValidation.validate(data.removePOA, InvalidRemovePaymentOnAccountError),
        BooleanValidation.validate(data.customerPaymentInformation, InvalidCustomerPaymentInformationError),
        BooleanValidation.validate(data.includeEstimatedCharges, InvalidIncludeChargeEstimateError)
      )
    }

  private def parameterRuleValidation: RetrieveBalanceAndTransactionsRawData => List[List[MtdError]] =
    (data: RetrieveBalanceAndTransactionsRawData) => {
      List(
        DateRangeValidationV2.validate(data.fromDate, data.toDate)
      )
    }

  override def validate(data: RetrieveBalanceAndTransactionsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

}
