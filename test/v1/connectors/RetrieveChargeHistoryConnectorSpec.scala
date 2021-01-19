/*
 * Copyright 2021 HM Revenue & Customs
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
import v1.fixtures.RetrieveChargeHistoryFixture
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveChargeHistory.RetrieveChargeHistoryParsedRequest

import scala.concurrent.Future

class RetrieveChargeHistoryConnectorSpec extends ConnectorSpec {

  val nino: Nino = Nino("AA123456A")
  val transactionId: String = "anId"

  class Test extends MockHttpClient with MockAppConfig {

    val connector: RetrieveChargeHistoryConnector = new RetrieveChargeHistoryConnector(http = mockHttpClient, appConfig = mockAppConfig)
    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "RetrieveChargeHistoryConnector" when {
    "retrieveChargeHistory" must {
      val request: RetrieveChargeHistoryParsedRequest = RetrieveChargeHistoryParsedRequest(nino, transactionId)

      "return a valid response" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, RetrieveChargeHistoryFixture.retrieveChargeHistoryResponse))

        MockedHttpClient
          .get(
            url = s"$baseUrl/cross-regime/charges/NINO/$nino/ITSA",
            queryParams = Seq("docNumber" -> transactionId),
            requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveChargeHistory(request)) shouldBe outcome
      }
    }
  }
}