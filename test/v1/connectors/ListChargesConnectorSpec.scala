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
import v1.fixtures.ListChargesFixture._
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.listCharges.ListChargesParsedRequest
import v1.models.response.listCharges.ListChargesResponse

import scala.concurrent.Future

class ListChargesConnectorSpec extends ConnectorSpec {

  val nino: Nino = Nino("AA123456A")

  val from: String = "2020-01-01"
  val to: String = "2020-01-02"

  val queryParams: Seq[(String, String)] = Seq(
    "dateFrom" -> from,
    "dateTo" -> to,
    "onlyOpenItems" -> "false",
    "includeLocks" -> "true",
    "calculateAccruedInterest" -> "true",
    "removePOA" -> "true",
    "customerPaymentInformation" -> "true"
  )

  private val response = ListChargesResponse(
    charges = Seq(
      fullChargeModel,
      fullChargeModel
    )
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: ListChargesConnector = new ListChargesConnector(http = mockHttpClient, appConfig = mockAppConfig)
    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "des-environment", "Authorization" -> s"Bearer des-token")

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "ListChargesConnector" when {
    "retrieving a list of charges" should {
      "return a valid response" in new Test {
        val request: ListChargesParsedRequest = ListChargesParsedRequest(nino, from, to)
        val outcome = Right(ResponseWrapper(correlationId, response))

        MockedHttpClient
          .get(
            url = s"$baseUrl/enterprise/02.00.00/financial-data/NINO/$nino/ITSA",
            queryParams = queryParams,
            requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token"
          )
          .returns(Future.successful(outcome))

        await(connector.listCharges(request)) shouldBe outcome
      }
    }
  }
}