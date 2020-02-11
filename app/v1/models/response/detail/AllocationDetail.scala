/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.models.response.detail

import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import play.api.libs.functional.syntax._

case class AllocationDetail(id: String,
                            from: String,
                            to: String,
                            `type`: String,
                            amount: BigDecimal,
                            clearedAmount: BigDecimal)
object AllocationDetail {

  implicit val writes: OWrites[AllocationDetail] = Json.writes[AllocationDetail]

  implicit val reads: Reads[AllocationDetail] = (
    (JsPath \  "sapDocNumber").read[String] and
      (JsPath \ "taxPeriodStartDate").read[String] and
      (JsPath \ "taxPeriodEndDate").read[String] and
      (JsPath \ "chargeType").read[String] and
      (JsPath \ "amount").read[BigDecimal] and
      (JsPath \ "clearedAmount").read[BigDecimal]
    )(AllocationDetail.apply _)
}