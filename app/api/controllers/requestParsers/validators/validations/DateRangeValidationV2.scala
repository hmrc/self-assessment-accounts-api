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

import api.models.errors.{InvalidDateRangeError, MtdError}

import java.time.LocalDate

object DateRangeValidationV2 {

  def validate(from: Option[String], to: Option[String]): List[MtdError] = {
    (from, to) match {
      case (Some(f), Some(t)) => checkIfToIsBeforeFrom(f, t)
      case (None, None)       => Nil
      case _                  => List(InvalidDateRangeError)
    }
  }

  private def checkIfToIsBeforeFrom(from: String, to: String): List[MtdError] = {
    val fmtFrom = LocalDate.parse(from, dateFormat)
    val fmtTo   = LocalDate.parse(to, dateFormat)
    if (fmtTo isBefore fmtFrom) List(InvalidDateRangeError) else Nil
  }

}
