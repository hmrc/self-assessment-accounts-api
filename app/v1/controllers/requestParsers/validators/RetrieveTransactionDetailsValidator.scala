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

package v1.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.controllers.requestParsers.validators.validations.{NinoValidation, TransactionIdValidation}
import api.models.errors.MtdError
import v1.models.request.retrieveTransactionDetails.RetrieveTransactionDetailsRawRequest

class RetrieveTransactionDetailsValidator extends Validator[RetrieveTransactionDetailsRawRequest] {

  private val validationSet = List(parameterFormatValidation)

  private def parameterFormatValidation: RetrieveTransactionDetailsRawRequest => List[List[MtdError]] =
    (data: RetrieveTransactionDetailsRawRequest) => {
      List(
        NinoValidation.validate(data.nino),
        TransactionIdValidation.validate(data.transactionId)
      )
    }

  override def validate(data: RetrieveTransactionDetailsRawRequest): List[MtdError] = {
    run(validationSet, data).distinct
  }

}
