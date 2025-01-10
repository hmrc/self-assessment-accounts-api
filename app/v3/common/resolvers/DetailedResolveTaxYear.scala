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

package v3.common.resolvers

import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import shared.models.domain.TaxYear
import shared.models.domain.TaxYear.currentTaxYear
import shared.models.errors._


case class DetailedResolveTaxYear(
    allowIncompleteTaxYear: Boolean = true,
    incompleteTaxYearError: MtdError = RuleTaxYearNotEndedError,
    maybeMinimumTaxYear: Option[Int] = None,
    minimumTaxYearError: MtdError = RuleTaxYearNotSupportedError
)
{


  def apply(value: String, maybeFormatError: Option[MtdError], errorPath: Option[String]): Validated[Seq[MtdError], TaxYear] = {

    def validateMinimumTaxYear(parsed: TaxYear): Validated[Seq[MtdError], Unit] =
      maybeMinimumTaxYear
        .traverse_ { minimumTaxYear =>
          if (parsed.year < minimumTaxYear) {
            Invalid(List(minimumTaxYearError.maybeWithExtraPath(errorPath)))
          } else {
            Valid(())
          }
        }

    def validateIncompleteTaxYear(parsed: TaxYear): Validated[Seq[MtdError], Unit] =
      if (allowIncompleteTaxYear || parsed.year < currentTaxYear().year) {
        Valid(())
      } else {
        Invalid(List(incompleteTaxYearError.maybeWithExtraPath(errorPath)))
      }

    resolve(value, maybeFormatError, errorPath)
      .andThen { parsed =>
        combine(
          validateMinimumTaxYear(parsed),
          validateIncompleteTaxYear(parsed)
        ).map(_ => parsed)
      }
  }
  private val taxYearFormat = "20[1-9][0-9]-[1-9][0-9]".r

  protected val rangeInvalidError: MtdError = RuleTaxYearRangeInvalidError

  protected def resolve(value: String, maybeFormatError: Option[MtdError], path: Option[String]): Validated[Seq[MtdError], TaxYear] =
    if (taxYearFormat.matches(value)) {
      val startTaxYearStart: Int = 2
      val startTaxYearEnd: Int   = 4

      val endTaxYearStart: Int = 5
      val endTaxYearEnd: Int   = 7

      val start = value.substring(startTaxYearStart, startTaxYearEnd).toInt
      val end   = value.substring(endTaxYearStart, endTaxYearEnd).toInt

      if (end - start == 1) {
        Valid(TaxYear.fromMtd(value))
      } else {
        Invalid(List(withError(None, rangeInvalidError, path)))
      }

    } else {
      Invalid(List(withError(maybeFormatError, TaxYearFormatError, path)))
    }
  protected def withError(maybeError: Option[MtdError], orDefault: MtdError, extraPath: Option[String]): MtdError =
    maybeError match {
      case Some(error) => error.maybeWithExtraPath(extraPath)
      case None        => orDefault.maybeWithExtraPath(extraPath)
    }

  protected def combine(results: Validated[Seq[MtdError], _]*): Validated[Seq[MtdError], Unit] =
    results.traverse_(identity)
}
