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

package v1.models.response.retrieveBalance

import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

case class RetrieveBalanceResponse(overdueAmount: BigDecimal,
                                   payableAmount: BigDecimal,
                                   payableDueDate: Option[String],
                                   pendingChargeDueAmount: BigDecimal,
                                   pendingChargeDueDate: Option[String],
                                   totalBalance: BigDecimal)

object RetrieveBalanceResponse extends HateoasLinks {

  implicit val reads: Reads[RetrieveBalanceResponse] = (
    (JsPath \ "balanceDetails" \ "overDueAmount").read[BigDecimal] and
      (JsPath \ "balanceDetails" \ "balanceDueWithin30Days").read[BigDecimal] and
      (JsPath \ "balanceDetails" \ "nextPaymentDateForChargesDueIn30Days").readNullable[String] and
      (JsPath \ "balanceDetails" \ "balanceNotDueIn30Days").read[BigDecimal] and
      (JsPath \ "balanceDetails" \ "nextPaymentDateBalanceNotDue").readNullable[String] and
      (JsPath \ "balanceDetails" \ "totalBalance").read[BigDecimal]
    ) (RetrieveBalanceResponse.apply _)

  implicit val writes: OWrites[RetrieveBalanceResponse] =
    Json.writes[RetrieveBalanceResponse]

  implicit object RetrieveBalanceLinksFactory extends HateoasLinksFactory[RetrieveBalanceResponse, RetrieveBalanceHateoasData] {
    override def links(appConfig: AppConfig, data: RetrieveBalanceHateoasData): Seq[Link] =
      Seq(
        retrieveBalance(appConfig, data.nino, isSelf = true)
      )
  }

}

case class RetrieveBalanceHateoasData(nino: String) extends HateoasData

