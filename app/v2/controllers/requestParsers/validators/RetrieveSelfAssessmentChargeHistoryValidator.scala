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

import com.google.inject.Inject
import config.AppConfig
import v2.controllers.requestParsers.validators.validations.TransactionIdValidation
import v2.controllers.requestParsers.validators.validations.NinoValidation
import v2.models.errors.MtdError
import v2.models.request.retrieveSelfAssessmentChargeHistory.RetrieveSelfAssessmentChargeHistoryRawData

import javax.inject.Singleton

@Singleton
class RetrieveSelfAssessmentChargeHistoryValidator @Inject()(appConfig: AppConfig) extends Validator[RetrieveSelfAssessmentChargeHistoryRawData] {

  private val validationSet = List(parameterFormatValidation)

  private def parameterFormatValidation: RetrieveSelfAssessmentChargeHistoryRawData => List[List[MtdError]] =
    (data: RetrieveSelfAssessmentChargeHistoryRawData) => {
      List(
        NinoValidation.validate(data.nino),
        TransactionIdValidation.validate(data.transactionId)
      )
    }

  override def validate(data: RetrieveSelfAssessmentChargeHistoryRawData): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
