/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.models.domain

import play.api.libs.json.Format
import utils.enums.Enums

sealed trait MtdSource {
  def toDownstreamSource: String
}

object MtdSource {

  case object hmrcHeld extends MtdSource {
    override def toDownstreamSource: String = "HMRC-HELD"
  }

  case object user extends MtdSource {
    override def toDownstreamSource: String = "CUSTOMER"
  }

  case object latest extends MtdSource {
    override def toDownstreamSource: String = "LATEST"
  }

  implicit val format: Format[MtdSource]         = Enums.format[MtdSource]
  val parser: PartialFunction[String, MtdSource] = Enums.parser[MtdSource]
}
