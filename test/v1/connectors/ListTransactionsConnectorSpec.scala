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

package v1.connectors

import mocks.MockAppConfig
import v1.models.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.listTransactions.ListTransactionsParsedRequest
import v1.models.response.listTransaction.{ListTransactionsResponse, TransactionItem}

import scala.concurrent.Future

class ListTransactionsConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"
  val from: String = "2018-05-05"
  val to: String = "2019-12-05"

  val queryParams: Seq[(String, String)] = Seq(
    "dateFrom" -> from,
    "dateTo" -> to,
    "onlyOpenItems" -> "false",
    "includeLocks" -> "true",
    "calculateAccruedInterest" -> "true",
    "removePOA" -> "false",
    "customerPaymentInformation" -> "true",
    "includeStatistical" -> "false"
  )

  val requestData: ListTransactionsParsedRequest = ListTransactionsParsedRequest(
    nino = Nino(nino),
    from = from,
    to = to
  )

  val listTransactionsResponse: ListTransactionsResponse[TransactionItem] = ListTransactionsResponse[TransactionItem](
    transactions = Seq(TransactionItem(
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

  class Test extends MockHttpClient with MockAppConfig {

    val connector: ListTransactionsConnector = new ListTransactionsConnector(http = mockHttpClient, appConfig = mockAppConfig)

    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "ListTransactionsConnector" when {
    "retrieving a list of transaction items" should {
      "return a valid response" in new Test {

        val outcome = Right(ResponseWrapper(correlationId, listTransactionsResponse))

        MockHttpClient
          .parameterGet(
            s"$baseUrl/enterprise/02.00.00/financial-data/NINO/$nino/ITSA",
            queryParams,
            dummyDesHeaderCarrierConfig,
            requiredDesHeaders,
            Seq("AnotherHeader" -> "HeaderValue")
          ).returns(Future.successful(outcome))

        await(connector.listTransactions(requestData)) shouldBe outcome
      }
    }
  }
}