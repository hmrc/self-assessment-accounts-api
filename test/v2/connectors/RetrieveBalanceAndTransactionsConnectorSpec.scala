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

package v2.connectors

import api.models.domain.Nino
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import v2.fixtures.retrieveBalanceAndTransactions.BalanceDetailsFixture.balanceDetails
import v2.fixtures.retrieveBalanceAndTransactions.CodingDetailsFixture.codingDetails
import v2.fixtures.retrieveBalanceAndTransactions.DocumentDetailsFixture.documentDetails
import v2.fixtures.retrieveBalanceAndTransactions.FinancialDetailsFixture.financialDetailsFullObject
import v2.mocks.MockHttpClient
import v2.models.request.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsRequest
import v2.models.response.retrieveBalanceAndTransactions._

import scala.concurrent.Future

class RetrieveBalanceAndTransactionsConnectorSpec extends ConnectorSpec {

  private val nino = "AA123456A"
  private val docNumber = "anId"
  private val dateFrom = "2018-08-13"
  private val dateTo = "2019-08-13"
  private val onlyOpenItems = false
  private val includeLocks = false
  private val calculateAccruedInterest = false
  private val removePOA = false
  private val customerPaymentInformation = false
  private val includeChargeEstimate = false

  private val validResponse: RetrieveBalanceAndTransactionsResponse =
    RetrieveBalanceAndTransactionsResponse(
      balanceDetails = balanceDetails,
      codingDetails = Some(Seq(codingDetails)),
      documentDetails = Some(Seq(documentDetails)),
      financialDetails = Some(Seq(financialDetailsFullObject))
    )

  private val validRequest: RetrieveBalanceAndTransactionsRequest = RetrieveBalanceAndTransactionsRequest(
    nino = Nino(nino),
    docNumber = Some(docNumber),
    dateFrom = Some(dateFrom),
    dateTo = Some(dateTo),
    onlyOpenItems = onlyOpenItems,
    includeLocks = includeLocks,
    calculateAccruedInterest = calculateAccruedInterest,
    removePOA = removePOA,
    customerPaymentInformation = customerPaymentInformation,
    includeChargeEstimate = includeChargeEstimate)

  private val commonQueryParams: Seq[(String, String)] = Seq(
    "onlyOpenItems" -> onlyOpenItems.toString,
    "includeLocks" -> includeLocks.toString,
    "calculateAccruedInterest" -> calculateAccruedInterest.toString,
    "removePOA" -> removePOA.toString,
    "customerPaymentInformation" -> customerPaymentInformation.toString,
    "includeChargeEstimate" -> includeChargeEstimate.toString
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: RetrieveBalanceAndTransactionsConnector =
      new RetrieveBalanceAndTransactionsConnector(http = mockHttpClient, appConfig = mockAppConfig)

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)

    def connectorRequest(request: RetrieveBalanceAndTransactionsRequest,
                         response: RetrieveBalanceAndTransactionsResponse,
                         queryParams: Seq[(String, String)]): Unit = {

      val outcome = Right(ResponseWrapper(correlationId, response))

      MockHttpClient
        .parameterGet(
          s"$baseUrl/enterprise/02.00.00/financial-data/NINO/$nino/ITSA",
          queryParams,
          dummyIfsHeaderCarrierConfig,
          requiredIfsHeaders,
          Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.retrieveBalanceAndTransactions(request)) shouldBe outcome
    }
  }

  "RetrieveBalanceAndTransactionsConnector" should {

      "return a valid response" when {

        "a valid request containing both docNumber and dateFrom and dateTo is supplied" in new Test {
          val queryParams: Seq[(String, String)] =
            commonQueryParams ++ Seq(
              "docNumber" -> s"$docNumber",
              "dateFrom" -> s"$dateFrom",
              "dateTo" -> s"$dateTo",
            )

          connectorRequest(validRequest, validResponse, queryParams)
        }

        "a valid request containing docNumber and not dateFrom or dateTo is supplied" in new Test {
          val request: RetrieveBalanceAndTransactionsRequest = validRequest.copy(dateFrom = None, dateTo = None)

          val queryParams: Seq[(String, String)] =
            commonQueryParams ++ Seq("docNumber" -> s"$docNumber")

          connectorRequest(request, validResponse, queryParams)
        }

        "a valid request containing dateFrom and dateTo and no docNumber is supplied" in new Test {
          val request: RetrieveBalanceAndTransactionsRequest = validRequest.copy(docNumber = None)

          val queryParams: Seq[(String, String)] =
            commonQueryParams ++ Seq(
            "dateFrom" -> s"$dateFrom",
            "dateTo" -> s"$dateTo"
          )

          connectorRequest(request, validResponse, queryParams)
        }

      }
  }

}
