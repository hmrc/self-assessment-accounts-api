/*
 * Copyright 2021 HM Revenue & Customs
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

import cats.data.EitherT
import cats.implicits._
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.ListTransactionsConnector
import v1.controllers.EndpointLogContext
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.listTransactions.ListTransactionsParsedRequest
import v1.models.response.listTransaction.{ListTransactionsResponse, TransactionItem}
import v1.support.DesResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListTransactionsService @Inject()(val connector: ListTransactionsConnector)
  extends DesResponseMappingSupport with Logging {

  def listTransactions(request: ListTransactionsParsedRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    logContext: EndpointLogContext,
    correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[ListTransactionsResponse[TransactionItem]]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.listTransactions(request)).leftMap(mapDesErrors(desErrorMap))
    } yield desResponseWrapper

    result.value
  }

  private def desErrorMap: Map[String, MtdError] =
    Map(
      "INVALID_IDTYPE" -> DownstreamError,
      "INVALID_IDNUMBER" -> NinoFormatError,
      "INVALID_REGIME_TYPE" -> DownstreamError,
      "INVALID_DOC_NUMBER" -> DownstreamError,
      "INVALID_ONLY_OPEN_ITEMS" -> DownstreamError,
      "INVALID_INCLUDE_LOCKS" -> DownstreamError,
      "INVALID_CALCULATE_ACCRUED_INTEREST" -> DownstreamError,
      "INVALID_CUSTOMER_PAYMENT_INFORMATION" -> DownstreamError,
      "INVALID_DATE_FROM" -> FromDateFormatError,
      "INVALID_DATE_TO" -> ToDateFormatError,
      "INVALID_DATE_RANGE" -> RuleDateRangeInvalidError,
      "INVALID_REQUEST" -> DownstreamError,
      "INVALID_REMOVE_PAYMENT_ON_ACCOUNT" -> DownstreamError,
      "INVALID_INCLUDE_STATISTICAL" -> DownstreamError,
      "REQUEST_NOT_PROCESSED" -> DownstreamError,
      "NO_DATA_FOUND" -> NotFoundError,
      "SERVER_ERROR" -> DownstreamError,
      "SERVICE_UNAVAILABLE" -> DownstreamError
    )
}