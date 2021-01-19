/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.models.response.retrieveAllocations.detail

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class AllocationDetail(transactionId: Option[String],
                            from: Option[String],
                            to: Option[String],
                            `type`: Option[String],
                            amount: Option[BigDecimal],
                            clearedAmount: Option[BigDecimal])
object AllocationDetail {

 val emptyAllocation: AllocationDetail = AllocationDetail(None,None,None,None,None,None)

  implicit val writes: OWrites[AllocationDetail] = Json.writes[AllocationDetail]

  implicit val reads: Reads[AllocationDetail] = (
    (JsPath \  "sapDocNumber").readNullable[String] and
      (JsPath \ "taxPeriodStartDate").readNullable[String] and
      (JsPath \ "taxPeriodEndDate").readNullable[String] and
      (JsPath \ "chargeType").readNullable[String] and
      (JsPath \ "amount").readNullable[BigDecimal] and
      (JsPath \ "clearedAmount").readNullable[BigDecimal]
    )(AllocationDetail.apply _)
}