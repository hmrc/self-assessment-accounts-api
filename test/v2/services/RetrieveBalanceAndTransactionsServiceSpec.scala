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

import api.models.domain.{DateRange, Nino}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors, MtdError, _}
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v2.connectors.MockRetrieveBalanceAndTransactionsConnector
import v2.fixtures.retrieveBalanceAndTransactions.BalanceDetailsFixture.balanceDetails
import v2.fixtures.retrieveBalanceAndTransactions.CodingDetailsFixture.codingDetails
import v2.fixtures.retrieveBalanceAndTransactions.DocumentDetailsFixture.documentDetails
import v2.fixtures.retrieveBalanceAndTransactions.FinancialDetailsFixture.financialDetailsFull
import v2.models.request.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsRequestData
import v2.models.response.retrieveBalanceAndTransactions._

import java.time.LocalDate
import scala.concurrent.Future

class RetrieveBalanceAndTransactionsServiceSpec extends ServiceSpec {

  private val nino                       = "AA123456A"
  private val docNumber                  = "anId"
  private val dateFrom                   = "2018-08-13"
  private val dateTo                     = "2018-08-14"
  private val onlyOpenItems              = false
  private val includeLocks               = false
  private val calculateAccruedInterest   = false
  private val removePOA                  = false
  private val customerPaymentInformation = false
  private val includeStatistical         = false

  private val requestData: RetrieveBalanceAndTransactionsRequestData =
    RetrieveBalanceAndTransactionsRequestData(
      Nino(nino),
      Some(docNumber),
      Some(DateRange(LocalDate.parse(dateFrom), LocalDate.parse(dateTo))),
      onlyOpenItems,
      includeLocks,
      calculateAccruedInterest,
      removePOA,
      customerPaymentInformation,
      includeStatistical
    )

  val retrieveBalanceAndTransactionsResponse: RetrieveBalanceAndTransactionsResponse =
    RetrieveBalanceAndTransactionsResponse(
      balanceDetails = balanceDetails,
      codingDetails = Some(List(codingDetails)),
      documentDetails = Some(List(documentDetails)),
      financialDetails = Some(List(financialDetailsFull))
    )

  "RetrieveBalanceAndTransactionsService" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockRetrieveBalanceAndTransactionsConnector
          .retrieveBalanceAndTransactions(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveBalanceAndTransactionsResponse))))

        await(service.retrieveBalanceAndTransactions(requestData)) shouldBe Right(
          ResponseWrapper(correlationId, retrieveBalanceAndTransactionsResponse))
      }
    }

    "map errors according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockRetrieveBalanceAndTransactionsConnector
            .retrieveBalanceAndTransactions(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          private val result = await(service.retrieveBalanceAndTransactions(requestData))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors: Seq[(String, MtdError)] =
        List(
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

      errors.foreach(args => (serviceError _).tupled(args))
    }

  }

  trait Test extends MockRetrieveBalanceAndTransactionsConnector {

    val service = new RetrieveBalanceAndTransactionsService(
      connector = mockRetrieveBalanceAndTransactionsConnector
    )

  }

}
