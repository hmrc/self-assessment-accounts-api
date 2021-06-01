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
import v1.models.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveAllocations.RetrieveAllocationsParsedRequest
import v1.models.response.retrieveAllocations.RetrieveAllocationsResponse
import v1.models.response.retrieveAllocations.detail.AllocationDetail

import scala.concurrent.Future

class RetrieveAllocationsConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"

  val paymentLot: String = "anId"
  val paymentLotItem: String = "anotherId"

  val queryParams: Seq[(String, String)] =
    Seq(
      "paymentLot" -> paymentLot,
      "paymentLotItem" -> paymentLotItem
    )

  val retrieveAllocationsResponse: RetrieveAllocationsResponse[AllocationDetail] =
    RetrieveAllocationsResponse(
      amount = Some(100.00),
      method = Some("aMethod"),
      transactionDate = Some("aDate"),
      allocations = Seq.empty[AllocationDetail]
    )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: RetrieveAllocationsConnector = new RetrieveAllocationsConnector(http = mockHttpClient, appConfig = mockAppConfig)

    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "RetrieveAllocationsConnector" when {
    "retrieving allocations" must {
      val request: RetrieveAllocationsParsedRequest = RetrieveAllocationsParsedRequest(Nino(nino), paymentLot, paymentLotItem)

      "return a valid response" in new Test {

        val outcome = Right(ResponseWrapper(correlationId, retrieveAllocationsResponse))

        MockHttpClient
          .parameterGet(
            s"$baseUrl/cross-regime/payment-allocation/NINO/$nino/ITSA",
            queryParams,
            dummyDesHeaderCarrierConfig,
            requiredDesHeaders,
            Seq("AnotherHeader" -> "HeaderValue")
          ).returns(Future.successful(outcome))

        await(connector.retrieveAllocations(request)) shouldBe outcome
      }
    }
  }
}