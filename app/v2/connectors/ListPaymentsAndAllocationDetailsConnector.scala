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
import api.connectors.DownstreamUri.DesUri
import api.connectors.httpparsers.StandardDesHttpParser.reads
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.models.request.listPaymentsAndAllocationDetails.ListPaymentsAndAllocationDetailsRequest
import v2.models.response.listPaymentsAndAllocationDetails.ListPaymentsAndAllocationDetailsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListPaymentsAndAllocationDetailsConnector @Inject()(val http: HttpClient, val appConfig: AppConfig)
  extends BaseDownstreamConnector {

  def listPaymentsAndAllocationDetails(request: ListPaymentsAndAllocationDetailsRequest)(implicit
                                                                                     hc: HeaderCarrier,
                                                                                     ec: ExecutionContext,
                                                                                     correlationId: String): Future[DownstreamOutcome[ListPaymentsAndAllocationDetailsResponse]] = {

    val nino = request.nino.nino
    val dateFrom = request.dateFrom
    val dateTo = request.dateTo
    val paymentLot = request.paymentLot
    val paymentLotItem = request.paymentLotItem

    def getIfExists(option: Option[String], name: String): Seq[(String, String)] = option match {
      case Some(x) => Seq(name -> x)
      case _ => Seq()
    }

    val queryParams: Seq[(String, String)] =
      getIfExists(dateFrom, "dateFrom") ++
        getIfExists(dateTo, "dateTo") ++
        getIfExists(paymentLot, "paymentLot") ++
        getIfExists(paymentLotItem, "paymentLotItem")

    get(DesUri[ListPaymentsAndAllocationDetailsResponse](s"cross-regime/payment-allocation/NINO/$nino/ITSA"), queryParams)

  }

}
