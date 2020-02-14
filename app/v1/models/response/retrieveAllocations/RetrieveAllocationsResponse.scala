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

package v1.models.response.retrieveAllocations


import java.lang.management.MemoryType

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v1.models.response.retrieveAllocations.detail.AllocationDetail

case class RetrieveAllocationsResponse(amount: Option[BigDecimal],
                                       method: Option[String],
                                       transactionDate: Option[String],
                                       allocations: Option[Seq[AllocationDetail]])

object RetrieveAllocationsResponse {

  implicit val writes: OWrites[RetrieveAllocationsResponse] = Json.writes[RetrieveAllocationsResponse]

  implicit val reads: Reads[RetrieveAllocationsResponse] = (
      (JsPath \ "paymentDetails" \\ "paymentAmount").readNullable[BigDecimal] and
      (JsPath \ "paymentDetails" \\ "paymentMethod").readNullable[String] and
      (JsPath \ "paymentDetails" \\ "valueDate").readNullable[String] and
      (JsPath \ "paymentDetails" \\ "sapClearingDocsDetails").readNullable[Seq[AllocationDetail]]
        .map(_.map(_.filterNot((_ == AllocationDetail.emptyAllocation))))
        .map{
        case Some(Nil) => None
        case notEmpty => notEmpty
        }
    )(RetrieveAllocationsResponse.apply _)
}