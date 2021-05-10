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
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.deleteCodingOut.DeleteCodingOutParsedRequest

import scala.concurrent.Future

class DeleteCodingOutConnectorSpec extends ConnectorSpec {

  val nino: String = "AA111111A"
  val taxYear: String = "2021-22"

  val request: DeleteCodingOutParsedRequest = DeleteCodingOutParsedRequest(
    nino = Nino(nino),
    taxYear = taxYear
  )

  class Test extends MockHttpClient with MockAppConfig {
    val connector: DeleteCodingOutConnector = new DeleteCodingOutConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.ifsBaseUrl returns baseUrl
    MockedAppConfig.ifsToken returns "ifs-token"
    MockedAppConfig.ifsEnvironment returns "ifs-environment"
  }

  "DeleteCodingOutConnector" when {
    ".deleteCodingOut" should {
      "return a 204 status for a success scenario" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockedHttpClient.delete(
          url = s"$baseUrl/income-tax/accounts/self-assessment/collection/tax-code/$nino/$taxYear",
          requiredHeaders = "Environment" -> "ifs-environment", "Authorization" -> s"Bearer ifs-token", "CorrelationId" -> s"$correlationId"
        ).returns(Future.successful(outcome))

        await(connector.deleteCodingOut(request)) shouldBe outcome
      }
    }
  }
}