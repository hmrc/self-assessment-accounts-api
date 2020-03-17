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
import v1.models.request.retrieveAllocations.RetrieveAllocationsParsedRequest
import v1.models.response.retrieveAllocations.RetrieveAllocationsResponse
import v1.models.response.retrieveAllocations.detail.AllocationDetail

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveAllocationsConnector @Inject()(val http: HttpClient,
                                             val appConfig: AppConfig) extends BaseDesConnector {

  def retrieveAllocations(request: RetrieveAllocationsParsedRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext): Future[DesOutcome[RetrieveAllocationsResponse[AllocationDetail]]] = {

    import v1.connectors.httpparsers.StandardDesHttpParser._

    val nino = request.nino.nino

    val queryParams: Seq[(String, String)] =
      Seq(
        "paymentLot" -> request.paymentLot,
        "paymentLotItem" -> request.paymentLotItem
      )

    get(
      uri = DesUri[RetrieveAllocationsResponse[AllocationDetail]](s"cross-regime/payment-allocation/NINO/$nino/ITSA"),
      queryParams = queryParams
    )
  }
}
