/*
 * Copyright 2025 HM Revenue & Customs
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

import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import shared.config.SharedAppConfig
import v4.retrieveChargeHistoryByTransactionId.def1.models.response.ChargeHistoryDetail

case class RetrieveChargeHistoryResponse(chargeHistoryDetails: Seq[ChargeHistoryDetail])

object RetrieveChargeHistoryResponse {

  implicit val reads: Reads[RetrieveChargeHistoryResponse] = {

    val ifsReads: Reads[RetrieveChargeHistoryResponse] = (JsPath \ "chargeHistoryDetails")
      .read[Seq[ChargeHistoryDetail]]
      .map(items => RetrieveChargeHistoryResponse(items))

    val hipReads: Reads[RetrieveChargeHistoryResponse] = (JsPath \ "success" \ "chargeHistoryDetails" \ "chargeHistory")
      .read[Seq[ChargeHistoryDetail]]
      .map(items => RetrieveChargeHistoryResponse(items))

    hipReads orElse ifsReads
  }

  implicit def writes(implicit appConfig: SharedAppConfig): OWrites[RetrieveChargeHistoryResponse] = Json.writes[RetrieveChargeHistoryResponse]
}
