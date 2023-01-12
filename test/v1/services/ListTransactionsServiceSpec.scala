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

import api.models.domain.Nino
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v1.mocks.connectors.MockListTransactionsConnector
import v1.models.request.listTransactions.ListTransactionsParsedRequest
import v1.models.response.listTransaction.{ListTransactionsResponse, TransactionItem}

import scala.concurrent.Future

class ListTransactionsServiceSpec extends ServiceSpec {

  val nino = "AA123456A"
  val from = "2018-05-05"
  val to   = "2019-12-05"

  val requestData: ListTransactionsParsedRequest = ListTransactionsParsedRequest(
    nino = Nino(nino),
    from = from,
    to = to
  )

  val listTransactionsResponse: ListTransactionsResponse[TransactionItem] = ListTransactionsResponse[TransactionItem](
    transactions = Seq(
      TransactionItem(
        taxYear = "2019-20",
        transactionId = "X1234567890A",
        paymentId = Some("081203010024-000001"),
        transactionDate = "2020-01-01",
        `type` = Some("Balancing Charge Debit"),
        originalAmount = 12.34,
        outstandingAmount = 10.33,
        lastClearingDate = Some("2020-01-02"),
        lastClearingReason = Some("Incoming payment"),
        lastClearedAmount = Some(2.01),
        accruingInterestAmount = Some(8.31),
        interestRate = Some(2.06),
        interestFromDate = Some("2020-01-11"),
        interestEndDate = Some("2020-04-06"),
        latePaymentInterestAmount = Some(5.01),
        interestOutstandingAmount = Some(6.01)
      ))
  )

  trait Test extends MockListTransactionsConnector {

    val service = new ListTransactionsService(
      connector = mockListTransactionsConnector
    )

  }

  "listTransactions" should {
    "return a successful response" when {
      "received a valid response for the supplied request" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, listTransactionsResponse))

        MockListTransactionsConnector
          .listTransactions(requestData)
          .returns(Future.successful(outcome))

        await(service.listTransactions(requestData)) shouldBe outcome
      }
    }

    "unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockListTransactionsConnector
              .listTransactions(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

            await(service.listTransactions(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input: Seq[(String, MtdError)] = Seq(
          ("INVALID_IDTYPE", InternalError),
          ("INVALID_IDNUMBER", NinoFormatError),
          ("INVALID_REGIME_TYPE", InternalError),
          ("INVALID_DOC_NUMBER", InternalError),
          ("INVALID_ONLY_OPEN_ITEMS", InternalError),
          ("INVALID_INCLUDE_LOCKS", InternalError),
          ("INVALID_CALCULATE_ACCRUED_INTEREST", InternalError),
          ("INVALID_CUSTOMER_PAYMENT_INFORMATION", InternalError),
          ("INVALID_DATE_FROM", V1_FromDateFormatError),
          ("INVALID_DATE_TO", V1_ToDateFormatError),
          ("INVALID_DATE_RANGE", RuleDateRangeInvalidError),
          ("INVALID_REQUEST", InternalError),
          ("INVALID_REMOVE_PAYMENT_ON_ACCOUNT", InternalError),
          ("INVALID_INCLUDE_STATISTICAL", InternalError),
          ("REQUEST_NOT_PROCESSED", InternalError),
          ("NO_DATA_FOUND", NotFoundError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
