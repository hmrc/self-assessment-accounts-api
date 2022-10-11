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

import api.models.errors.{DateFromFormatError, DateToFormatError, InvalidDateRangeError, MissingFromDateError, MissingToDateError, MtdError, RuleDateRangeInvalidError, RuleDateToBeforeDateFromError}

import java.time.temporal.ChronoUnit.DAYS
import java.time.LocalDate

object StartEndDateValidation {

  def validate(from: Option[String], to: Option[String]): List[MtdError] = {
    (from, to) match {
      case (Some(f), Some(t)) => validateDates(f, t)
      case (Some(_), None)    => List(MissingToDateError)
      case (None, Some(_))    => List(MissingFromDateError)
      case (None, None)       => NoValidationErrors
      case _                  => List(InvalidDateRangeError)
    }
  }

  private def validateDates(from: String, to: String): List[MtdError] = {
    val dateFormatErrors = DateFormatValidation.validate(from, DateFromFormatError) ++
      DateFormatValidation.validate(to, DateToFormatError)

    if (dateFormatErrors.equals(Nil)) {
      val toBeforeFromErrors: List[MtdError] = checkIfToIsBeforeFrom(from, to)

      if (toBeforeFromErrors.equals(Nil)) checkDateRange(from, to) else toBeforeFromErrors
    } else {
      dateFormatErrors
    }
  }

  private def checkIfToIsBeforeFrom(from: String, to: String): List[MtdError] = {
    val fmtFrom = LocalDate.parse(from, dateFormat)
    val fmtTo   = LocalDate.parse(to, dateFormat)
    if (fmtTo isBefore fmtFrom) List(RuleDateToBeforeDateFromError) else NoValidationErrors
  }

  private def checkDateRange(from: String, to: String): List[MtdError] = {
    val fmtFrom = LocalDate.parse(from, dateFormat)
    val fmtTo   = LocalDate.parse(to, dateFormat)

    val MAX_DATE_RANGE: Int = 366
    val days: Int           = DAYS.between(fmtFrom, fmtTo).toInt

    if (days > MAX_DATE_RANGE) List(RuleDateRangeInvalidError) else NoValidationErrors
  }

}
