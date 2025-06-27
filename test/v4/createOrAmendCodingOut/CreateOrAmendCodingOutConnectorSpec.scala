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

package v4.createOrAmendCodingOut

import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v4.createOrAmendCodingOut.def1.model.request.Def1_CreateOrAmendCodingOutRequestData
import v4.createOrAmendCodingOut.def1.models.request.CodingOutFixtures.validRequestBody
import v4.createOrAmendCodingOut.model.request.CreateOrAmendCodingOutRequestData

import scala.concurrent.Future

class CreateOrAmendCodingOutConnectorSpec extends ConnectorSpec {

  private val nino       = Nino("AA123456A")
  private val taxYear    = TaxYear.fromMtd("2019-20")
  private val tysTaxYear = TaxYear.fromMtd("2023-24")

  trait Test {
    _: ConnectorTest =>

    val connector: CreateOrAmendCodingOutConnector =
      new CreateOrAmendCodingOutConnector(mockHttpClient, mockSharedAppConfig)

    def connectorRequest(taxYear: TaxYear): Unit = {

      val validRequest: CreateOrAmendCodingOutRequestData = Def1_CreateOrAmendCodingOutRequestData(nino, taxYear, validRequestBody)
      val outcome = Right(ResponseWrapper(correlationId, ()))

       val uri = if (taxYear.useTaxYearSpecificApi) {
        url"$baseUrl/income-tax/${taxYear.asTysDownstream}/accounts/self-assessment/collection/tax-code/$nino"
      } else {
        url"$baseUrl/income-tax/accounts/self-assessment/collection/tax-code/$nino/${taxYear.asMtd}"
      }

      willPut(
        url = uri,
        body = validRequest.body
      ).returns(Future.successful(outcome))

      val result = await(connector.amendCodingOut(validRequest))
      result shouldBe outcome
    }

  }

  "CreateOrAmendCodingOutConnector" should {
    "return a valid response" when {
      "a valid request is supplied" in new IfsTest with Test {
        connectorRequest(taxYear)
      }

      "a valid TYS request is supplied" in new TysIfsTest with Test {
        connectorRequest(tysTaxYear)
      }
    }
  }

}
