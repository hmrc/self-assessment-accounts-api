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

package v4.listPaymentsAndAllocationDetails

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.DesUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v4.listPaymentsAndAllocationDetails.model.request.ListPaymentsAndAllocationDetailsRequestData
import v4.listPaymentsAndAllocationDetails.model.response.ListPaymentsAndAllocationDetailsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListPaymentsAndAllocationDetailsConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

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
