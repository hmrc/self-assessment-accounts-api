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
import v1.connectors.RetrieveBalanceConnector
import v1.models.request.retrieveBalance.RetrieveBalanceParsedRequest
import v1.models.response.retrieveBalance.RetrieveBalanceResponse
import v1.support.DownstreamResponseMappingSupport

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveBalanceService @Inject() (connector: RetrieveBalanceConnector) extends BaseService with DownstreamResponseMappingSupport with Logging {

  def retrieveBalance(request: RetrieveBalanceParsedRequest)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[Either[ErrorWrapper, ResponseWrapper[RetrieveBalanceResponse]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.retrieveBalance(request)).leftMap(mapDownstreamErrors(desErrorMap))
    } yield desResponseWrapper.map(des => des)

    result.value
  }

  private def desErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_IDTYPE"                       -> InternalError,
      "INVALID_IDNUMBER"                     -> NinoFormatError,
      "INVALID_REGIME_TYPE"                  -> InternalError,
      "INVALID_DOC_NUMBER"                   -> InternalError,
      "INVALID_ONLY_OPEN_ITEMS"              -> InternalError,
      "INVALID_INCLUDE_LOCKS"                -> InternalError,
      "INVALID_CALCULATE_ACCRUED_INTEREST"   -> InternalError,
      "INVALID_CUSTOMER_PAYMENT_INFORMATION" -> InternalError,
      "INVALID_DATE_FROM"                    -> InternalError,
      "INVALID_DATE_TO"                      -> InternalError,
      "INVALID_DATE_RANGE"                   -> InternalError,
      "INVALID_REQUEST"                      -> InternalError,
      "INVALID_INCLUDE_STATISTICAL"          -> InternalError,
      "INVALID_REMOVE_PAYMENT_ON_ACCOUNT"    -> InternalError,
      "REQUEST_NOT_PROCESSED"                -> InternalError,
      "NO_DATA_FOUND"                        -> NotFoundError,
      "SERVER_ERROR"                         -> InternalError,
      "SERVICE_UNAVAILABLE"                  -> InternalError
    )

}
