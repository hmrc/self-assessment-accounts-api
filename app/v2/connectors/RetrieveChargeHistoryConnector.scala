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

package v2.connectors

import api.connectors.DownstreamUri.{DesUri, Ifs1Uri}
import api.connectors.httpparsers.StandardDownstreamHttpParser.reads
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.models.request.retrieveChargeHistory.RetrieveChargeHistoryRequestData
import v2.models.response.retrieveChargeHistory.RetrieveChargeHistoryResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveChargeHistoryConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieveChargeHistory(request: RetrieveChargeHistoryRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveChargeHistoryResponse]] = {

    val nino            = request.nino.nino
    val transactionId   = request.transactionId
    val chargeReference = request.chargeReference

    val queryParams = if (chargeReference.isDefined && featureSwitches.isChargeReferencePoaAdjustmentChangesEnabled) {
      Seq("docNumber" -> transactionId.toString, "chargeReference" -> chargeReference.get.value)
    } else {
      Seq("docNumber" -> transactionId.toString)
    }

    if (featureSwitches.isChargeReferencePoaAdjustmentChangesEnabled) {
      get(Ifs1Uri[RetrieveChargeHistoryResponse](s"cross-regime/charges/NINO/$nino/ITSA"), queryParams)
    } else {
      get(DesUri[RetrieveChargeHistoryResponse](s"cross-regime/charges/NINO/$nino/ITSA"), queryParams)
    }
  }

}
