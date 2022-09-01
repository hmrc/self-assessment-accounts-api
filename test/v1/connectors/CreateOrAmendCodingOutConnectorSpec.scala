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
import mocks.MockAppConfig
import uk.gov.hmrc.http.HeaderCarrier
import v1.models.domain.Nino
import v1.mocks.MockHttpClient
import v1.models.outcomes.ResponseWrapper
import v1.models.request.createOrAmendCodingOut.{
  CreateOrAmendCodingOutParsedRequest,
  CreateOrAmendCodingOutRequestBody,
  TaxCodeComponent,
  TaxCodeComponents
}

import scala.concurrent.Future

class CreateOrAmendCodingOutConnectorSpec extends ConnectorSpec {

  val nino: String    = "AA111111A"
  val taxYear: String = "2021-22"

  val createOrAmendCodingOutRequestBody: CreateOrAmendCodingOutRequestBody = CreateOrAmendCodingOutRequestBody(taxCodeComponents = TaxCodeComponents(
    payeUnderpayment = Some(Seq(TaxCodeComponent(id = 12345, amount = 123.45))),
    selfAssessmentUnderpayment = Some(Seq(TaxCodeComponent(id = 12345, amount = 123.45))),
    debt = Some(Seq(TaxCodeComponent(id = 12345, amount = 123.45))),
    inYearAdjustment = Some(TaxCodeComponent(id = 12345, amount = 123.45))
  ))

  val request: CreateOrAmendCodingOutParsedRequest = CreateOrAmendCodingOutParsedRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = createOrAmendCodingOutRequestBody
  )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: CreateOrAmendCodingOutConnector = new CreateOrAmendCodingOutConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "CreateOrAmendCodingOutConnector" when {
    ".amendCodingOut" should {
      "return a success upon HttpClient success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        implicit val hc: HeaderCarrier                   = HeaderCarrier(otherHeaders = otherHeaders ++ Seq("Content-Type" -> "application/json"))
        val requiredIfsHeadersPut: Seq[(String, String)] = requiredIfsHeaders ++ Seq("Content-Type" -> "application/json")

        MockHttpClient
          .put(
            url = s"$baseUrl/income-tax/accounts/self-assessment/collection/tax-code/$nino/$taxYear",
            config = dummyIfsHeaderCarrierConfig,
            body = createOrAmendCodingOutRequestBody,
            requiredHeaders = requiredIfsHeadersPut,
            excludedHeaders = Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.amendCodingOut(request)) shouldBe outcome
      }
    }
  }

}
