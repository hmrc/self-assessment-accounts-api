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

package v1.controllers.requestParsers.validators

import config.AppConfig
import javax.inject.Inject
import v1.controllers.requestParsers.validators.validations._
import v1.models.errors.MtdError
import v1.models.request.retrieveCodingOut.RetrieveCodingOutRawRequest

class RetrieveCodingOutValidator @Inject()(implicit appConfig: AppConfig)
  extends Validator[RetrieveCodingOutRawRequest] {

  private val validationSet = List(parameterFormatValidation, parameterRuleValidation)

  private def parameterFormatValidation: RetrieveCodingOutRawRequest => List[List[MtdError]] = (data: RetrieveCodingOutRawRequest) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear),
      data.source.map(SourceValidation.validate).getOrElse(Nil)
    )
  }

  private def parameterRuleValidation: RetrieveCodingOutRawRequest => List[List[MtdError]] = (data: RetrieveCodingOutRawRequest) => {
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear)
    )
  }

  override def validate(data: RetrieveCodingOutRawRequest): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
