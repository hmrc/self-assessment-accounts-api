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

package v3.deleteCodingOut

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.IfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v3.deleteCodingOut.model.request.DeleteCodingOutRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeleteCodingOutConnector @Inject()(val http: HttpClientV2, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def deleteCodingOut(request: DeleteCodingOutRequestData)(implicit
                                                           hc: HeaderCarrier,
                                                           ec: ExecutionContext,
                                                           correlationId: String): Future[DownstreamOutcome[Unit]] = {

    import request._

    val downstreamUri = if (taxYear.useTaxYearSpecificApi) {
      IfsUri[Unit](
        s"income-tax/${taxYear.asTysDownstream}/accounts/self-assessment/collection/tax-code/$nino"
      )
    } else {
      IfsUri[Unit](
        s"income-tax/accounts/self-assessment/collection/tax-code/$nino/${taxYear.asMtd}"
      )
    }

    delete(downstreamUri)
  }

}
