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
import api.controllers.requestParsers.validators.validations.{NinoValidation, TransactionIdValidation}
import api.models.errors.MtdError
import com.google.inject.Inject
import config.AppConfig
import v2.models.request.retrieveChargeHistory.RetrieveChargeHistoryRawData

import javax.inject.Singleton

@Singleton
class RetrieveChargeHistoryValidator @Inject() (appConfig: AppConfig) extends Validator[RetrieveChargeHistoryRawData] {

  private val validationSet = List(parameterFormatValidation)

  override def validate(data: RetrieveChargeHistoryRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: RetrieveChargeHistoryRawData => List[List[MtdError]] =
    (data: RetrieveChargeHistoryRawData) => {
      List(
        NinoValidation.validate(data.nino),
        TransactionIdValidation.validate(data.transactionId)
      )
    }

}
