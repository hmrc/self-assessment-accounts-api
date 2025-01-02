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

package v3.retrieveCodingOutStatus

import api.connectors.ConnectorSpec
import v3.retrieveCodingOutStatus.def1.model.request.Def1_RetrieveCodingOutStatusRequestData
import v3.retrieveCodingOutStatus.def1.model.response.Def1_RetrieveCodingOutStatusResponse
import v3.retrieveCodingOutStatus.model.request.RetrieveCodingOutStatusRequestData
import v3.retrieveCodingOutStatus.model.response.RetrieveCodingOutStatusResponse

import scala.concurrent.Future

class RetrieveCodingOutStatusConnectorSpec extends ConnectorSpec {

  private val nino: String           = "AA123456A"
  private val taxYear: TaxYear       = TaxYear("2024")
  private val processingDate: String = "2020-12-17T09:30:47Z"

  trait Test {
    _: ConnectorTest =>

    val response: RetrieveCodingOutStatusResponse =
      Def1_RetrieveCodingOutStatusResponse(processingDate = processingDate, nino = nino, taxYear = taxYear, optOutIndicator = true)

    protected val connector: RetrieveCodingOutStatusConnector = new RetrieveCodingOutStatusConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    protected def request(nino: Nino, taxYear: TaxYear): RetrieveCodingOutStatusRequestData = Def1_RetrieveCodingOutStatusRequestData(nino, taxYear)
  }

  "RetrieveCodingOutStatusConnector" when {
    "retrieveCodingOutStatus" must {
      "return a 200 status with a valid response" in new Ifs1Test with Test {
        private val outcome = Right(ResponseWrapper(correlationId, response))
        willGet(s"$baseUrl/income-tax/accounts/self-assessment/tax-code/opt-out/ITSA/$nino/2024")
          .returns(Future.successful(outcome))

        await(connector.retrieveCodingOutStatus(request(Nino(nino), TaxYear.fromMtd("2023-24")))) shouldEqual outcome
      }
    }
  }

}
