/*
 * Copyright 2023 HM Revenue & Customs
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

package v3.listPaymentsAndAllocationDetails.def1.model.response

import play.api.libs.json.{JsPath, Json, Reads, Writes}
import v3.listPaymentsAndAllocationDetails.model.response.ListPaymentsAndAllocationDetailsResponse

case class Def1_ListPaymentsAndAllocationDetailsResponse(
    payments: List[Payment]
) extends ListPaymentsAndAllocationDetailsResponse

object Def1_ListPaymentsAndAllocationDetailsResponse {
  implicit val writes: Writes[Def1_ListPaymentsAndAllocationDetailsResponse] = Json.writes[Def1_ListPaymentsAndAllocationDetailsResponse]

  implicit val reads: Reads[Def1_ListPaymentsAndAllocationDetailsResponse] =
    (JsPath \ "paymentDetails").read[List[Payment]].map(Def1_ListPaymentsAndAllocationDetailsResponse.apply)

}
