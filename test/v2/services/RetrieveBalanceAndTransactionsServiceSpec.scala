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
import api.models.domain.Nino
import api.models.errors.{DownstreamErrorCode, DownstreamErrors, MtdError, _}
import api.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.HeaderCarrier
import v2.fixtures.retrieveBalanceAndTransactions.BalanceDetailsFixture.balanceDetails
import v2.fixtures.retrieveBalanceAndTransactions.CodingDetailsFixture.codingDetails
import v2.fixtures.retrieveBalanceAndTransactions.DocumentDetailsFixture.documentDetails
import v2.fixtures.retrieveBalanceAndTransactions.FinancialDetailsFixture.financialDetails
import v2.mocks.connectors.MockRetrieveBalanceAndTransactionsConnector
import v2.models.request.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsRequest
import v2.models.response.retrieveBalanceAndTransactions._
import v2.services.RetrieveBalanceAndTransactionsService.downstreamErrorMap

import scala.concurrent.Future

class RetrieveBalanceAndTransactionsServiceSpec extends ServiceSpec {

  private val nino = "AA123456A"
  private val docNumber = "anId"
  private val dateFrom = "2018-08-13"
  private val dateTo = "2018-08-14"
  private val onlyOpenItems = false
  private val includeLocks = false
  private val calculateAccruedInterest = false
  private val removePOA = false
  private val customerPaymentInformation = false
  private val includeStatistical = false

  private val requestData: RetrieveBalanceAndTransactionsRequest =
    RetrieveBalanceAndTransactionsRequest(
      Nino(nino),
      Some(docNumber),
      Some(dateFrom),
      Some(dateTo),
      onlyOpenItems,
      includeLocks,
      calculateAccruedInterest,
      removePOA,
      customerPaymentInformation,
      includeStatistical)

  val retrieveBalanceAndTransactionsResponse: RetrieveBalanceAndTransactionsResponse =
    RetrieveBalanceAndTransactionsResponse(
      balanceDetails = balanceDetails,
      codingDetails = Some(Seq(codingDetails)),
      documentDetails = Some(Seq(documentDetails)),
      financialDetails = Some(Seq(financialDetails))
    )

  "RetrieveBalanceAndTransactionsService" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockRetrieveBalanceAndTransactionsConnector
          .retrieveBalanceAndTransactions(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveBalanceAndTransactionsResponse))))

        await(service.retrieveBalanceAndTransactions(requestData)) shouldBe Right(ResponseWrapper(correlationId, retrieveBalanceAndTransactionsResponse))
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

      downstreamErrorMap.foreach(args => (serviceError _).tupled(args))
    }

  }

  trait Test extends MockRetrieveBalanceAndTransactionsConnector {

    implicit val hc: HeaderCarrier = HeaderCarrier()

    implicit val logContext: EndpointLogContext =
      EndpointLogContext("RetrieveBalanceAndTransactionsController", "RetrieveBalanceAndTransactions")

    val service = new RetrieveBalanceAndTransactionsService(
      connector = mockRetrieveBalanceAndTransactionsConnector
    )
  }
}
