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

import api.connectors.BaseDownstreamConnector
import api.connectors.DownstreamUri.{IfsUri}
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.models.request.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsRequest
import v2.models.response.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveBalanceAndTransactionsConnector @Inject()(val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieveBalanceAndTransactions(request: RetrieveBalanceAndTransactionsRequest)(implicit
                                                                                     hc: HeaderCarrier,
                                                                                     ec: ExecutionContext,
                                                                                     correlationId: String): Future[DownstreamOutcome[RetrieveBalanceAndTransactionsResponse]] = {

    val nino = request.nino.nino
    val docNumber = request.docNumber
    val dateFrom = request.dateFrom
    val dateTo = request.dateTo
    val onlyOpenItems = request.onlyOpenItems
    val includeLocks = request.includeLocks
    val calculateAccruedInterest = request.calculateAccruedInterest
    val removePOA = request.removePOA
    val customerPaymentInformation = request.customerPaymentInformation
    val includeStatistical = request.includeStatistical

    val booleanQueryParams: Seq[(String, String)] =
      Seq(
        "onlyOpenItems"              -> onlyOpenItems.toString,
        "includeLocks"               -> includeLocks.toString,
        "calculateAccruedInterest"   -> calculateAccruedInterest.toString,
        "removePOA"                  -> removePOA.toString,
        "customerPaymentInformation" -> customerPaymentInformation.toString,
        "includeStatistical"         -> includeStatistical.toString
      )

    val queryParams = docNumber match {
      case Some(x) => Seq("docNumber" -> x) ++ booleanQueryParams
      case _ => Seq("dateFrom" -> dateFrom.get,
                    "dateTo" -> dateTo.get) ++ booleanQueryParams
    }
    get(IfsUri[RetrieveBalanceAndTransactionsResponse](s"enterprise/02.00.00/financial-data/NINO/$nino/ITSA"), queryParams)
  }

}
