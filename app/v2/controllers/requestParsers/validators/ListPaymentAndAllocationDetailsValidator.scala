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
import api.controllers.requestParsers.validators.validations.{BooleanValidation, DateFormatValidation, DateRangeValidationV2, DocNumberValidation, NinoValidation, PaymentLotItemValidation, PaymentLotValidation, ToFromDateValidation}
import api.models.errors.{EndDateFormatError, InvalidCalculateAccruedInterestError, InvalidCustomerPaymentInformationError, InvalidDateFromError, InvalidDateToError, InvalidIncludeChargeEstimateError, InvalidIncludeLocksError, InvalidOnlyOpenItemsError, InvalidRemovePaymentOnAccountError, MtdError, StartDateFormatError}
import config.AppConfig
import v2.models.request.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsRawData

import javax.inject.Inject

class ListPaymentAndAllocationDetailsValidator @Inject() (appConfig: AppConfig) extends Validator[ListPaymentAndAllocationDetailsRawData] {

  private val validationSet = List(parameterFormatValidation)

  private def parameterFormatValidation: ListPaymentAndAllocationDetailsRawData => List[List[MtdError]] =
    (data: ListPaymentAndAllocationDetailsRawData) => {
      List(
        NinoValidation.validate(data.nino),
        ToFromDateValidation.validate(data.from, data.to),
        PaymentLotValidation.validate(data.paymentLot),
        PaymentLotItemValidation.validate(data.paymentLotItem),
      )
    }

  override def validate(data: ListPaymentAndAllocationDetailsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

}
