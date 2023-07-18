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
import v1.models.request.deleteCodingOut.DeleteCodingOutParsedRequest

import scala.concurrent.Future

class DeleteCodingOutConnectorSpec extends ConnectorSpec {

  "DeleteCodingOutConnector" should {
    "return a 204 status for a success scenario" when {
      "a valid non-TYS request is made" in new Ifs1Test with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2019-20")

        val outcome = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = s"$baseUrl/income-tax/accounts/self-assessment/collection/tax-code/$nino/2019-20"
        ).returns(Future.successful(outcome))

        val result = await(connector.deleteCodingOut(request))

        result shouldBe outcome
      }

      "a valid TYS request is made" in new TysIfsTest with Test {
        def taxYear: TaxYear = TaxYear.fromMtd("2023-24")

        val outcome = Right(ResponseWrapper(correlationId, ()))

        willDelete(
          url = s"$baseUrl/income-tax/23-24/accounts/self-assessment/collection/tax-code/$nino"
        ).returns(Future.successful(outcome))

        val result = await(connector.deleteCodingOut(request))

        result shouldBe outcome
      }
    }
  }

  trait Test {
    _: ConnectorTest =>

    def taxYear: TaxYear

    protected val nino: String = "AA111111A"

    val request: DeleteCodingOutParsedRequest = DeleteCodingOutParsedRequest(
      nino = Nino(nino),
      taxYear = taxYear
    )
    val connector: DeleteCodingOutConnector = new DeleteCodingOutConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )
  }
}
