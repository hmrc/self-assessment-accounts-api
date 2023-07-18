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

package v1.models.response.retrieveCodingOut

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class UnmatchedCustomerSubmissions(amount: BigDecimal, submittedOn: String, id: Option[BigInt])

object UnmatchedCustomerSubmissions {

  implicit val reads: Reads[UnmatchedCustomerSubmissions] = (
    (JsPath \ "amount").read[BigDecimal] and
      (JsPath \ "submittedOn").read[String] and
      (JsPath \ "componentIdentifier").readNullable[String].map(_.map(BigInt(_)))
  )(UnmatchedCustomerSubmissions.apply _)

  implicit val writes: OWrites[UnmatchedCustomerSubmissions] = Json.writes[UnmatchedCustomerSubmissions]
}
