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

package v4.retrieveCodingOut

import config.SaAccountsConfig
import shared.controllers.validators.Validator
import v4.retrieveCodingOut.RetrieveCodingOutSchema.Def1
import v4.retrieveCodingOut.def1.Def1_RetrieveCodingOutValidator
import v4.retrieveCodingOut.model.request.RetrieveCodingOutRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class RetrieveCodingOutValidatorFactory @Inject() {

  def validator(nino: String, taxYear: String, source: Option[String], appConfig: SaAccountsConfig): Validator[RetrieveCodingOutRequestData] = {

    val schema = RetrieveCodingOutSchema.schemaFor(taxYear)

    schema match {
      case Def1 => new Def1_RetrieveCodingOutValidator(nino, taxYear, source, appConfig)
    }
  }

}
