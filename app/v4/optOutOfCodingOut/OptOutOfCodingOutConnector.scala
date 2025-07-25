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

package v4.optOutOfCodingOut

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.IfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser.reads
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import shared.models.domain.EmptyJsonBody
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v4.optOutOfCodingOut.model.request.OptOutOfCodingOutRequestData
import v4.optOutOfCodingOut.model.response.OptOutOfCodingOutResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class  OptOutOfCodingOutConnector @Inject() (val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def amendCodingOutOptOut(request: OptOutOfCodingOutRequestData)(implicit
                                                                  hc: HeaderCarrier,
                                                                  ec: ExecutionContext,
                                                                  correlationId: String): Future[DownstreamOutcome[OptOutOfCodingOutResponse]] = {

    import request._
    import schema._

    put(
      EmptyJsonBody,
      uri = IfsUri[DownstreamResp](s"income-tax/accounts/self-assessment/tax-code/opt-out/ITSA/${nino.value}/${taxYear.asDownstream}")
    )
  }

}
