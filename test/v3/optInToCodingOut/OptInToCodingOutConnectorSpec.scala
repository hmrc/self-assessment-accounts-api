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

package v3.optInToCodingOut

import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper

import scala.concurrent.Future

class OptInToCodingOutConnectorSpec extends ConnectorSpec {

  private val nino: String = "AA123456A"

  trait Test {
    _: ConnectorTest =>

    protected val connector: OptInToCodingOutConnector = new OptInToCodingOutConnector(
      http = mockHttpClient,
      appConfig = mockSharedAppConfig
    )

  }

  "DeleteCodingOutOptOutConnector" when {
    "downstream returns a successful response" must {
      "return a successful result" in new IfsTest with Test {
        private val outcome = Right(ResponseWrapper(correlationId, ()))

        willDelete(s"$baseUrl/income-tax/accounts/self-assessment/tax-code/opt-out/ITSA/$nino/2024") returns
          Future.successful(outcome)

        await(connector.optInToCodingOut(Nino(nino), TaxYear.fromMtd("2023-24"))) shouldBe outcome
      }
    }
  }

}
