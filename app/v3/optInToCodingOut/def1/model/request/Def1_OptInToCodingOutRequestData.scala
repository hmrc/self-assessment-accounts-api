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

package v3.optInToCodingOut.def1.model.request

import api.models.domain.{Nino, TaxYear}
import v3.optInToCodingOut.OptInToCodingOutSchema
import v3.optInToCodingOut.model.request.OptInToCodingOutRequestData

case class Def1_OptInToCodingOutRequestData(
                                             nino: Nino,
                                            taxYear: TaxYear
                                           ) extends OptInToCodingOutRequestData {

  override val schema: OptInToCodingOutSchema =  OptInToCodingOutSchema.Def1
}
