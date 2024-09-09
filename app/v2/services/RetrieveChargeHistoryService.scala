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

package v2.services

import api.controllers.RequestContext
import api.models.errors._
import api.services.{BaseService, ServiceOutcome}
import cats.implicits._
import v2.connectors.RetrieveChargeHistoryConnector
import v2.models.request.retrieveChargeHistory.RetrieveChargeHistoryRequestData
import v2.models.response.retrieveChargeHistory.RetrieveChargeHistoryResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveChargeHistoryService @Inject() (connector: RetrieveChargeHistoryConnector) extends BaseService {

  def retrieveChargeHistory(request: RetrieveChargeHistoryRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[RetrieveChargeHistoryResponse]] = {

    connector
      .retrieveChargeHistory(request)
      .map(_.leftMap(mapDownstreamErrors(errorMap)))
  }

  private val errorMap: Map[String, MtdError] =
    Map(
      "INVALID_CORRELATIONID" -> InternalError,
      "INVALID_ID_TYPE"       -> InternalError,
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
