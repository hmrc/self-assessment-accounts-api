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

package v3.optInToCodingOut.def1

import cats.data.Validated
import cats.implicits.catsSyntaxTuple2Semigroupal
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.{ResolveNino, ResolveTaxYear}
import shared.models.errors.MtdError
import v3.optInToCodingOut.def1.model.request.Def1_OptInToCodingOutRequestData
import v3.optInToCodingOut.model.request.OptInToCodingOutRequestData

import javax.inject.Singleton

@Singleton
class Def1_OptInToCodingOutValidator(nino: String, taxYear: String) extends Validator[OptInToCodingOutRequestData] {

  def validate: Validated[Seq[MtdError], OptInToCodingOutRequestData] =
    (
      ResolveNino(nino),
      ResolveTaxYear(taxYear)
    ).mapN(Def1_OptInToCodingOutRequestData.apply)

}
