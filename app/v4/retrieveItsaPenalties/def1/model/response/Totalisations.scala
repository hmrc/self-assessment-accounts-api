/*
 * Copyright 2026 HM Revenue & Customs
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

package v4.retrieveItsaPenalties.def1.model.response

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

case class Totalisations(
    lateSubmissionPenaltyTotalValue: BigDecimal,
    penalisedPrincipalTotal: BigDecimal,
    latePaymentPenaltyPostedTotal: BigDecimal,
    latePaymentPenaltyEstimateTotal: BigDecimal
)

object Totalisations {

  implicit val reads: Reads[Totalisations] = (
    (JsPath \ "lspTotalValue").read[BigDecimal] and
      (JsPath \ "penalisedPrincipalTotal").read[BigDecimal] and
      (JsPath \ "lppPostedTotal").read[BigDecimal] and
      (JsPath \ "lppEstimatedTotal").read[BigDecimal]
  )(Totalisations.apply)

  implicit val writes: OWrites[Totalisations] =
    Json.writes[Totalisations]

}
