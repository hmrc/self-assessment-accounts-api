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

package v2.services

import api.controllers.EndpointLogContext
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import cats.data.EitherT
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v2.connectors.ListPaymentsAndAllocationDetailsConnector
import v2.models.request.listPaymentsAndAllocationDetails.ListPaymentsAndAllocationDetailsRequest
import v2.models.response.listPaymentsAndAllocationDetails.ListPaymentsAndAllocationDetailsResponse
import v2.support.DownstreamResponseMappingSupport

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListPaymentsAndAllocationDetailsService @Inject() (connector: ListPaymentsAndAllocationDetailsConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def listPaymentsAndAllocationDetails(request: ListPaymentsAndAllocationDetailsRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[ListPaymentsAndAllocationDetailsResponse]]] = {

    val result = EitherT(connector.listPaymentsAndAllocationDetails(request)).leftMap(mapDownstreamErrors(downstreamErrorMap))

    result.value
  }

  val downstreamErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_CORRELATIONID"    -> InternalError,
      "INVALID_IDVALUE"          -> NinoFormatError,
      "INVALID_IDTYPE"           -> InternalError,
      "INVALID_REGIME_TYPE"      -> InternalError,
      "INVALID_PAYMENT_LOT"      -> PaymentLotFormatError,
      "INVALID_PAYMENT_LOT_ITEM" -> PaymentLotItemFormatError,
      "INVALID_CLEARING_DOC"     -> InternalError,
      "INVALID_DATE_FROM"        -> V2_FromDateFormatError,
      "INVALID_DATE_TO"          -> V2_ToDateFormatError,
      "INVALID_DATE_RANGE"       -> RuleInvalidDateRangeError,
      "REQUEST_NOT_PROCESSED"    -> InternalError,
      "NO_DATA_FOUND"            -> NotFoundError,
      "PARTIALLY_MIGRATED"       -> InternalError,
      "SERVER_ERROR"             -> InternalError,
      "SERVICE_UNAVAILABLE"      -> InternalError
    )

}
