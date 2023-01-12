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
import api.models.domain.{Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import v1.models.request.createOrAmendCodingOut.{CreateOrAmendCodingOutParsedRequest, CreateOrAmendCodingOutRequestBody, TaxCodeComponents}

import scala.concurrent.Future

class CreateOrAmendCodingOutConnectorSpec extends ConnectorSpec {

  val nino: String = "AA111111A"

  trait Test { _: ConnectorTest =>
    def taxYear: TaxYear

    val createOrAmendCodingOutRequestBody: CreateOrAmendCodingOutRequestBody =
      CreateOrAmendCodingOutRequestBody(taxCodeComponents = TaxCodeComponents(None, None, None, None))

    val request: CreateOrAmendCodingOutParsedRequest = CreateOrAmendCodingOutParsedRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      body = createOrAmendCodingOutRequestBody
    )

    val connector: CreateOrAmendCodingOutConnector = new CreateOrAmendCodingOutConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

  }

  "CreateOrAmendCodingOutConnector" when {
    "called for a non-TYS tax year" should {
      "return a success upon HttpClient success" in new Ifs1Test with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2021-22")

        val outcome = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = s"$baseUrl/income-tax/accounts/self-assessment/collection/tax-code/$nino/2021-22",
          body = createOrAmendCodingOutRequestBody
        ) returns Future.successful(outcome)

        await(connector.amendCodingOut(request)) shouldBe outcome
      }
    }

    "called for a TYS tax year" should {
      "return a success upon HttpClient success" in new TysIfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val outcome = Right(ResponseWrapper(correlationId, ()))

        willPut(
          url = s"$baseUrl/income-tax/23-24/accounts/self-assessment/collection/tax-code/$nino",
          body = createOrAmendCodingOutRequestBody
        ) returns Future.successful(outcome)

        await(connector.amendCodingOut(request)) shouldBe outcome
      }
    }
  }

}
