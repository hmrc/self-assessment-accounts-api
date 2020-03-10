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

import support.UnitSpec
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockRetrieveTransactionsConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.fixtures.RetrieveTransactionFixture._
import v1.models.response.retrieveTransaction.RetrieveTransactionsResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveTransactionsServiceSpec extends UnitSpec {

  private val correlationId = "X-123"

  trait Test extends MockRetrieveTransactionsConnector {

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("controller", "retrieveTransactions")

    val service = new RetrieveTransactionsService(
      connector = mockRetrieveTransactionsConnector
    )
  }

  "retrieveDetails" should {
    "return a successful response" when {
      "received a valid response for the supplied request" in new Test {

        MockRetrieveTransactionsConnector.retrieveTransactions(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, fullSingleRetreiveTransactionModel))))

        await(service.list(requestData)) shouldBe Right(ResponseWrapper(correlationId, fullSingleRetreiveTransactionModel))
      }
    }

    "return NoTransactionDetailsFoundError response" when {
      "the transactionItems are empty" in new Test {
        MockRetrieveTransactionsConnector.retrieveTransactions(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, RetrieveTransactionsResponse(Seq())))))

        await(service.list(requestData)) shouldBe Left(ErrorWrapper(Some(correlationId), NoTransactionsFoundError, None))
      }
    }
  }

  "unsuccessful" must {
    "map errors according to spec" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the service" in new Test {


          MockRetrieveTransactionsConnector.retrieveTransactions(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.list(requestData)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
        }

      val input: Seq[(String, MtdError)] = Seq(
        ("NO_TRANSACTIONS_FOUND", NotFoundError),
        ("INVALID_IDTYPE", DownstreamError),
        ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
        ("INVALID_REGIME_TYPE", DownstreamError),
        ("INVALID_DATE_FROM", FromDateFormatError),
        ("INVALID_DATE_TO", ToDateFormatError),
        ("NO_DATA_FOUND", NotFoundError),
        ("SERVER_ERROR", DownstreamError),
        ("SERVICE_UNAVAILABLE", DownstreamError)
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}
