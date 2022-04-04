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

import play.api.libs.json
import utils.enums.Enums

sealed trait DownstreamSource {
  def toMtdSource: String
}

object DownstreamSource {

  case object `HMRC HELD` extends DownstreamSource {
    override def toMtdSource: String = "hmrcHeld"
  }

  case object `CUSTOMER` extends DownstreamSource {
    override def toMtdSource: String = "user"
  }

  implicit val format: json.Format[DownstreamSource] = Enums.format[DownstreamSource]
}
