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

sealed trait CodedOutStatus {
  def fromDownstream: String
}

object CodedOutStatus {

  implicit val reads: Reads[CodedOutStatus] = Enums
    .readsFrom[CodedOutStatus](_.fromDownstream)
    .orElse(
      Enums.reads[CodedOutStatus]
    )

  implicit val writes: Writes[CodedOutStatus] = Enums.writes[CodedOutStatus]

  case object `initiated` extends CodedOutStatus {
    override val fromDownstream: String = "I"
  }

  case object `not-collected` extends CodedOutStatus {
    override val fromDownstream: String = "N"
  }

  case object `partly-collected` extends CodedOutStatus {
    override val fromDownstream: String = "P"
  }

  case object `fully-collected` extends CodedOutStatus {
    override val fromDownstream: String = "F"
  }

  case object `awaiting-collection` extends CodedOutStatus {
    override val fromDownstream: String = "A"
  }

  case object `waiting-cancellation` extends CodedOutStatus {
    override val fromDownstream: String = "W"
  }

  case object `cancelled` extends CodedOutStatus {
    override val fromDownstream: String = "C"
  }

  case object `rejected` extends CodedOutStatus {
    override val fromDownstream: String = "R"
  }
}
