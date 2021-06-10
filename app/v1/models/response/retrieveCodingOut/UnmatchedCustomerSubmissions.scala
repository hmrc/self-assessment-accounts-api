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

package v1.models.response.retrieveCodingOut

import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import play.api.libs.functional.syntax._

case class UnmatchedCustomerSubmissions(amount: BigDecimal,
                                        submittedOn: String,
                                        id: BigInt)

object UnmatchedCustomerSubmissions {
  implicit val reads: Reads[UnmatchedCustomerSubmissions] = (
    (JsPath \ "amount").read[BigDecimal] and
      (JsPath \ "submittedOn").read[String] and
      (JsPath \ "componentIdentifier").read[BigInt]
    ) (UnmatchedCustomerSubmissions.apply _)

  implicit val writes: OWrites[UnmatchedCustomerSubmissions] = Json.writes[UnmatchedCustomerSubmissions]
}
