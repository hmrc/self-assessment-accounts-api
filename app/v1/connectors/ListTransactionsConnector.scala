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

package v1.connectors

import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import api.connectors.DownstreamUri.DesUri
import v1.models.request.listTransactions.ListTransactionsParsedRequest
import v1.models.response.listTransaction.{ListTransactionsResponse, TransactionItem}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListTransactionsConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def listTransactions(request: ListTransactionsParsedRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[ListTransactionsResponse[TransactionItem]]] = {

    val nino = request.nino.nino
    val from = request.from
    val to   = request.to

    val queryParams: Seq[(String, String)] = Seq(
      "dateFrom"                   -> from,
      "dateTo"                     -> to,
      "onlyOpenItems"              -> "false",
      "includeLocks"               -> "true",
      "calculateAccruedInterest"   -> "true",
      "removePOA"                  -> "false",
      "customerPaymentInformation" -> "true",
      "includeStatistical"         -> "false"
    )

    get(
      DesUri[ListTransactionsResponse[TransactionItem]](s"enterprise/02.00.00/financial-data/NINO/$nino/ITSA"),
      queryParams
    )
  }

}
