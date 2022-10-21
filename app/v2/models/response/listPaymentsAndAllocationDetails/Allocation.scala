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

package v2.models.response.listPaymentsAndAllocationDetails

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class Allocation(
    chargeReference: String,
    periodKey: Option[String],
    periodKeyDescription: Option[String],
    startDate: Option[String],
    endDate: Option[String],
    dueDate: Option[String],
    chargeDetail: Option[ChargeDetail],
    amount: Option[BigDecimal],
    clearedAmount: Option[BigDecimal],
    contractAccount: Option[String]
)

object Allocation {

  implicit val writes: Writes[Allocation] = Json.writes[Allocation]

  implicit val reads: Reads[Allocation] = (
    (JsPath \ "chargeReference").read[String] and
      (JsPath \ "periodKey").readNullable[String] and
      (JsPath \ "periodKeyDescription").readNullable[String] and
      (JsPath \ "taxPeriodStartDate").readNullable[String] and
      (JsPath \ "taxPeriodEndDate").readNullable[String] and
      (JsPath \ "dueDate").readNullable[String] and
      JsPath.readNullable[ChargeDetail] and
      (JsPath \ "amount").readNullable[BigDecimal] and
      (JsPath \ "clearedAmount").readNullable[BigDecimal] and
      (JsPath \ "contractAccount").readNullable[String]
  )(Allocation.apply _)

}
