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

import v1.controllers.requestParsers.validators.validations.{DateFormatValidation, DateRangeValidation, MissingParameterValidation, NinoValidation}
import v1.models.errors._
import v1.models.request.RawDataWithDateRange

trait ValidatorWithDateRange[T <: RawDataWithDateRange] extends Validator[T] {
    private val validationSet = List(parameterFormatValidation, parameterRuleValidation)

    private def parameterFormatValidation: T => List[List[MtdError]] = (data: T) => {
      List(
        NinoValidation.validate(data.nino),
        data.from.map(DateFormatValidation.validate(_, FromDateFormatError)).getOrElse(Nil),
        data.to.map(DateFormatValidation.validate(_, ToDateFormatError)).getOrElse(Nil)
      )
    }

      private def parameterRuleValidation: T => List[List[MtdError]] = { data =>
      List(
        MissingParameterValidation.validate(data.from, MissingFromDateError),
        MissingParameterValidation.validate(data.to, MissingToDateError),
        (for {
          from <- data.from
          to <- data.to
        } yield DateRangeValidation.validate(from, to)).getOrElse(Nil)
      )
    }

    override def validate(data: T): List[MtdError] = {
      run(validationSet, data).distinct
    }
}
