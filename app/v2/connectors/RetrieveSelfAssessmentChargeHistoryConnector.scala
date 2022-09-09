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

package v2.connectors

import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import api.connectors.DownstreamUri.DesUri
import api.connectors.httpparsers.StandardDesHttpParser.reads
import api.connectors.BaseDownstreamConnector
import v2.models.request.retrieveSelfAssessmentChargeHistory.RetrieveSelfAssessmentChargeHistoryRequest
import v2.models.response.retrieveChargeHistory.RetrieveSelfAssessmentChargeHistoryResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveSelfAssessmentChargeHistoryConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieveChargeHistory(request: RetrieveSelfAssessmentChargeHistoryRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveSelfAssessmentChargeHistoryResponse]] = {

    val nino          = request.nino.nino
    val transactionId = request.transactionId

    val queryParams = Seq("docNumber" -> transactionId)

    get(DesUri[RetrieveSelfAssessmentChargeHistoryResponse](s"cross-regime/charges/NINO/$nino/ITSA"), queryParams)
  }

}
