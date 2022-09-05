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
import cats.data.EitherT
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v2.connectors.RetrieveSelfAssessmentChargeHistoryConnector
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import v2.models.request.retrieveSelfAssessmentChargeHistory.RetrieveSelfAssessmentChargeHistoryRequest
import v2.models.response.retrieveSelfAssessmentChargeHistory.RetrieveSelfAssessmentChargeHistoryResponse
import v2.support.DownstreamResponseMappingSupport

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveSelfAssessmentChargeHistoryService @Inject() (connector: RetrieveSelfAssessmentChargeHistoryConnector)
    extends DownstreamResponseMappingSupport
    with Logging {

  def retrieveChargeHistory(request: RetrieveSelfAssessmentChargeHistoryRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[RetrieveSelfAssessmentChargeHistoryResponse]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.retrieveChargeHistory(request)).leftMap(mapDownstreamErrors(downstreamErrorMap))
    } yield desResponseWrapper.map(des => des)

    result.value
  }

  private val downstreamErrorMap =
    Map(
      "INVALID_CORRELATIONID" -> InternalError,
      "INVALID_IDTYPE"        -> InternalError,
      "INVALID_IDVALUE"       -> NinoFormatError,
      "INVALID_REGIME_TYPE"   -> InternalError,
      "INVALID_DOC_NUMBER"    -> TransactionIdFormatError,
      "INVALID_DATE_FROM"     -> InternalError,
      "INVALID_DATE_TO"       -> InternalError,
      "INVALID_DATE_RANGE"    -> InternalError,
      "INVALID_REQUEST"       -> InternalError,
      "REQUEST_NOT_PROCESSED" -> InternalError,
      "NO_DATA_FOUND"         -> NotFoundError,
      "SERVER_ERROR"          -> InternalError,
      "SERVICE_UNAVAILABLE"   -> InternalError
    )

}
