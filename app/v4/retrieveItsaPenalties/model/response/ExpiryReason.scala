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

package v4.retrieveItsaPenalties.model.response

import play.api.libs.json.{Reads, Writes}
import api.utils.enums.Enums

enum ExpiryReason(val fromDownstream: String) {
  case appeal                        extends ExpiryReason("APP")
  case `submission-frequency-change` extends ExpiryReason("FAP")
  case `obligations-reversed`        extends ExpiryReason("ICR")
  case `hmrc-removed`                extends ExpiryReason("MAN")
  case `natural-expiry`              extends ExpiryReason("NAT")
  case `penalty-removed`             extends ExpiryReason("NLT")
  case `expiry-conditions-met`       extends ExpiryReason("POC")
  case `hmrc-reset`                  extends ExpiryReason("RES")

}

object ExpiryReason {

  given Reads[ExpiryReason] = Enums.readsFrom[ExpiryReason](values, _.fromDownstream)

  given Writes[ExpiryReason] = Enums.writes[ExpiryReason]
}
