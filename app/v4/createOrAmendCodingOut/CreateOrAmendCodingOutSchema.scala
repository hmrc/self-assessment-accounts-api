/*
 * Copyright 2024 HM Revenue & Customs
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

package v4.createOrAmendCodingOut

import shared.controllers.validators.resolvers.ResolveTaxYear
import shared.models.domain.TaxYear

sealed trait CreateOrAmendCodingOutSchema

object CreateOrAmendCodingOutSchema {

  case object Def1 extends CreateOrAmendCodingOutSchema

  private val defaultSchema = Def1

  def schemaFor(maybeTaxYear: String): CreateOrAmendCodingOutSchema = {
    ResolveTaxYear(maybeTaxYear)
      .map(schemaFor)
      .getOrElse(defaultSchema)
  }

  def schemaFor(taxYear: TaxYear): CreateOrAmendCodingOutSchema = {
      Def1
  }

}
