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

package v1.connectors

import config.AppConfig
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.http.HttpClient
import v1.models.request.retrieveChargeHistory.RetrieveChargeHistoryParsedRequest
import v1.models.response.retrieveChargeHistory.RetrieveChargeHistoryResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveChargeHistoryConnector @Inject()(val http: HttpClient,
                                               val appConfig: AppConfig) extends BaseDesConnector {

  def retrieveChargeHistory(request: RetrieveChargeHistoryParsedRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[DesOutcome[RetrieveChargeHistoryResponse]] = {

    import v1.connectors.httpparsers.StandardDesHttpParser._

    val nino = request.nino.nino
    val chargeId = request.chargeId

    val queryParams = Seq("documentId" -> chargeId)

    get(
      uri = DesUri[RetrieveChargeHistoryResponse](s"cross-regime/charge-history-placeholder/NINO/$nino/ITSA"),
      queryParams = queryParams
    )
  }
}
