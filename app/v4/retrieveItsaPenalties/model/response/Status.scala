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

import play.api.libs.json.*
import shared.utils.enums.Enums

enum Status {
  case `under-appeal`, `appeal-upheld`, `appeal-rejected`, `cannot-be-appealed`
}

object Status {

  private val downstreamMap: Map[String, Status] = Map(
    "A"  -> `under-appeal`,
    "B"  -> `appeal-upheld`,
    "92" -> `appeal-upheld`,
    "93" -> `appeal-upheld`,
    "C"  -> `appeal-rejected`,
    "91" -> `appeal-rejected`,
    "94" -> `appeal-rejected`,
    "99" -> `cannot-be-appealed`
  )

  given Reads[Status] =
    Reads.StringReads.collect(JsonValidationError("error.expected.Status"))(downstreamMap)

  given Writes[Status] = Enums.writes[Status]

}
