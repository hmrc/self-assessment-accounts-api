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

package v1.connectors

import mocks.MockAppConfig
import v1.fixtures.ListTransactionFixture._
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper

import scala.concurrent.Future

class ListTransactionsConnectorSpec extends ConnectorSpec {
  val chargeId = "anId"

  val queryParams: Seq[(String, String)] = Seq(
    "dateFrom" -> dateFrom,
    "dateTo" -> dateTo
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: ListTransactionsConnector = new ListTransactionsConnector(http = mockHttpClient, appConfig = mockAppConfig)
    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "ListTransactionsConnector" when {
    "retrieving a list of transaction items" should {
      "return a valid response" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, fullDesSingleListTransactionResponse))

        MockedHttpClient
          .get(
            url = s"$baseUrl/cross-regime/transactions-placeholder/NINO/$nino/ITSA",
            queryParams = queryParams,
            requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
          )
          .returns(Future.successful(outcome))

        await(connector.listTransactions(requestData)) shouldBe outcome
      }
    }
  }
}
