/*
 * Copyright 2021 HM Revenue & Customs
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
import uk.gov.hmrc.http.HttpClient
import v1.connectors.DownstreamUri.IfsUri
import v1.models.request.createOrAmendCodingOut.CreateOrAmendCodingOutParsedRequest

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateOrAmendCodingOutConnector @Inject() (val http: HttpClient,
                                               val appConfig: AppConfig) extends BaseDownstreamConnector {

  import v1.connectors.httpparsers.StandardDesHttpParser._

  def amendCodingOut(request: CreateOrAmendCodingOutParsedRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    correlationId: String): Future[DownstreamOutcome[Unit]] = {

    val nino = request.nino
    val taxYear = request.taxYear

    put(
      request.body, IfsUri[Unit](s"income-tax/accounts/self-assessment/collection/tax-code/$nino/$taxYear")
    )
  }
}