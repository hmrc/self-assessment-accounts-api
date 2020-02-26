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

package v1.models.response.retrieveBalance

import config.AppConfig
import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v1.hateoas.{HateoasLinks, HateoasLinksFactory}
import v1.models.hateoas.{HateoasData, Link}

case class RetrieveBalanceResponse (overdueAmount : Option[BigDecimal],
                                    payableAmount: BigDecimal,
                                    payableDueDate: Option[String],
                                    pendingChargeDueAmount: Option[BigDecimal],
                                    pendingChargeDueDate: Option[String])

object RetrieveBalanceResponse extends HateoasLinks {

  implicit val writes: OWrites[RetrieveBalanceResponse] = Json.writes[RetrieveBalanceResponse]

  implicit val reads: Reads[RetrieveBalanceResponse] = (
    (JsPath \  "overdueAmount").readNullable[BigDecimal] and
      (JsPath \ "payableAmount").read[BigDecimal] and
      (JsPath \ "payableDueDate").readNullable[String] and
      (JsPath \ "pendingChargeDueAmount").readNullable[BigDecimal] and
      (JsPath \ "pendingChargeDueDate").readNullable[String]
    )(RetrieveBalanceResponse.apply _)

  implicit object  RetrieveBalanceLinksFactory extends HateoasLinksFactory[RetrieveBalanceResponse, RetrieveBalanceHateoasData] {
    override def links(appConfig: AppConfig, data: RetrieveBalanceHateoasData): Seq[Link] = {
      import data._
      Seq(
        retrieveBalance(appConfig, nino, isSelf = true),
        retrieveTransactions(appConfig, nino, isSelf = false)
      )
    }
  }
}

case class RetrieveBalanceHateoasData(nino: String) extends HateoasData

