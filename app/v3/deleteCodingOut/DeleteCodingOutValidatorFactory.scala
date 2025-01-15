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

package v3.deleteCodingOut

import config.SaAccountsConfig
import shared.controllers.validators.Validator
import v3.deleteCodingOut.DeleteCodingOutSchema.Def1
import v3.deleteCodingOut.def1.Def1_DeleteCodingOutValidator
import v3.deleteCodingOut.model.request.DeleteCodingOutRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class DeleteCodingOutValidatorFactory @Inject() {

  def validator(nino: String, taxYear: String, appConfig: SaAccountsConfig): Validator[DeleteCodingOutRequestData] = {

    val schema = DeleteCodingOutSchema.schemaFor(taxYear)

    schema match {
      case Def1 => new Def1_DeleteCodingOutValidator(nino: String, taxYear: String, appConfig: SaAccountsConfig)
    }

  }

}
