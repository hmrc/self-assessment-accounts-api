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

package api.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.validations.{DateFormatValidation, DateRangeValidationV1, MissingParameterValidation, NinoValidation}
import api.models.errors._
import api.models.request.RawDataWithDateRange

trait ValidatorWithDateRange[T <: RawDataWithDateRange] extends Validator[T] {
  private val validationSet = List(parameterFormatValidation, parameterRuleValidation)

  override def validate(data: T): List[MtdError] = {
    run(validationSet, data).distinct
  }

  private def parameterFormatValidation: T => List[List[MtdError]] = (data: T) => {
    List(
      NinoValidation.validate(data.nino),
      data.from.map(DateFormatValidation.validate(_, V1_FromDateFormatError)).getOrElse(Nil),
      data.to.map(DateFormatValidation.validate(_, V1_ToDateFormatError)).getOrElse(Nil)
    )
  }

  private def parameterRuleValidation: T => List[List[MtdError]] = { data =>
    List(
      MissingParameterValidation.validate(data.from, V1_MissingFromDateError),
      MissingParameterValidation.validate(data.to, V1_MissingToDateError),
      (for {
        from <- data.from
        to   <- data.to
      } yield DateRangeValidationV1.validate(from, to)).getOrElse(Nil)
    )
  }

}
