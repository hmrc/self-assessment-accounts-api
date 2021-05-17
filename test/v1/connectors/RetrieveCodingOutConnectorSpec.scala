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
import v1.models.domain.MtdSource
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveCodingOut.RetrieveCodingOutParsedRequest
import v1.models.response.retrieveCodingOut.{RetrieveCodingOutResponse, TaxCodeComponent}

import scala.concurrent.Future

class RetrieveCodingOutConnectorSpec extends ConnectorSpec {

  val nino: Nino = Nino("AA123456A")
  val taxYear: String = "2019-20"
  val source: String = "hmrcHeld"


  private val taxCodeComponent: TaxCodeComponent =
    TaxCodeComponent(
      amount = 1000,
      relatedTaxYear = "2020",
      submittedOn = "2020-07-06T09:37:17Z"
    )

  private val retrieveCodingOutResponse: RetrieveCodingOutResponse =
    RetrieveCodingOutResponse(
      source = "hmrcHeld",
      selfAssessmentUnderPayments = Some(Seq(taxCodeComponent)),
      payeUnderpayments = Some(Seq(taxCodeComponent)),
      debts = Some(Seq(taxCodeComponent)),
      inYearAdjustments = Some(taxCodeComponent)
    )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: RetrieveCodingOutConnector = new RetrieveCodingOutConnector(http = mockHttpClient, appConfig = mockAppConfig)
    val desRequestHeaders: Seq[(String, String)] = Seq("Environment" -> "ifs-environment", "Authorization" -> s"Bearer ifs-token")

    MockedAppConfig.ifsBaseUrl returns baseUrl
    MockedAppConfig.ifsToken returns "ifs-token"
    MockedAppConfig.ifsEnvironment returns "ifs-environment"
  }

  "RetrieveCodingOutConnector" when {
    "retrieveCodingOut" must {
      "return a valid response" in new Test {
        val request: RetrieveCodingOutParsedRequest = RetrieveCodingOutParsedRequest(nino, taxYear, Some(source))
        val outcome = Right(ResponseWrapper(correlationId, retrieveCodingOutResponse))

        MockedHttpClient
          .get(
            url = s"$baseUrl/income-tax/accounts/self-assessment/collection/tax-code/$nino/$taxYear",
            queryParams = Seq("view" -> MtdSource.parser(source).toDownstreamSource),
            requiredHeaders = "Environment" -> "ifs-environment", "Authorization" -> s"Bearer ifs-token", "CorrelationId" -> s"$correlationId"
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveCodingOut(request)) shouldBe outcome
      }

      "return a valid response when there's no source parameter" in new Test {
        val request: RetrieveCodingOutParsedRequest = RetrieveCodingOutParsedRequest(nino, taxYear, None)
        val outcome = Right(ResponseWrapper(correlationId, retrieveCodingOutResponse))

        MockedHttpClient
          .get(
            url = s"$baseUrl/income-tax/accounts/self-assessment/collection/tax-code/$nino/$taxYear",
            requiredHeaders = "Environment" -> "ifs-environment", "Authorization" -> s"Bearer ifs-token", "CorrelationId" -> s"$correlationId"
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveCodingOut(request)) shouldBe outcome
      }
    }
  }
}
