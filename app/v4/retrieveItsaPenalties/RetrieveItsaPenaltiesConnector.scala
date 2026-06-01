/*
 * Copyright 2026 HM Revenue & Customs
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

package v4.retrieveItsaPenalties

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.HipUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v4.retrieveItsaPenalties.model.request.RetrieveItsaPenaltiesRequestData
import v4.retrieveItsaPenalties.model.response.RetrieveItsaPenaltiesResponse
import shared.utils.DateUtils.isoDateTimeStamp

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveItsaPenaltiesConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def retrieveItsaPenalties(request: RetrieveItsaPenaltiesRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveItsaPenaltiesResponse]] = {

    import request.*

    val hipRequiredQueryParams: Seq[(String, String)] =
      List(
        "taxRegime" -> "ITSA",
        "idType"    -> "NINO",
        "idNumber"  -> nino.nino
      )

    val additionalContractHeaders: Seq[(String, String)] = List(
      "X-Originating-System"  -> "MDTP",
      "X-Receipt-Date"        -> isoDateTimeStamp,
      "X-Transmitting-System" -> "HIP"
    )

    val (queryParams, downstreamUri) = (
      hipRequiredQueryParams,
      HipUri[RetrieveItsaPenaltiesResponse](
        path = "etmp/RESTAdapter/cross-regime/taxpayer/penalties",
        additionalContractHeaders = additionalContractHeaders))

    get(downstreamUri, queryParams)
  }

}
