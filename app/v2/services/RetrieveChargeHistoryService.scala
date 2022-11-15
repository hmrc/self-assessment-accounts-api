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
import cats.implicits._
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v2.connectors.RetrieveChargeHistoryConnector
import v2.models.request.retrieveChargeHistory.RetrieveChargeHistoryRequest
import v2.models.response.retrieveChargeHistory.RetrieveChargeHistoryResponse
import v2.support.DownstreamResponseMappingSupport

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveChargeHistoryService @Inject() (connector: RetrieveChargeHistoryConnector) extends DownstreamResponseMappingSupport with Logging {

  def retrieveChargeHistory(request: RetrieveChargeHistoryRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[RetrieveChargeHistoryResponse]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.retrieveChargeHistory(request)).leftMap(mapDownstreamErrors(downstreamErrorMap))
    } yield desResponseWrapper.map(des => des)

    result.value
  }

  val downstreamErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_CORRELATIONID" -> StandardDownstreamError,
      "INVALID_IDTYPE"        -> StandardDownstreamError,
      "INVALID_IDVALUE"       -> NinoFormatError,
      "INVALID_REGIME_TYPE"   -> StandardDownstreamError,
      "INVALID_DOC_NUMBER"    -> TransactionIdFormatError,
      "INVALID_DATE_FROM"     -> StandardDownstreamError,
      "INVALID_DATE_TO"       -> StandardDownstreamError,
      "INVALID_DATE_RANGE"    -> StandardDownstreamError,
      "INVALID_REQUEST"       -> StandardDownstreamError,
      "REQUEST_NOT_PROCESSED" -> StandardDownstreamError,
      "NO_DATA_FOUND"         -> NotFoundError,
      "SERVER_ERROR"          -> StandardDownstreamError,
      "SERVICE_UNAVAILABLE"   -> StandardDownstreamError
    )

}
