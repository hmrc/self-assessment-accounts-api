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

package v4.retrieveBalanceAndTransactions.def1.model.response

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._
import shared.models.domain.TaxYear

case class CodingDetails(returnTaxYear: Option[String], totalLiabilityAmount: Option[BigDecimal], codingTaxYear: Option[String], coded: Option[Coded])

object CodingDetails {

  implicit val reads: Reads[CodingDetails] =
    (
      (JsPath \ "taxYearReturn").readNullable[String].map(_.map(TaxYear.fromDownstream(_).asMtd)) and
        (JsPath \ "totalLiabilityAmount").readNullable[BigDecimal] and
        (JsPath \ "taxYearCoding").readNullable[String].map(_.map(TaxYear.fromDownstream(_).asMtd)) and
        (JsPath \ "coded").readNullable[Coded]
    )(CodingDetails.apply _)

  implicit val writes: OWrites[CodingDetails] = Json.writes[CodingDetails]
}

case class Coded(charge: Option[BigDecimal], initiationDate: Option[String])

object Coded {

  implicit val reads: Reads[Coded] = (
    (JsPath \ "amount").readNullable[BigDecimal] and
      (JsPath \ "initiationDate").readNullable[String]
  )(Coded.apply _)

  implicit val writes: OWrites[Coded] = Json.writes[Coded]
}
