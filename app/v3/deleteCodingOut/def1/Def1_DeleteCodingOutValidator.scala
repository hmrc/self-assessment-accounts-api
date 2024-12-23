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

package v3.deleteCodingOut.def1

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{DetailedResolveTaxYear, ResolveNino}
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits._
import config.AppConfig
import v3.deleteCodingOut.def1.model.request.Def1_DeleteCodingOutRequestData
import v3.deleteCodingOut.model.request.DeleteCodingOutRequestData

import javax.inject.Singleton

@Singleton
class Def1_DeleteCodingOutValidator(nino: String, taxYear: String, appConfig: AppConfig) extends Validator[DeleteCodingOutRequestData] {

      private val resolveTaxYear =
        DetailedResolveTaxYear(maybeMinimumTaxYear = Some(appConfig.minimumPermittedTaxYear))

      def validate: Validated[Seq[MtdError], DeleteCodingOutRequestData] =
        (
          ResolveNino(nino),
          resolveTaxYear(taxYear)
        ).mapN(Def1_DeleteCodingOutRequestData)

}
