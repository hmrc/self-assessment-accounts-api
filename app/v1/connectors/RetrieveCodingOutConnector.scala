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

package v1.connectors

import api.connectors.DownstreamUri.{Ifs1Uri, TaxYearSpecificIfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser.reads
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import api.models.domain.MtdSource
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v1.models.request.retrieveCodingOut.RetrieveCodingOutParsedRequest
import v1.models.response.retrieveCodingOut.RetrieveCodingOutResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveCodingOutConnector @Inject() (val http: HttpClient, val appConfig: AppConfig) extends BaseDownstreamConnector {

  def retrieveCodingOut(request: RetrieveCodingOutParsedRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      correlationId: String): Future[DownstreamOutcome[RetrieveCodingOutResponse]] = {

    import request._

    val queryParams = Seq("view" -> source).collect { case (key, Some(value)) =>
      key -> MtdSource.parser(value).toDownstreamSource
    }

    val downstreamUri = if (taxYear.useTaxYearSpecificApi) {
      TaxYearSpecificIfsUri[RetrieveCodingOutResponse](
        s"income-tax/accounts/self-assessment/collection/tax-code/${taxYear.asTysDownstream}/${nino.value}")
    } else {
      Ifs1Uri[RetrieveCodingOutResponse](s"income-tax/accounts/self-assessment/collection/tax-code/${nino.value}/${taxYear.asMtd}")
    }

    get(uri = downstreamUri, queryParams = queryParams)
  }

}
