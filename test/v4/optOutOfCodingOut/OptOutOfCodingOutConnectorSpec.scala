/*
 * Copyright 2025 HM Revenue & Customs
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

package v4.optOutOfCodingOut

import shared.connectors.ConnectorSpec
import shared.models.domain.{EmptyJsonBody, Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import v4.optOutOfCodingOut.def1.model.request.Def1_OptOutOfCodingOutRequestData
import v4.optOutOfCodingOut.def1.model.response.Def1_OptOutOfCodingOutResponse

import scala.concurrent.Future

class OptOutOfCodingOutConnectorSpec extends ConnectorSpec {

  private val nino = Nino("AA123456A")
  private val taxYear = TaxYear.fromMtd("2019-20")

  private val request = new Def1_OptOutOfCodingOutRequestData(nino, taxYear)

  trait Test {
    _: ConnectorTest =>

    protected val connector = new OptOutOfCodingOutConnector(mockHttpClient, mockSharedAppConfig)

  }

  "OptOutOfCodingOutConnector" when {
    "downstream returns a successful response" should {
      "return a successful result" in new IfsTest with Test {

        private val outcome = Right(ResponseWrapper(correlationId, Def1_OptOutOfCodingOutResponse(processingDate = "2020-12-17T09:30:47Z")))
        willPut(s"$baseUrl/income-tax/accounts/self-assessment/tax-code/opt-out/ITSA/${nino.value}/${taxYear.asDownstream}", EmptyJsonBody)
          .returns(Future.successful(outcome))

        await(connector.amendCodingOutOptOut(request)) shouldBe outcome
      }
    }
  }

}
