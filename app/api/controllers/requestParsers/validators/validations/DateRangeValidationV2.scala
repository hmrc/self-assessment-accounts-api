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

package api.controllers.requestParsers.validators.validations

import api.models.errors.{MtdError, V2_MissingFromDateError, V2_MissingToDateError, V2_RangeToDateBeforeFromDateError}

import java.time.LocalDate

object DateRangeValidationV2 {

  def validate(fromDate: Option[String], toDate: Option[String]): List[MtdError] = {
    (fromDate, toDate) match {
      case (Some(f), Some(t)) => checkIfToDateIsBeforeFromDate(f, t)
      case (Some(_), None)    => List(V2_MissingToDateError)
      case (None, Some(_))    => List(V2_MissingFromDateError)
      case (None, None)       => Nil
    }
  }

  private def checkIfToDateIsBeforeFromDate(fromDate: String, toDate: String): List[MtdError] = {
    val fmtFrom = LocalDate.parse(fromDate, dateFormat)
    val fmtTo   = LocalDate.parse(toDate, dateFormat)
    if (fmtTo isBefore fmtFrom) List(V2_RangeToDateBeforeFromDateError) else Nil
  }

}
