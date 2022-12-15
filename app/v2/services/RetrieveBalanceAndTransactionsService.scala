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

import api.controllers.RequestContext
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.BaseService
import cats.implicits._
import v2.connectors.RetrieveBalanceAndTransactionsConnector
import v2.models.request.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsRequest
import v2.models.response.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveBalanceAndTransactionsService @Inject() (connector: RetrieveBalanceAndTransactionsConnector) extends BaseService {

  def retrieveBalanceAndTransactions(request: RetrieveBalanceAndTransactionsRequest)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[Either[ErrorWrapper, ResponseWrapper[RetrieveBalanceAndTransactionsResponse]]] = {

    connector
      .retrieveBalanceAndTransactions(request)
      .map(_.leftMap(mapDownstreamErrors(errorMap)))
  }

  private val errorMap: Map[String, MtdError] =
    Map(
      "INVALID_CORRELATIONID"                -> InternalError,
      "INVALID_IDTYPE"                       -> InternalError,
      "INVALID_IDNUMBER"                     -> NinoFormatError,
      "INVALID_REGIME_TYPE"                  -> InternalError,
      "INVALID_DOC_NUMBER"                   -> DocNumberFormatError,
      "INVALID_ONLY_OPEN_ITEMS"              -> OnlyOpenItemsFormatError,
      "INVALID_INCLUDE_LOCKS"                -> IncludeLocksFormatError,
      "INVALID_CALCULATE_ACCRUED_INTEREST"   -> CalculateAccruedInterestFormatError,
      "INVALID_CUSTOMER_PAYMENT_INFORMATION" -> CustomerPaymentInformationFormatError,
      "INVALID_DATE_FROM"                    -> FromDateFormatError,
      "INVALID_DATE_TO"                      -> ToDateFormatError,
      "INVALID_DATE_RANGE"                   -> RuleInvalidDateRangeError,
      "INVALID_REQUEST"                      -> RuleInconsistentQueryParamsError,
      "INVALID_REMOVE_PAYMENT_ON_ACCOUNT"    -> RemovePaymentOnAccountFormatError,
      "INVALID_INCLUDE_STATISTICAL"          -> IncludeEstimatedChargesFormatError,
      "REQUEST_NOT_PROCESSED"                -> InternalError,
      "NO_DATA_FOUND"                        -> NotFoundError,
      "SERVER_ERROR"                         -> InternalError,
      "SERVICE_UNAVAILABLE"                  -> InternalError
    )

}
