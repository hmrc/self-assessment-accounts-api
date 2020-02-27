/*
 * Copyright 2019 HM Revenue & Customs
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

package v1.models.response.retrieveChargeHistory

import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import play.api.libs.functional.syntax._

case class ChargeHistory(taxYear: Option[String],
                         id: Option[String],
                         transactionDate: Option[String],
                         `type`: Option[String],
                         amount: Option[BigDecimal],
                         reversalDate: Option[String],
                         reversalReason: Option[String])

object ChargeHistory {
  implicit val reads: Reads[ChargeHistory] = (
    (JsPath \ "taxYear").readNullable[String] and
      (JsPath \ "id").readNullable[String] and
      (JsPath \ "transactionDate").readNullable[String] and
      (JsPath \ "type").readNullable[String] and
      (JsPath \ "amount").readNullable[BigDecimal] and
      (JsPath \ "reversalDate").readNullable[String] and
      (JsPath \ "reversalReason").readNullable[String]
  )(ChargeHistory.apply _)

  implicit val writes: OWrites[ChargeHistory] = Json.writes[ChargeHistory]

}
