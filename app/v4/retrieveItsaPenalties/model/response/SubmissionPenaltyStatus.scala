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

import api.utils.enums.Enums
import play.api.libs.json.{Reads, Writes}

enum SubmissionPenaltyStatus(val fromDownstream: String) {
  case active   extends SubmissionPenaltyStatus("ACTIVE")
  case inactive extends SubmissionPenaltyStatus("INACTIVE")
}

object SubmissionPenaltyStatus {
  given Reads[SubmissionPenaltyStatus] = Enums.readsFrom[SubmissionPenaltyStatus](values, _.fromDownstream)

  given Writes[SubmissionPenaltyStatus] = Enums.writes[SubmissionPenaltyStatus]
}
