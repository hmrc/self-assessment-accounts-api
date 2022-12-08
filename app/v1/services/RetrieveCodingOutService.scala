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

package v1.services

import api.controllers.RequestContext
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.BaseService
import cats.data.EitherT
import utils.{CurrentDate, Logging}
import v1.connectors.RetrieveCodingOutConnector
import v1.models.request.retrieveCodingOut.RetrieveCodingOutParsedRequest
import v1.models.response.retrieveCodingOut.RetrieveCodingOutResponse
import v1.support.DownstreamResponseMappingSupport

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveCodingOutService @Inject() (connector: RetrieveCodingOutConnector)(implicit currentDate: CurrentDate)
    extends BaseService
    with DownstreamResponseMappingSupport
    with Logging {

  def retrieveCodingOut(request: RetrieveCodingOutParsedRequest)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[Either[ErrorWrapper, ResponseWrapper[RetrieveCodingOutResponse]]] = {

    val result = for {
      downstreamResponseWrapper <- EitherT(connector.retrieveCodingOut(request)).leftMap(mapDownstreamErrors(downstreamErrorMap))
      mtdResponseWrapper        <- EitherT.fromEither[Future](validateCodingOutResponse(downstreamResponseWrapper, request.taxYear.asMtd))
    } yield mtdResponseWrapper

    result.value
  }

  private def downstreamErrorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_VIEW"              -> SourceFormatError,
      "INVALID_CORRELATIONID"     -> DownstreamError,
      "NO_DATA_FOUND"             -> CodingOutNotFoundError,
      "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
      "SERVER_ERROR"              -> DownstreamError,
      "SERVICE_UNAVAILABLE"       -> DownstreamError
    )

    val extraTysErrors = Map(
      "INVALID_CORRELATION_ID" -> DownstreamError,
      "NOT_FOUND"              -> NotFoundError
    )

    errors ++ extraTysErrors
  }

}
