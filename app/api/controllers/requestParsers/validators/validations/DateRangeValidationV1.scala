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

package api.controllers.requestParsers.validators.validations

import api.models.errors.{MtdError, RuleDateRangeInvalidError, RuleFromDateNotSupportedError, V1_RangeToDateBeforeFromDateError}

import java.time.{Duration, LocalDate}

object DateRangeValidationV1 {

  def validate(from: String, to: String): List[MtdError] = {
    val fmtFrom = LocalDate.parse(from, dateFormat)
    val fmtTo   = LocalDate.parse(to, dateFormat)

    List(
      checkIfToIsBeforeFrom(fmtFrom, fmtTo),
      checkIfFromIsTooEarly(fmtFrom),
      checkIfDateRangeIsTooLarge(fmtFrom, fmtTo)
    ).flatten
  }

  private def checkIfToIsBeforeFrom(from: LocalDate, to: LocalDate): List[MtdError] =
    if (to isBefore from) List(V1_RangeToDateBeforeFromDateError) else Nil

  private def checkIfFromIsTooEarly(from: LocalDate): List[MtdError] = if (from isBefore earliestDate) List(RuleFromDateNotSupportedError) else Nil

  private def checkIfDateRangeIsTooLarge(from: LocalDate, to: LocalDate): List[MtdError] = {
    val start = from.atStartOfDay()
    val end   = to.atStartOfDay()
    if (Duration.between(start, end.plusDays(1) /* add day to make inclusive */ ).toDays > maxDateRange)
      List(RuleDateRangeInvalidError)
    else Nil
  }

}
