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

package v4.optInToCodingOut

import shared.config.SharedAppConfig
import shared.connectors.DownstreamUri.IfsUri
import shared.connectors.httpparsers.StandardDownstreamHttpParser._
import shared.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import shared.models.domain.{Nino, TaxYear}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OptInToCodingOutConnector @Inject()(val http: HttpClient, val appConfig: SharedAppConfig) extends BaseDownstreamConnector {

  def optInToCodingOut(nino: Nino, taxYear: TaxYear)(implicit
                                                     hc: HeaderCarrier,
                                                     ec: ExecutionContext,
                                                     correlationId: String): Future[DownstreamOutcome[Unit]] = {

    delete(uri = IfsUri[Unit](s"income-tax/accounts/self-assessment/tax-code/opt-out/ITSA/${nino.value}/${taxYear.asDownstream}"))
  }

}
