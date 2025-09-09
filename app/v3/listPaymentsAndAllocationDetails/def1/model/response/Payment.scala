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

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class Payment(
    paymentLot: Option[String],
    paymentLotItem: Option[String],
    paymentReference: Option[String],
    paymentAmount: Option[BigDecimal],
    paymentMethod: Option[String],
    transactionDate: Option[String],
    allocations: List[Allocation]
)

object Payment {

  implicit val writes: Writes[Payment] = Json.writes[Payment]

  implicit val reads: Reads[Payment] = (
    (JsPath \ "paymentLot").readNullable[String] and
      (JsPath \ "paymentLotItem").readNullable[String] and
      (JsPath \ "paymentReference").readNullable[String] and
      (JsPath \ "paymentAmount").readNullable[BigDecimal] and
      (JsPath \ "paymentMethod").readNullable[String] and
      (JsPath \ "valueDate").readNullable[String] and
      (JsPath \ "sapClearingDocsDetails").readNullable[List[Allocation]].map(_.getOrElse(List()))
  )(Payment.apply)

}
