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

package v4.retrieveBalanceAndTransactions

import common.errors._
import shared.models.domain.{DateRange, Nino}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.{ServiceOutcome, ServiceSpec}
import v4.retrieveBalanceAndTransactions.def1.model.BalanceDetailsFixture.balanceDetails
import v4.retrieveBalanceAndTransactions.def1.model.CodingDetailsFixture.codingDetails
import v4.retrieveBalanceAndTransactions.def1.model.DocumentDetailsFixture.{documentDetails, documentDetailsWithoutDocDueDate}
import v4.retrieveBalanceAndTransactions.def1.model.FinancialDetailsFixture.financialDetailsFull
import v4.retrieveBalanceAndTransactions.model.request.RetrieveBalanceAndTransactionsRequestData
import v4.retrieveBalanceAndTransactions.model.response.RetrieveBalanceAndTransactionsResponse

import java.time.LocalDate
import scala.concurrent.Future

class RetrieveBalanceAndTransactionsServiceSpec extends ServiceSpec {

  private val dateFrom           = LocalDate.parse("2018-08-13")
  private val dateTo             = LocalDate.parse("2018-08-14")
  private val includeStatistical = false

  private val requestData: RetrieveBalanceAndTransactionsRequestData =
    RetrieveBalanceAndTransactionsRequestData(
      Nino("AA123456A"),
      docNumber = Some("anId"),
      Some(DateRange(dateFrom -> dateTo)),
      onlyOpenItems = false,
      includeLocks = false,
      calculateAccruedInterest = false,
      removePOA = false,
      customerPaymentInformation = false,
      includeEstimatedCharges = includeStatistical
    )

  val retrieveBalanceAndTransactionsResponse: RetrieveBalanceAndTransactionsResponse =
    RetrieveBalanceAndTransactionsResponse(
      balanceDetails,
      Some(List(codingDetails)),
      Some(List(documentDetails, documentDetailsWithoutDocDueDate)),
      Some(List(financialDetailsFull))
    )

  "RetrieveBalanceAndTransactionsService" when {
    "the service call is successful" should {
      "return the mapped result" in new Test {
        MockedRetrieveBalanceAndTransactionsConnector
          .retrieveBalanceAndTransactions(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveBalanceAndTransactionsResponse))))

        val result: ServiceOutcome[RetrieveBalanceAndTransactionsResponse] = await(service.retrieveBalanceAndTransactions(requestData))
        result shouldBe Right(ResponseWrapper(correlationId, retrieveBalanceAndTransactionsResponse))
      }
    }

    "map errors according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockedRetrieveBalanceAndTransactionsConnector
            .retrieveBalanceAndTransactions(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          val result: ServiceOutcome[RetrieveBalanceAndTransactionsResponse] = await(service.retrieveBalanceAndTransactions(requestData))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors: Seq[(String, MtdError)] = {
        val ifsErrors =
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

        val hipErrors = Map(
          "002" -> InternalError,
          "003" -> InternalError,
          "005" -> NotFoundError,
          "015" -> InternalError
        )

        ifsErrors ++ hipErrors
      }

      errors.foreach((serviceError _).tupled)
    }
  }

  trait Test extends MockRetrieveBalanceAndTransactionsConnector {
    val service = new RetrieveBalanceAndTransactionsService(mockRetrieveBalanceAndTransactionsConnector)
  }

}
