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

import api.connectors.ConnectorSpec
import api.models.domain.{Nino, TransactionId}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import v2.mocks.MockHttpClient
import v2.models.request.retrieveChargeHistory.RetrieveChargeHistoryRequestData
import v2.models.response.retrieveChargeHistory._

import scala.concurrent.Future

class RetrieveChargeHistoryConnectorSpec extends ConnectorSpec {

  val nino: String          = "AA123456A"
  val transactionId: String = "anId"

  val chargeHistoryDetails: ChargeHistoryDetail =
    ChargeHistoryDetail(
      taxYear = Some("2019-20"),
      transactionId = "X123456790A",
      transactionDate = "2019-06-01",
      description = "Balancing Charge Debit",
      totalAmount = 600.01,
      changeDate = "2019-06-05",
      changeReason = "Example reason"
    )

  val retrieveChargeHistoryResponse: RetrieveChargeHistoryResponse =
    RetrieveChargeHistoryResponse(
      chargeHistoryDetails = Seq(chargeHistoryDetails)
    )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: RetrieveChargeHistoryConnector =
      new RetrieveChargeHistoryConnector(http = mockHttpClient, appConfig = mockAppConfig)

    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "RetrieveChargeHistoryConnector" when {
    "retrieveChargeHistory" must {
      val request: RetrieveChargeHistoryRequestData = RetrieveChargeHistoryRequestData(Nino(nino), TransactionId(transactionId))

      "return a valid response" in new Test {

        val outcome = Right(ResponseWrapper(correlationId, retrieveChargeHistoryResponse))

        MockHttpClient
          .get(
            s"$baseUrl/cross-regime/charges/NINO/$nino/ITSA",
            dummyHeaderCarrierConfig,
            parameters = Seq("docNumber" -> transactionId),
            requiredDesHeaders,
            Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveChargeHistory(request)) shouldBe outcome
      }
    }
  }

}
