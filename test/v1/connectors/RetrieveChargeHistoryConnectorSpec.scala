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

import api.connectors.ConnectorSpec
import mocks.MockAppConfig
import v1.models.domain.Nino
import v1.fixtures.RetrieveChargeHistoryFixture
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveChargeHistory.RetrieveChargeHistoryParsedRequest

import scala.concurrent.Future

class RetrieveChargeHistoryConnectorSpec extends ConnectorSpec {

  val nino: String          = "AA123456A"
  val transactionId: String = "anId"

  class Test extends MockHttpClient with MockAppConfig {

    val connector: RetrieveChargeHistoryConnector = new RetrieveChargeHistoryConnector(http = mockHttpClient, appConfig = mockAppConfig)

    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "RetrieveChargeHistoryConnector" when {
    "retrieveChargeHistory" must {
      val request: RetrieveChargeHistoryParsedRequest = RetrieveChargeHistoryParsedRequest(Nino(nino), transactionId)

      "return a valid response" in new Test {

        val outcome = Right(ResponseWrapper(correlationId, RetrieveChargeHistoryFixture.retrieveChargeHistoryResponse))

        MockHttpClient
          .parameterGet(
            s"$baseUrl/cross-regime/charges/NINO/$nino/ITSA",
            Seq("docNumber" -> transactionId),
            dummyDesHeaderCarrierConfig,
            requiredDesHeaders,
            Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveChargeHistory(request)) shouldBe outcome
      }
    }
  }

}
