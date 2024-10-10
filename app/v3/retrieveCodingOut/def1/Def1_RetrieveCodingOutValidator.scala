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

package v3.retrieveCodingOut.def1

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{DetailedResolveTaxYear, ResolveNino}
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits._
import config.AppConfig
import v3.common.resolvers
import v3.retrieveCodingOut.def1.model.request.Def1_RetrieveCodingOutRequestData
import v3.retrieveCodingOut.model.request.RetrieveCodingOutRequestData

import javax.inject.Singleton

@Singleton
class Def1_RetrieveCodingOutValidator(nino: String, taxYear: String, source: Option[String], appConfig: AppConfig)
  extends Validator[RetrieveCodingOutRequestData] {

      private val resolveTaxYear = DetailedResolveTaxYear(maybeMinimumTaxYear = Some(appConfig.minimumPermittedTaxYear))

      def validate: Validated[Seq[MtdError], RetrieveCodingOutRequestData] =
        (
          ResolveNino(nino),
          resolveTaxYear(taxYear),
          resolvers.ResolveSource(source)
        ).mapN(Def1_RetrieveCodingOutRequestData)

}
