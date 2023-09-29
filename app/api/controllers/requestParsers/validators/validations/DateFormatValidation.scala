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

import api.models.errors.MtdError

import java.time.LocalDate
import scala.util.{Failure, Success, Try}

class DateFormatValidation(minYear: Int, maxYear: Int) {

  def validate(date: Option[String], isFromDate: Boolean, error: MtdError): List[MtdError] = date.map(validate(_, isFromDate, error)).getOrElse(Nil)

  def validate(date: String, isFromDate: Boolean, error: MtdError): List[MtdError] = Try {
    LocalDate.parse(date, dateFormat)
  } match {
    case Success(parsedDate) => validateFromAndToDate(parsedDate, isFromDate, error)
    case Failure(_)          => List(error)
  }

  private def validateFromAndToDate(date: LocalDate, isFromDate: Boolean, error: MtdError): List[MtdError] =
    if (isFromDate && date.getYear < minYear) {
      List(error)
    } else if (!isFromDate && date.getYear >= maxYear) {
      List(error)
    } else {
      NoValidationErrors
    }

}
