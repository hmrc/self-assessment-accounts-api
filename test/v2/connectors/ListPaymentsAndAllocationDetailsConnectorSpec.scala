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
import v2.mocks.MockHttpClient

import scala.concurrent.Future

class ListPaymentsAndAllocationDetailsConnectorSpec extends ConnectorSpec {

  private val nino = "AA123456A"
  private val from = "2018-08-13"
  private val to = "2019-08-13"
  private val paymentLot = "081203010024"
  private val paymentLotItem = "000001"

  class Test extends MockHttpClient with MockAppConfig {

    val connector: ListPaymentsAndAllocationDetailsConnector =
      new ListPaymentsAndAllocationDetailsConnector(http = mockHttpClient, appConfig = mockAppConfig)

    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)

    def connectorRequest(request: ListPaymentsAndAllocationDetailsRequest,
                         response: ListPaymentsAndAllocationDetailsResponse,
                         queryParams: Seq[(String, String)]): Unit = {

      val outcome = Right(ResponseWrapper(correlationId, response))

      MockHttpClient
        .parameterGet(
          s"$baseUrl/cross-regime/payment-allocation/NINO/$nino/ITSA",
          queryParams,
          dummyIfsHeaderCarrierConfig,
          requiredIfsHeaders,
          Seq("AnotherHeader" -> "HeaderValue")
        )
        .returns(Future.successful(outcome))

      val result = await(connector.listPaymentsAndAllocationDetails(request))
      result shouldBe outcome
    }
  }

  "ListPaymentsAndAllocationDetailsConnector" should {
    "return a valid response" when {
      "a valid request is supplied" in new Test {
        val queryParams: Seq[(String, String)] =
          Seq(
            "from" -> s"$from",
            "to" -> s"$to",
            "paymentLot" -> s"$paymentLot",
            "paymentLotItem" -> s"$paymentLotItem",
          )

        connectorRequest(validRequest, validResponse, queryParams)
      }
    }
  }

}
