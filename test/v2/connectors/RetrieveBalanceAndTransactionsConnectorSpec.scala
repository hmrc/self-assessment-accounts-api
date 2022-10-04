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
import v2.fixtures.retrieveBalanceAndTransactions.BalanceDetailsFixture.balanceDetailsObject
import v2.fixtures.retrieveBalanceAndTransactions.CodingDetailsFixture.codingDetailsObject
import v2.fixtures.retrieveBalanceAndTransactions.DocumentDetailsFixture.documentDetails
import v2.fixtures.retrieveBalanceAndTransactions.FinanceDetailsFixture.financeDetailsFullObject
import v2.mocks.MockHttpClient
import v2.models.request.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsRequest
import v2.models.response.retrieveBalanceAndTransactions._

import scala.concurrent.Future

class RetrieveBalanceAndTransactionsConnectorSpec extends ConnectorSpec {

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

  val retrieveBalanceAndTransactionsResponse: RetrieveBalanceAndTransactionsResponse =
    RetrieveBalanceAndTransactionsResponse(
      balanceDetails = balanceDetailsObject,
      codingDetails = Some(Seq(codingDetailsObject)),
      documentDetails = Some(Seq(documentDetails)),
      financeDetails = Some(Seq(financeDetailsFullObject))
    )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: RetrieveBalanceAndTransactionsConnector =
      new RetrieveBalanceAndTransactionsConnector(http = mockHttpClient, appConfig = mockAppConfig)

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "RetrieveBalanceAndTransactionsConnector" when {
    "retrieveBalanceAndTransactions" must {
      val request: RetrieveBalanceAndTransactionsRequest = RetrieveBalanceAndTransactionsRequest(
        nino = Nino(nino),
        docNumber = Some(docNumber),
        dateFrom = Some(dateFrom),
        dateTo = Some(dateTo),
        onlyOpenItems = onlyOpenItems,
        includeLocks = includeLocks,
        calculateAccruedInterest = calculateAccruedInterest,
        removePOA = removePOA,
        customerPaymentInformation = customerPaymentInformation,
        includeStatistical = includeStatistical)

      "return a valid response" in new Test {

        val outcome = Right(ResponseWrapper(correlationId, retrieveBalanceAndTransactionsResponse))

        MockHttpClient
          .parameterGet(
            s"$baseUrl/enterprise/02.00.00/financial-data/NINO/$nino/ITSA",
            Seq(
              "docNumber" -> "anId",
              "onlyOpenItems" -> onlyOpenItems.toString,
              "includeLocks" -> includeLocks.toString,
              "calculateAccruedInterest" -> calculateAccruedInterest.toString,
              "removePOA" -> removePOA.toString,
              "customerPaymentInformation" -> customerPaymentInformation.toString,
              "includeStatistical" -> includeStatistical.toString
            ),
            dummyIfsHeaderCarrierConfig,
            requiredIfsHeaders,
            Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveBalanceAndTransactions(request)) shouldBe outcome
      }
    }
  }

}
