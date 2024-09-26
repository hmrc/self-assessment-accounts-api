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

package v3.listPaymentsAndAllocationDetails

import api.connectors.DownstreamUri.DesUri
import api.connectors.httpparsers.StandardDownstreamHttpParser._
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v3.listPaymentsAndAllocationDetails.model.request.ListPaymentsAndAllocationDetailsRequestData
import v3.listPaymentsAndAllocationDetails.model.response.ListPaymentsAndAllocationDetailsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListPaymentsAndAllocationDetailsConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def listPaymentsAndAllocationDetails(request: ListPaymentsAndAllocationDetailsRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[ListPaymentsAndAllocationDetailsResponse]] = {

    import request._
    import schema._

    val dateFrom = fromAndToDates.map(_.startDate.toString)
    val dateTo   = fromAndToDates.map(_.endDate.toString)

    def getIfExists(option: Option[String], name: String): Seq[(String, String)] = option match {
      case Some(x) => Seq(name -> x)
      case _       => Seq()
    }

    val queryParams: Seq[(String, String)] =
      getIfExists(dateFrom, "dateFrom") ++
        getIfExists(dateTo, "dateTo") ++
        getIfExists(paymentLot, "paymentLot") ++
        getIfExists(paymentLotItem, "paymentLotItem")

    get(DesUri[DownstreamResp](s"cross-regime/payment-allocation/NINO/$nino/ITSA"), queryParams)

  }

}