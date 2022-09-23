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
import v2.connectors.RetrieveBalanceAndTransactionsConnector
import v2.models.request.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsRequest
import v2.models.response.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsResponse
import v2.support.DownstreamResponseMappingSupport

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveBalanceAndTransactionsService @Inject()(connector: RetrieveBalanceAndTransactionsConnector) extends
  DownstreamResponseMappingSupport with Logging {

  def retrieveBalanceAndTransactions(request: RetrieveBalanceAndTransactionsRequest)(implicit
                                                                                     hc: HeaderCarrier,
                                                                                     ec: ExecutionContext,
                                                                                     logContext: EndpointLogContext,
                                                                                     correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[RetrieveBalanceAndTransactionsResponse]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.retrieveBalanceAndTransactions(request)).leftMap(mapDownstreamErrors(downstreamErrorMap))
    } yield desResponseWrapper.map(des => des)

    result.value
  }

  private val downstreamErrorMap =
    Map(
      "INVALID_CORRELATIONID" -> InternalError,
      "INVALID_IDTYPE" -> InternalError,
      "INVALID_IDNUMBER" -> NinoFormatError,
      "INVALID_REGIME_TYPE" -> InternalError,
      "INVALID_DOC_NUMBER" -> InvalidDocNumberError,
      "INVALID_ONLY_OPEN_ITEMS" -> InvalidOnlyOpenItemsError,
      "INVALID_INCLUDE_LOCKS" -> InvalidIncludeLocksError,
      "INVALID_CALCULATE_ACCRUED_INTEREST" -> InvalidCalculateAccruedInterestError,
      "INVALID_CUSTOMER_PAYMENT_INFORMATION" -> InvalidCustomerPaymentInformationError,
      "INVALID_DATE_FROM" -> InvalidDateFromError,
      "INVALID_DATE_TO" -> InvalidDateToError,
      "INVALID_DATE_RANGE" -> InvalidDateRangeError,
      "INVALID_REQUEST" -> BadRequestError,
      "INVALID_REMOVE_PAYMENT_ON_ACCOUNT" -> InvalidRemovePaymentOnAccountError,
      "INVALID_INCLUDE_STATISTICAL" -> InvalidIncludeStatisticalError,
      "REQUEST_NOT_PROCESSED" -> InternalError,
      "NO_DATA_FOUND" -> NotFoundError,
      "SERVER_ERROR" -> InternalError,
      "SERVICE_UNAVAILABLE" -> InternalError
    )

}
