/*
 * Copyright 2025 HM Revenue & Customs
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

package v4.retrieveBalanceAndTransactions.def1.model.response

import play.api.libs.json.{Reads, Writes}
import shared.utils.enums.Enums

enum CodedOutStatus(val fromDownstream: String) {
  case initiated              extends CodedOutStatus("I")
  case `not-collected`        extends CodedOutStatus("N")
  case `partly-collected`     extends CodedOutStatus("P")
  case `fully-collected`      extends CodedOutStatus("F")
  case `awaiting-collection`  extends CodedOutStatus("A")
  case `waiting-cancellation` extends CodedOutStatus("W")
  case cancelled              extends CodedOutStatus("C")
  case rejected               extends CodedOutStatus("R")
}

object CodedOutStatus {

  given Reads[CodedOutStatus] = Enums.readsFrom[CodedOutStatus](values, _.fromDownstream)

  given Writes[CodedOutStatus] = Enums.writes
}
