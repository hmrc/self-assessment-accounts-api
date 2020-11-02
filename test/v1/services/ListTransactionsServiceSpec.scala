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

import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.fixtures.ListTransactionsFixture._
import v1.mocks.connectors.MockListTransactionsConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.response.listTransaction.ListTransactionsResponse

import scala.concurrent.Future

class ListTransactionsServiceSpec extends ServiceSpec {

  trait Test extends MockListTransactionsConnector {

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("controller", "listTransactions")

    val service = new ListTransactionsService(
      connector = mockListTransactionsConnector
    )
  }

  "listTransactions" should {
    "return a successful response" when {
      "received a valid response for the supplied request" in new Test {
        MockListTransactionsConnector.listTransactions(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, fullSingleListTransactionsModel))))

        await(service.listTransactions(requestData)) shouldBe Right(ResponseWrapper(correlationId, fullSingleListTransactionsModel))
      }
    }

    "return NoTransactionDetailsFoundError response" when {
      "the transactionItems are empty" in new Test {
        MockListTransactionsConnector.listTransactions(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ListTransactionsResponse(Seq())))))

        await(service.listTransactions(requestData)) shouldBe Left(ErrorWrapper(correlationId, NoTransactionsFoundError, None))
      }
    }

    "unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockListTransactionsConnector.listTransactions(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.listTransactions(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input: Seq[(String, MtdError)] = Seq(
          ("INVALID_IDTYPE", DownstreamError),
          ("INVALID_IDNUMBER", NinoFormatError),
          ("INVALID_REGIME_TYPE", DownstreamError),
          ("INVALID_DOC_NUMBER", DownstreamError),
          ("INVALID_ONLY_OPEN_ITEMS", DownstreamError),
          ("INVALID_INCLUDE_LOCKS", DownstreamError),
          ("INVALID_CALCULATE_ACCRUED_INTEREST", DownstreamError),
          ("INVALID_CUSTOMER_PAYMENT_INFORMATION", DownstreamError),
          ("INVALID_DATE_FROM", FromDateFormatError),
          ("INVALID_DATE_TO", ToDateFormatError),
          ("INVALID_REMOVE_PAYMENT_ON_ACCOUNT", DownstreamError),
          ("REQUEST_NOT_PROCESSED", DownstreamError),
          ("NO_DATA_FOUND", NotFoundError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}