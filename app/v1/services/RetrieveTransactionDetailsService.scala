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
import v1.connectors.RetrieveTransactionDetailsConnector
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import v1.models.request.retrieveTransactionDetails.RetrieveTransactionDetailsParsedRequest
import v1.models.response.retrieveTransactionDetails.RetrieveTransactionDetailsResponse
import v1.support.DownstreamResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveTransactionDetailsService @Inject() (val connector: RetrieveTransactionDetailsConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def retrieveTransactionDetails(request: RetrieveTransactionDetailsParsedRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[RetrieveTransactionDetailsResponse]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.retrieveTransactionDetails(request)).leftMap(mapDownstreamErrors(desErrorMap))
      mtdResponseWrapper <- EitherT.fromEither[Future](validateTransactionDetailsResponse(desResponseWrapper))
    } yield mtdResponseWrapper

    result.value
  }

  private def desErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_IDTYPE"                       -> DownstreamError,
      "INVALID_IDNUMBER"                     -> NinoFormatError,
      "INVALID_REGIME_TYPE"                  -> DownstreamError,
      "INVALID_DOC_NUMBER"                   -> TransactionIdFormatError,
      "INVALID_ONLY_OPEN_ITEMS"              -> DownstreamError,
      "INVALID_INCLUDE_LOCKS"                -> DownstreamError,
      "INVALID_CALCULATE_ACCRUED_INTEREST"   -> DownstreamError,
      "INVALID_CUSTOMER_PAYMENT_INFORMATION" -> DownstreamError,
      "INVALID_DATE_FROM"                    -> DownstreamError,
      "INVALID_DATE_TO"                      -> DownstreamError,
      "INVALID_DATE_RANGE"                   -> DownstreamError,
      "INVALID_REQUEST"                      -> DownstreamError,
      "INVALID_INCLUDE_STATISTICAL"          -> DownstreamError,
      "INVALID_REMOVE_PAYMENT_ON_ACCOUNT"    -> DownstreamError,
      "REQUEST_NOT_PROCESSED"                -> DownstreamError,
      "NO_DATA_FOUND"                        -> NotFoundError,
      "SERVER_ERROR"                         -> DownstreamError,
      "SERVICE_UNAVAILABLE"                  -> DownstreamError
    )

}
