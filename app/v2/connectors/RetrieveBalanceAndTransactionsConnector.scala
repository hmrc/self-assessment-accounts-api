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

import api.connectors.DownstreamUri.Ifs2Uri
import api.connectors.httpparsers.StandardDownstreamHttpParser.reads
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.models.request.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsRequestData
import v2.models.response.retrieveBalanceAndTransactions.{FinancialDetailsItem, RetrieveBalanceAndTransactionsResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveBalanceAndTransactionsConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieveBalanceAndTransactions(request: RetrieveBalanceAndTransactionsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveBalanceAndTransactionsResponse]] = {

    import request._

    val booleanQueryParams: Seq[(String, String)] =
      Seq(
        "onlyOpenItems"              -> onlyOpenItems,
        "includeLocks"               -> includeLocks,
        "calculateAccruedInterest"   -> calculateAccruedInterest,
        "removePOA"                  -> removePOA,
        "customerPaymentInformation" -> customerPaymentInformation,
        "includeStatistical"         -> includeEstimatedCharges
      ).map { case (k, v) => k -> v.toString }

    val optionalQueryParams: Seq[(String, String)] =
      Seq(
        "docNumber" -> docNumber,
        "dateFrom"  -> fromAndToDates.map(_.startDate.toString),
        "dateTo"    -> fromAndToDates.map(_.endDate.toString)
      ).collect { case (k, Some(v)) => k -> v }

    val queryParams = booleanQueryParams ++ optionalQueryParams

    // So that we don't read locks into result unless we've asked for them
    implicit val jsonReadLocks: FinancialDetailsItem.ReadLocks = FinancialDetailsItem.ReadLocks(request.includeLocks)

    get(Ifs2Uri[RetrieveBalanceAndTransactionsResponse](s"enterprise/02.00.00/financial-data/NINO/${nino.nino}/ITSA"), queryParams)
  }

}
