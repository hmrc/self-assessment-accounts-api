/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators

import v1.controllers.requestParsers.validators.validations.{NinoValidation, TransactionIdValidation}
import v1.models.errors.MtdError
import v1.models.request.retrieveChargeHistory.RetrieveChargeHistoryRawRequest

class RetrieveChargeHistoryValidator extends Validator[RetrieveChargeHistoryRawRequest] {

  private val validationSet = List(parameterFormatValidation)

  private def parameterFormatValidation: RetrieveChargeHistoryRawRequest => List[List[MtdError]] = (data: RetrieveChargeHistoryRawRequest) => {
    List(
      NinoValidation.validate(data.nino),
      TransactionIdValidation.validate(data.chargeId)
    )
  }
  override def validate(data: RetrieveChargeHistoryRawRequest): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
