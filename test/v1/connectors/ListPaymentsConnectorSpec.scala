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

package v1.connectors

import api.connectors.ConnectorSpec
import api.mocks.MockHttpClient
import api.models.domain.Nino
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import v1.models.request.listPayments.ListPaymentsParsedRequest
import v1.models.response.listPayments.{ListPaymentsResponse, Payment}

import scala.concurrent.Future

class ListPaymentsConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"

  val from: String = "2020-01-01"
  val to: String   = "2020-01-02"

  val queryParams: Seq[(String, String)] = Seq(
    "dateFrom" -> from,
    "dateTo"   -> to
  )

  private val response = ListPaymentsResponse(
    payments = Seq(
      Payment(
        paymentId = Some("123-456"),
        amount = Some(10.25),
        method = Some("beans"),
        transactionDate = Some("2020-01-01")
      ),
      Payment(
        paymentId = Some("234-567"),
        amount = Some(20.25),
        method = Some("more beans"),
        transactionDate = Some("2020-01-02")
      )
    )
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: ListPaymentsConnector = new ListPaymentsConnector(http = mockHttpClient, appConfig = mockAppConfig)

    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "ListPaymentsConnector" when {

    val request: ListPaymentsParsedRequest = ListPaymentsParsedRequest(Nino(nino), from, to)

    "retrieving a list of payments" should {
      "return a valid response" in new Test {

        val outcome = Right(ResponseWrapper(correlationId, response))

        MockHttpClient
          .parameterGet(
            s"$baseUrl/cross-regime/payment-allocation/NINO/$nino/ITSA",
            queryParams,
            dummyHeaderCarrierConfig,
            requiredDesHeaders,
            Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.listPayments(request)) shouldBe outcome
      }
    }
  }

}
