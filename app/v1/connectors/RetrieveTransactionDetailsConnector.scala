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

package v1.connectors

import api.connectors.DownstreamUri.DesUri
import api.connectors.httpparsers.StandardDownstreamHttpParser.reads
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.retrieveTransactionDetails.RetrieveTransactionDetailsParsedRequest
import v1.models.response.retrieveTransactionDetails.RetrieveTransactionDetailsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveTransactionDetailsConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieveTransactionDetails(request: RetrieveTransactionDetailsParsedRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveTransactionDetailsResponse]] = {

    val nino          = request.nino.nino
    val transactionId = request.transactionId

    val queryParams: Seq[(String, String)] =
      Seq(
        "docNumber"                  -> transactionId,
        "onlyOpenItems"              -> "false",
        "includeLocks"               -> "true",
        "calculateAccruedInterest"   -> "true",
        "removePOA"                  -> "false",
        "customerPaymentInformation" -> "true",
        "includeStatistical"         -> "false"
      )

    get(
      DesUri[RetrieveTransactionDetailsResponse](s"enterprise/02.00.00/financial-data/NINO/$nino/ITSA"),
      queryParams
    )
  }

}
