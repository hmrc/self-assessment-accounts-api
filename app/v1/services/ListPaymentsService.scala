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
import cats.implicits._
import utils.Logging
import v1.connectors.ListPaymentsConnector
import v1.models.request.listPayments.ListPaymentsParsedRequest
import v1.models.response.listPayments.{ListPaymentsResponse, Payment}
import v1.support.DownstreamResponseMappingSupport

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListPaymentsService @Inject() (listPaymentsConnector: ListPaymentsConnector)
    extends BaseService
    with DownstreamResponseMappingSupport
    with Logging {

  def list(request: ListPaymentsParsedRequest)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[Either[ErrorWrapper, ResponseWrapper[ListPaymentsResponse[Payment]]]] = {

    val result = for {
      desResponseWrapper <- EitherT(listPaymentsConnector.listPayments(request)).leftMap(mapDownstreamErrors(desErrorMap))
    } yield desResponseWrapper

    result.value
  }

  private def desErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_IDTYPE"           -> DownstreamError,
      "INVALID_IDVALUE"          -> NinoFormatError,
      "INVALID_REGIME_TYPE"      -> DownstreamError,
      "INVALID_PAYMENT_LOT"      -> DownstreamError,
      "INVALID_PAYMENT_LOT_ITEM" -> DownstreamError,
      "INVALID_CLEARING_DOC"     -> DownstreamError,
      "INVALID_DATE_FROM"        -> V1_FromDateFormatError,
      "INVALID_DATE_TO"          -> V1_ToDateFormatError,
      "INVALID_DATE_RANGE"       -> DownstreamError,
      "INVALID_CORRELATIONID"    -> DownstreamError,
      "REQUEST_NOT_PROCESSED"    -> DownstreamError,
      "NO_DATA_FOUND"            -> NotFoundError,
      "PARTIALLY_MIGRATED"       -> DownstreamError,
      "SERVER_ERROR"             -> DownstreamError,
      "SERVICE_UNAVAILABLE"      -> DownstreamError
    )

}
