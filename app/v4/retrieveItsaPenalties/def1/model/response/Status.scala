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

package v4.retrieveItsaPenalties.def1.model.response

import play.api.libs.json.{Reads, Writes}
import shared.utils.enums.Enums

enum Status {
  case `under-appeal`
  case `appeal-upheld`
  case `appeal-rejected`
  case `cannot-be-appealed`
}

//enum Status(val fromDownstream: String) {
//  case `under-appeal`       extends Status("A")
//  case `appeal-upheld`      extends Status("B")
//  case `appeal-rejected`    extends Status("C")
//  case `cannot-be-appealed` extends Status("99")
//  case `appeal-rejected`    extends Status("91")
//  case `appeal-upheld`      extends Status("92")
//  case `appeal-upheld`      extends Status("93")
//  case `appeal-rejected`    extends Status("94")
//
//}

object Status {

  private val downstreamMap: Map[String, Status] = Map(
    "A"  -> Status.`under-appeal`,
    "B"  -> Status.`appeal-upheld`,
    "92" -> Status.`appeal-upheld`,
    "93" -> Status.`appeal-upheld`,
    "C"  -> Status.`appeal-rejected`,
    "91" -> Status.`appeal-rejected`,
    "94" -> Status.`appeal-rejected`,
    "99" -> Status.`cannot-be-appealed`
  )

  given Reads[Status] =
    Reads.StringReads.map(downstreamMap)

  given Writes[Status] = Enums.writes
}
