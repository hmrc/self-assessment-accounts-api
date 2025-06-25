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

package v3.retrieveCodingOut

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v3.retrieveCodingOut.model.request.RetrieveCodingOutRequestData
import v3.retrieveCodingOut.model.response.RetrieveCodingOutResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveCodingOutConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def retrieveCodingOut(request: RetrieveCodingOutRequestData)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveCodingOutResponse]] = {

    import request._
    import schema._

    val queryParams = Seq("view" -> source).collect { case (key, Some(value)) =>
      key -> value.toDownstreamSource
    }

    val downstreamUri = if (taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[DownstreamResp](
        s"income-tax/accounts/self-assessment/collection/tax-code/${taxYear.asTysDownstream}/${nino.value}")
    } else {
      IfsUri[DownstreamResp](s"income-tax/accounts/self-assessment/collection/tax-code/${nino.value}/${taxYear.asMtd}")
    }

    get(uri = downstreamUri, queryParams = queryParams)
  }

}
