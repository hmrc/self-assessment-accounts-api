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
import v1.models.request.listPayments.ListPaymentsParsedRequest
import v1.models.response.listPayments.{ListPaymentsResponse, Payment}

import scala.concurrent.Future

class ListPaymentsConnectorSpec extends ConnectorSpec {

  val nino = Nino("AA123456A")

  val from = "2020-01-01"
  val to = "2020-01-02"

  val queryParams: Seq[(String, String)] = Seq(
    "dateFrom" -> from,
    "dateTo" -> to
  )

  val response = ListPaymentsResponse(
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
    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "ListPaymentsConnector" when {
    "retrieving a list of payments" should {
      "return a valid response" in new Test {
        val request: ListPaymentsParsedRequest = ListPaymentsParsedRequest(nino, from, to)
        val outcome = Right(ResponseWrapper(correlationId, response))

        MockedHttpClient
          .get(
            url = s"$baseUrl/cross-regime/payment-allocation/NINO/$nino/ITSA",
            queryParams = queryParams,
            requiredHeaders ="Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
          )
          .returns(Future.successful(outcome))

        await(connector.listPayments(request)) shouldBe outcome
      }
    }
  }

}
