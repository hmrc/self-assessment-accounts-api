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
import api.controllers.requestParsers.validators.validations.{NinoValidation, PaymentLotItemValidation, PaymentLotValidation, StartEndDateValidation}
import api.models.errors.MtdError
import config.AppConfig
import v2.models.request.listPaymentsAndAllocationDetails.ListPaymentsAndAllocationDetailsRawData

import javax.inject.Inject

class ListPaymentsAndAllocationDetailsValidator @Inject() (appConfig: AppConfig) extends Validator[ListPaymentsAndAllocationDetailsRawData] {

  private val validationSet = List(parameterValidation)

  private def parameterValidation: ListPaymentsAndAllocationDetailsRawData => List[List[MtdError]] =
    (data: ListPaymentsAndAllocationDetailsRawData) => {
      List(
        NinoValidation.validate(data.nino),
        StartEndDateValidation.validate(data.dateFrom, data.dateTo),
        PaymentLotValidation.validate(data.paymentLot),
        PaymentLotItemValidation.validate(data.paymentLotItem),
      )
    }

  override def validate(data: ListPaymentsAndAllocationDetailsRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

}
