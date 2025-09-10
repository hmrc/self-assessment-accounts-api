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

package common.resolvers

import cats.data.Validated
import cats.data.Validated.cond
import shared.controllers.validators.resolvers.{ResolveTaxYear, ResolveTaxYearMinimum}
import shared.models.domain.TaxYear
import shared.models.domain.TaxYear.currentTaxYear
import shared.models.errors.*

case class DetailedResolveTaxYear(
    allowIncompleteTaxYear: Boolean = true,
    maybeMinimumTaxYear: Option[Int] = None
) {

  def apply(value: String): Validated[Seq[MtdError], TaxYear] = {

    val resolve: Validated[Seq[MtdError], TaxYear] =
      maybeMinimumTaxYear.fold(ResolveTaxYear(value))(minYear => ResolveTaxYearMinimum(TaxYear.ending(minYear))(value))

    resolve.andThen { parsed =>
      cond(allowIncompleteTaxYear || parsed.year < currentTaxYear.year, parsed, List(RuleTaxYearNotEndedError))
    }
  }

}
