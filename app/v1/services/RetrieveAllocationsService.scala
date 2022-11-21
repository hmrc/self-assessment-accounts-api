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

import api.controllers.EndpointLogContext
import cats.data.EitherT
import cats.implicits._

import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.RetrieveAllocationsConnector
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import v1.models.request.retrieveAllocations.RetrieveAllocationsParsedRequest
import v1.models.response.retrieveAllocations.RetrieveAllocationsResponse
import v1.models.response.retrieveAllocations.detail.AllocationDetail
import v1.support.DownstreamResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveAllocationsService @Inject() (connector: RetrieveAllocationsConnector) extends DownstreamResponseMappingSupport with Logging {

  def retrieveAllocations(request: RetrieveAllocationsParsedRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[RetrieveAllocationsResponse[AllocationDetail]]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.retrieveAllocations(request)).leftMap(mapDownstreamErrors(desErrorMap))
    } yield desResponseWrapper

    result.value
  }

  private def desErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_CORRELATIONID"    -> DownstreamError,
      "INVALID_IDTYPE"           -> DownstreamError,
      "INVALID_IDVALUE"          -> NinoFormatError,
      "INVALID_REGIME_TYPE"      -> DownstreamError,
      "INVALID_PAYMENT_LOT"      -> PaymentIdFormatError,
      "INVALID_PAYMENT_LOT_ITEM" -> PaymentIdFormatError,
      "INVALID_CLEARING_DOC"     -> DownstreamError,
      "INVALID_DATE_FROM"        -> DownstreamError,
      "INVALID_DATE_TO"          -> DownstreamError,
      "REQUEST_NOT_PROCESSED"    -> DownstreamError,
      "PARTIALLY_MIGRATED"       -> DownstreamError,
      "NO_DATA_FOUND"            -> NotFoundError,
      "SERVER_ERROR"             -> DownstreamError,
      "SERVICE_UNAVAILABLE"      -> DownstreamError
    )

}
