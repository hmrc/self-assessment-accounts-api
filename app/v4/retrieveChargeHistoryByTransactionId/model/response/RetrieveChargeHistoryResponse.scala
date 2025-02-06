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

package v4.retrieveChargeHistoryByTransactionId.model.response

import common.hateoas.HateoasLinks
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import shared.config.SharedAppConfig
import shared.hateoas.{HateoasData, HateoasLinksFactory, Link}
import v4.retrieveChargeHistoryByTransactionId.def1.models.response.ChargeHistoryDetail

case class RetrieveChargeHistoryResponse(chargeHistoryDetails: Seq[ChargeHistoryDetail])

object RetrieveChargeHistoryResponse extends HateoasLinks {

  implicit val reads: Reads[RetrieveChargeHistoryResponse] =
    (JsPath \ "chargeHistoryDetails")
      .read[Seq[ChargeHistoryDetail]]
      .map(items => RetrieveChargeHistoryResponse(items))

  implicit val writes: OWrites[RetrieveChargeHistoryResponse] = Json.writes[RetrieveChargeHistoryResponse]

  implicit object RetrieveChargeHistoryLinksFactory extends HateoasLinksFactory[RetrieveChargeHistoryResponse, RetrieveChargeHistoryHateoasData] {

    override def links(appConfig: SharedAppConfig, data: RetrieveChargeHistoryHateoasData): Seq[Link] = {
      import data._
      Seq(
        retrieveChargeHistory(appConfig, nino, transactionId, isSelf = true),
        retrieveTransactionDetails(appConfig, nino, transactionId, isSelf = false)
      )
    }

  }

  case class RetrieveChargeHistoryHateoasData(nino: String, transactionId: String) extends HateoasData
}
