/*
 * Copyright 2020 HM Revenue & Customs
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

import cats.implicits._
import cats.data.EitherT
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import utils.Logging
import v1.connectors.RetrieveTransactionsConnector
import v1.controllers.EndpointLogContext
import v1.models.errors.{DownstreamError, ErrorWrapper, FromDateFormatError, MtdError, NinoFormatError, NotFoundError, ToDateFormatError}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveTransactions.RetrieveTransactionsParsedRequest
import v1.models.response.retrieveTransaction.{RetrieveTransactionsResponse, TransactionItem}
import v1.support.DesResponseMappingSupport

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveTransactionsService @Inject()(val connector: RetrieveTransactionsConnector) extends DesResponseMappingSupport with Logging {

  def list(request: RetrieveTransactionsParsedRequest)(
    implicit hc: HeaderCarrier,
    ec: ExecutionContext,
    logContext: EndpointLogContext): Future[Either[ErrorWrapper, ResponseWrapper[RetrieveTransactionsResponse[TransactionItem]]]] = {

    val result = for {
      desResponseWrapper <- EitherT(connector.retrieveTransactions(request)).leftMap(mapDesErrors(desErrorMap))
      mtdResponseWrapper <- EitherT.fromEither[Future](validateListTransactionsResponse(desResponseWrapper))
    } yield mtdResponseWrapper

    result.value
  }

  private def desErrorMap: Map[String, MtdError] =
    Map(
      "NO_TRANSACTIONS_FOUND" -> NotFoundError,
      "INVALID_IDTYPE" -> DownstreamError,
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_REGIME_TYPE" -> DownstreamError,
      "INVALID_DATE_FROM" -> FromDateFormatError,
      "INVALID_DATE_TO" -> ToDateFormatError,
      "NO_DATA_FOUND" -> NotFoundError,
      "SERVER_ERROR" -> DownstreamError,
      "SERVICE_UNAVAILABLE" -> DownstreamError,
    )
}