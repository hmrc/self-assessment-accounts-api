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

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class PaymentDetails(amount: BigDecimal,
                          method: String,
                          transactionDate: String,
                          allocations: Seq[AllocationDetail])

object PaymentDetails {

  implicit val writes: OWrites[PaymentDetails] = Json.writes[PaymentDetails]

  implicit val reads: Reads[PaymentDetails] = (
      (JsPath \ "paymentAmount").read[BigDecimal] and
      (JsPath \ "paymentMethod").read[String] and
      (JsPath \ "valueDate").read[String] and
      (JsPath \ "sapClearingDocsDetails").read[Seq[AllocationDetail]]
    )(PaymentDetails.apply _)
}