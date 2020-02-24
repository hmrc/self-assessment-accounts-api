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
import uk.gov.hmrc.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveBalance.RetrieveBalanceParsedRequest
import v1.models.response.retrieveBalance.RetrieveBalanceResponse

import scala.concurrent.Future

class RetrieveBalanceConnectorSpec extends ConnectorSpec {

  val nino = Nino("AA123456A")

  val retrieveBalanceResponse: RetrieveBalanceResponse =
    RetrieveBalanceResponse(
      overdueAmount = Some(1000.00),
      payableAmount = 1000.00,
      payableDueDate = Some("2018-04-05"),
      pendingChargeDueAmount = Some(1000.00),
      pendingChargeDueDate = Some("2019-11-05")
    )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: RetrieveBalanceConnector = new RetrieveBalanceConnector(http = mockHttpClient, appConfig = mockAppConfig)
    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "RetrieveBalanceConnector" when {
    "getting balance" must {
      val request: RetrieveBalanceParsedRequest = RetrieveBalanceParsedRequest(nino)

      "return a valid response" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, retrieveBalanceResponse))

        MockedHttpClient
          .get(
            url = s"$baseUrl/cross-regime/balance/NINO/$nino/ITSA",
            requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveBalance(request)) shouldBe outcome
      }
    }
  }
}

