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
import v1.fixtures.RetrieveTransactionDetailsFixture._
import v1.mocks.connectors.MockRetrieveTransactionDetailsConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.response.retrieveTransactionDetails.RetrieveTransactionDetailsResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveTransactionDetailsServiceSpec extends UnitSpec {

  private val correlationId = "X-123"

  trait Test extends MockRetrieveTransactionDetailsConnector {

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("controller", "retrieveTransactions")

    val service = new RetrieveTransactionDetailsService(
      connector = mockRetrieveTransactionDetailsConnector
    )
  }

  "retrieveTransactionDetails" should {
    "return a successful response" when {
      "received a valid response for the supplied request" in new Test {
        val response = Right(ResponseWrapper(correlationId, retrieveTransactionDetailsResponsePayment))

        MockRetrieveTransactionDetailsConnector.retrieveDetails(requestData)
          .returns(Future.successful(response))

        await(service.retrieveTransactionDetails(requestData)) shouldBe response
      }
    }

    "return NoTransactionDetailsFoundError response" when {
      "the transactionItems are empty" in new Test {
        MockRetrieveTransactionDetailsConnector.retrieveDetails(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, RetrieveTransactionDetailsResponse(Seq())))))

        await(service.retrieveTransactionDetails(requestData)) shouldBe Left(ErrorWrapper(Some(correlationId), NoTransactionDetailsFoundError, None))
      }
    }

    "return error response" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned" in new Test {

          MockRetrieveTransactionDetailsConnector.retrieveDetails(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.retrieveTransactionDetails(requestData)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
        }

        val input: Seq[(String, MtdError)] = Seq(
          ("INVALID_IDTYPE", DownstreamError),
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_REGIME_TYPE", DownstreamError),
          ("INVALID_SAP_DOCUMENT_NUMBER", TransactionIdFormatError),
          ("NOT_FOUND", NotFoundError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
    }
  }
}
