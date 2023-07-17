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

package v1.services

import api.controllers.RequestContext
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.BaseService
import cats.data.EitherT
import cats.implicits._
import v1.connectors.RetrieveTransactionDetailsConnector
import v1.models.request.retrieveTransactionDetails.RetrieveTransactionDetailsParsedRequest
import v1.models.response.retrieveTransactionDetails.RetrieveTransactionDetailsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveTransactionDetailsService @Inject() (val connector: RetrieveTransactionDetailsConnector) extends BaseService {

  private val errorMap: Map[String, MtdError] =
    Map(
      "INVALID_IDTYPE"                       -> InternalError,
      "INVALID_IDNUMBER"                     -> NinoFormatError,
      "INVALID_REGIME_TYPE"                  -> InternalError,
      "INVALID_DOC_NUMBER"                   -> TransactionIdFormatError,
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

  def retrieveTransactionDetails(request: RetrieveTransactionDetailsParsedRequest)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[Either[ErrorWrapper, ResponseWrapper[RetrieveTransactionDetailsResponse]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.retrieveTransactionDetails(request)).leftMap(mapDownstreamErrors(errorMap))
      mtdResponseWrapper <- EitherT.fromEither[Future](validateTransactionDetailsResponse(desResponseWrapper))
    } yield mtdResponseWrapper

    result.value
  }

}
