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
import api.mocks.MockHttpClient
import mocks.MockAppConfig
import api.models.domain.Nino
import api.models.outcomes.ResponseWrapper
import v1.models.request.retrieveBalance.RetrieveBalanceParsedRequest
import v1.models.response.retrieveBalance.RetrieveBalanceResponse

import scala.concurrent.Future

class RetrieveBalanceConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"

  val retrieveBalanceResponse: RetrieveBalanceResponse =
    RetrieveBalanceResponse(
      overdueAmount = 1000.00,
      payableAmount = 1000.00,
      payableDueDate = Some("2018-04-05"),
      pendingChargeDueAmount = 1000.00,
      pendingChargeDueDate = Some("2019-11-05"),
      totalBalance = 1000.00
    )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: RetrieveBalanceConnector = new RetrieveBalanceConnector(http = mockHttpClient, appConfig = mockAppConfig)

    val queryParams: Seq[(String, String)] =
      Seq(
        "onlyOpenItems"              -> "true",
        "includeLocks"               -> "true",
        "calculateAccruedInterest"   -> "true",
        "removePOA"                  -> "true",
        "customerPaymentInformation" -> "true",
        "includeStatistical"         -> "false"
      )

    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "RetrieveBalanceConnector" when {
    "getting balance" must {
      val request: RetrieveBalanceParsedRequest = RetrieveBalanceParsedRequest(Nino(nino))

      "return a valid response" in new Test {

        val outcome = Right(ResponseWrapper(correlationId, retrieveBalanceResponse))

        MockHttpClient
          .parameterGet(
            s"$baseUrl/enterprise/02.00.00/financial-data/NINO/$nino/ITSA",
            queryParams,
            dummyHeaderCarrierConfig,
            requiredDesHeaders,
            Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveBalance(request)) shouldBe outcome
      }
    }
  }

}
