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

package v2.connectors

import api.config.MockAppConfig
import api.connectors.{ConnectorSpec, MockHttpClient}
import api.models.domain.{DateRange, Nino}
import api.models.outcomes.ResponseWrapper
import v2.fixtures.retrieveBalanceAndTransactions.BalanceDetailsFixture.balanceDetails
import v2.fixtures.retrieveBalanceAndTransactions.CodingDetailsFixture.codingDetails
import v2.fixtures.retrieveBalanceAndTransactions.DocumentDetailsFixture.documentDetails
import v2.fixtures.retrieveBalanceAndTransactions.FinancialDetailsFixture.financialDetailsFull
import v2.models.request.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsRequestData
import v2.models.response.retrieveBalanceAndTransactions._

import java.time.LocalDate
import scala.concurrent.Future

class RetrieveBalanceAndTransactionsConnectorSpec extends ConnectorSpec {

  private val nino                       = "AA123456A"
  private val docNumber                  = "anId"
  private val fromDate                   = "2018-08-13"
  private val toDate                     = "2019-08-13"
  private val onlyOpenItems              = false
  private val includeLocks               = false
  private val calculateAccruedInterest   = false
  private val removePOA                  = false
  private val customerPaymentInformation = false
  private val includeEstimatedCharges    = false

  private val validResponse: RetrieveBalanceAndTransactionsResponse =
    RetrieveBalanceAndTransactionsResponse(
      balanceDetails = balanceDetails,
      codingDetails = Some(List(codingDetails)),
      documentDetails = Some(List(documentDetails)),
      financialDetails = Some(List(financialDetailsFull))
    )

  private val validRequest: RetrieveBalanceAndTransactionsRequestData = RetrieveBalanceAndTransactionsRequestData(
    nino = Nino(nino),
    docNumber = Some(docNumber),
    Some(DateRange(LocalDate.parse(fromDate), LocalDate.parse(toDate))),
    onlyOpenItems = onlyOpenItems,
    includeLocks = includeLocks,
    calculateAccruedInterest = calculateAccruedInterest,
    removePOA = removePOA,
    customerPaymentInformation = customerPaymentInformation,
    includeEstimatedCharges = includeEstimatedCharges
  )

  private val commonQueryParams: Seq[(String, String)] = List(
    "onlyOpenItems"              -> onlyOpenItems.toString,
    "includeLocks"               -> includeLocks.toString,
    "calculateAccruedInterest"   -> calculateAccruedInterest.toString,
    "removePOA"                  -> removePOA.toString,
    "customerPaymentInformation" -> customerPaymentInformation.toString,
    "includeStatistical"         -> includeEstimatedCharges.toString
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: RetrieveBalanceAndTransactionsConnector =
      new RetrieveBalanceAndTransactionsConnector(http = mockHttpClient, appConfig = mockAppConfig)

    MockedAppConfig.ifs2BaseUrl returns baseUrl
    MockedAppConfig.ifs2Token returns "ifs2-token"
    MockedAppConfig.ifs2Environment returns "ifs2-environment"
    MockedAppConfig.ifs2EnvironmentHeaders returns Some(allowedIfs2Headers)

    def connectorRequest(request: RetrieveBalanceAndTransactionsRequestData,
                         response: RetrieveBalanceAndTransactionsResponse,
                         queryParams: Seq[(String, String)]): Unit = {

      val outcome = Right(ResponseWrapper(correlationId, response))

      MockedHttpClient
        .get(
          url = s"$baseUrl/enterprise/02.00.00/financial-data/NINO/$nino/ITSA",
          config = dummyHeaderCarrierConfig,
          parameters = queryParams,
          requiredHeaders = requiredIfs2Headers,
          excludedHeaders = List("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      await(connector.retrieveBalanceAndTransactions(request)) shouldBe outcome
    }

  }

  "RetrieveBalanceAndTransactionsConnector" should {

    "return a valid response" when {

      "a valid request containing both docNumber and fromDate and dateTo is supplied" in new Test {
        val queryParams: Seq[(String, String)] =
          commonQueryParams ++ List(
            "docNumber" -> s"$docNumber",
            "dateFrom"  -> s"$fromDate",
            "dateTo"    -> s"$toDate"
          )

        connectorRequest(validRequest, validResponse, queryParams)
      }

      "a valid request containing docNumber and not fromDate or dateTo is supplied" in new Test {
        val request: RetrieveBalanceAndTransactionsRequestData = validRequest.copy(fromAndToDates = None)

        val queryParams: Seq[(String, String)] =
          commonQueryParams ++ List("docNumber" -> s"$docNumber")

        connectorRequest(request, validResponse, queryParams)
      }

      "a valid request containing fromDate and dateTo and no docNumber is supplied" in new Test {
        val request: RetrieveBalanceAndTransactionsRequestData = validRequest.copy(docNumber = None)

        val queryParams: Seq[(String, String)] =
          commonQueryParams ++ List(
            "dateFrom" -> s"$fromDate",
            "dateTo"   -> s"$toDate"
          )

        connectorRequest(request, validResponse, queryParams)
      }

    }
  }

}
