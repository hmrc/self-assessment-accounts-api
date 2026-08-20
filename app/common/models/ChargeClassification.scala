/*
 * Copyright 2026 HM Revenue & Customs
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

package common.models

import api.utils.enums.Enums
import play.api.libs.json.*

enum ChargeClassification(val fromDownstream: String) {
  case `enquiry-amendment`                  extends ChargeClassification("RA")
  case `auto-correction`                    extends ChargeClassification("AC")
  case `manual-correction`                  extends ChargeClassification("MC")
  case `customer-rejection-of-a-correction` extends ChargeClassification("RC")
}

object ChargeClassification {

  given Reads[ChargeClassification] = Enums.readsFrom[ChargeClassification](values, _.fromDownstream)

  given Writes[ChargeClassification] = Enums.writes[ChargeClassification]
}
