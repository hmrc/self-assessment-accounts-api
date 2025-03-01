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

package v4.retrieveCodingOut

import cats.data.EitherT
import common.errors.{CodingOutNotFoundError, SourceFormatError}
import shared.controllers.RequestContext
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.BaseService
import v4.retrieveCodingOut.model.request.RetrieveCodingOutRequestData
import v4.retrieveCodingOut.model.response.RetrieveCodingOutResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveCodingOutService @Inject() (connector: RetrieveCodingOutConnector)
    extends BaseService
    with MappingSupportDownstream {

  def retrieveCodingOut(request: RetrieveCodingOutRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[Either[ErrorWrapper, ResponseWrapper[RetrieveCodingOutResponse]]] = {

    val result = for {
      downstreamResponseWrapper <- EitherT(connector.retrieveCodingOut(request)).leftMap(mapDownstreamErrors(errorMap))
      mtdResponseWrapper        <- EitherT.fromEither[Future](validateCodingOutResponse(downstreamResponseWrapper, request.taxYear))
    } yield mtdResponseWrapper

    result.value
  }

  private val errorMap: Map[String, MtdError] = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_VIEW"              -> SourceFormatError,
      "INVALID_CORRELATIONID"     -> InternalError,
      "NO_DATA_FOUND"             -> CodingOutNotFoundError,
      "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError
    )

    val extraTysErrors = Map(
      "INVALID_CORRELATION_ID" -> InternalError,
      "NOT_FOUND"              -> CodingOutNotFoundError
    )

    errors ++ extraTysErrors
  }

}
