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

import play.api.libs.json._
import shared.utils.Logging

trait CodedOutStatus

object CodedOutStatus extends Logging {

  case object Initiated extends CodedOutStatus {
    override def toString: String = "initiated"
  }

  case object NotCollected extends CodedOutStatus {
    override def toString: String = "not-collected"
  }

  case object PartlyCollected extends CodedOutStatus {
    override def toString: String = "partly-collected"
  }

  case object FullyCollected extends CodedOutStatus {
    override def toString: String = "fully-collected"
  }

  case object AwaitingCollection extends CodedOutStatus {
    override def toString: String = "awaiting-collection"
  }

  case object WaitingCancellation extends CodedOutStatus {
    override def toString: String = "waiting-cancellation"
  }

  case object Cancelled extends CodedOutStatus {
    override def toString: String = "cancelled"
  }

  case object Rejected extends CodedOutStatus {
    override def toString: String = "rejected"
  }

  implicit val writes: Writes[CodedOutStatus] = Writes { (codedOutStatus: CodedOutStatus) =>
    JsString(codedOutStatus.toString)
  }

  implicit val reads: Reads[CodedOutStatus] = Reads { json =>
    json.as[String] match {
      case "I" | "i" => JsSuccess(Initiated)
      case "N" | "n" => JsSuccess(NotCollected)
      case "P" | "p" => JsSuccess(PartlyCollected)
      case "F" | "f" => JsSuccess(FullyCollected)
      case "A" | "a" => JsSuccess(AwaitingCollection)
      case "W" | "w" => JsSuccess(WaitingCancellation)
      case "C" | "c" => JsSuccess(Cancelled)
      case "R" | "r" => JsSuccess(Rejected)
      case other     =>
        logger.error(s"Unknown coded out status: $other")
        JsError(s"Unknown coded out status: $other")
    }
  }

}
