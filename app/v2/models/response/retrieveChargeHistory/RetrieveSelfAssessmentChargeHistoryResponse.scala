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

package v2.models.response.retrieveChargeHistory

import api.hateoas.HateoasLinksFactory
import api.models.hateoas.{HateoasData, Link}
import config.AppConfig
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v1.models.response.retrieveChargeHistory.RetrieveChargeHistoryResponse.{retrieveChargeHistory, retrieveTransactionDetails}

case class RetrieveSelfAssessmentChargeHistoryResponse(chargeHistoryDetails: Seq[ChargeHistoryDetail])

object RetrieveSelfAssessmentChargeHistoryResponse {

  implicit val reads: Reads[RetrieveSelfAssessmentChargeHistoryResponse] =
    (JsPath \ "chargeHistoryDetails")
      .read[Seq[ChargeHistoryDetail]]
      .map(items => RetrieveSelfAssessmentChargeHistoryResponse(items))

  implicit val writes: OWrites[RetrieveSelfAssessmentChargeHistoryResponse] =
    Json.writes[RetrieveSelfAssessmentChargeHistoryResponse]

  implicit object RetrieveSelfAssessmentChargeHistoryLinksFactory
      extends HateoasLinksFactory[RetrieveSelfAssessmentChargeHistoryResponse, RetrieveSelfAssessmentChargeHistoryHateoasData] {

    override def links(appConfig: AppConfig, data: RetrieveSelfAssessmentChargeHistoryHateoasData): Seq[Link] = {
      import data._
      Seq(
        retrieveChargeHistory(appConfig, nino, transactionId, isSelf = true),
        retrieveTransactionDetails(appConfig, nino, transactionId, isSelf = false)
      )
    }

  }

  case class RetrieveSelfAssessmentChargeHistoryHateoasData(nino: String, transactionId: String) extends HateoasData
}
