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
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.createOrAmendCodingOut.{CreateOrAmendCodingOutParsedRequest, CreateOrAmendCodingOutRequestBody}

import scala.concurrent.Future

class CreateOrAmendCodingOutConnectorSpec extends ConnectorSpec {

  val nino: String = "AA111111A"
  val taxYear: String = "2021-22"

  val createOrAmendCodingOutRequestBody: CreateOrAmendCodingOutRequestBody = CreateOrAmendCodingOutRequestBody(
    payeUnderpayments = Some(1000.99),
    selfAssessmentUnderPayments = Some(1000.99),
    debts = Some(1000.99),
    inYearAdjustments = Some(1000.99)
  )

  val request: CreateOrAmendCodingOutParsedRequest = CreateOrAmendCodingOutParsedRequest(
    nino = nino,
    taxYear = taxYear,
    body = createOrAmendCodingOutRequestBody
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: CreateOrAmendCodingOutConnector = new CreateOrAmendCodingOutConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockedAppConfig.desBaseUrl returns baseUrl
    MockedAppConfig.desToken returns "des-token"
    MockedAppConfig.desEnvironment returns "des-environment"
  }

  "CreateOrAmendCodingOutConnector" when {
    ".amendCodingOut" should {
      "return a success upon HttpClient success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockedHttpClient
          .put(
            url = s"$baseUrl/income-tax/accounts/self-assessment/collection/tax-code/$nino/$taxYear",
            body = CreateOrAmendCodingOutRequestBody,
            requiredHeaders = "Environment" -> "des-environment", "Authorization" -> s"Bearer des-token", "CorrelationId" -> s"$correlationId"
          )
          .returns(Future.successful(outcome))

        await(connector.amendCodingOut(request)) shouldBe outcome
      }
    }
  }
}