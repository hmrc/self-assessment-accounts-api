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
import api.models.domain.{MtdSource, Nino, TaxYear}
import api.models.outcomes.ResponseWrapper
import v1.models.request.retrieveCodingOut.RetrieveCodingOutParsedRequest
import v1.models.response.retrieveCodingOut._

import scala.concurrent.Future

class RetrieveCodingOutConnectorSpec extends ConnectorSpec {

  val nino: String    = "AA123456A"
  val taxYear: String = "2021-22"
  val source: String  = "hmrcHeld"

  val unmatchedCustomerSubmissions: UnmatchedCustomerSubmissions =
    UnmatchedCustomerSubmissions(
      0,
      "2021-08-24T14:15:22Z",
      Some(BigInt(12345678910L))
    )

  val taxCodeComponents: TaxCodeComponents =
    TaxCodeComponents(
      0,
      Some("2021-22"),
      "2021-08-24T14:15:22Z",
      "hmrcHeld",
      Some(BigInt(12345678910L))
    )

  val taxCodeComponentObject: TaxCodeComponentsObject =
    TaxCodeComponentsObject(
      Some(Seq(taxCodeComponents)),
      Some(Seq(taxCodeComponents)),
      Some(Seq(taxCodeComponents)),
      Some(taxCodeComponents)
    )

  val unmatchedCustomerSubmissionsObject: UnmatchedCustomerSubmissionsObject =
    UnmatchedCustomerSubmissionsObject(
      Some(Seq(unmatchedCustomerSubmissions)),
      Some(Seq(unmatchedCustomerSubmissions)),
      Some(Seq(unmatchedCustomerSubmissions)),
      Some(unmatchedCustomerSubmissions)
    )

  val retrieveCodingOutResponse: RetrieveCodingOutResponse =
    RetrieveCodingOutResponse(
      Some(taxCodeComponentObject),
      Some(unmatchedCustomerSubmissionsObject)
    )

  trait Test { _: ConnectorTest =>

    def taxYear: TaxYear

    def requestWithParams: RetrieveCodingOutParsedRequest = RetrieveCodingOutParsedRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      source = Some(source)
    )

    def requestWithoutParams: RetrieveCodingOutParsedRequest = RetrieveCodingOutParsedRequest(
      nino = Nino(nino),
      taxYear = taxYear,
      source = None
    )

    val connector: RetrieveCodingOutConnector = new RetrieveCodingOutConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

  }

  "RetrieveCodingOutConnector" should {
    "retrieveCodingOut" when {
      "return a valid response" in new Ifs1Test with Test {

        val taxYear = TaxYear.fromMtd("2019-20")
        val outcome = Right(ResponseWrapper(correlationId, retrieveCodingOutResponse))

        willGet(
          url = s"$baseUrl/income-tax/accounts/self-assessment/collection/tax-code/$nino/${taxYear.asMtd}",
          parameters = Seq("view" -> MtdSource.parser(source).toDownstreamSource)
        )
          .returns(Future.successful(outcome))

        await(connector.retrieveCodingOut(requestWithParams)) shouldBe outcome
      }

      "return a valid response when there's no source parameter" in new Ifs1Test with Test {

        val taxYear = TaxYear.fromMtd("2019-20")
        val outcome = Right(ResponseWrapper(correlationId, retrieveCodingOutResponse))

        willGet(
          url = s"$baseUrl/income-tax/accounts/self-assessment/collection/tax-code/$nino/${taxYear.asMtd}"
        )
          .returns(Future.successful(outcome))

        await(connector.retrieveCodingOut(requestWithoutParams)) shouldBe outcome
      }

      "return a valid response when there's no source parameter for a TYS tax year" in new TysIfsTest with Test {

        val taxYear = TaxYear.fromMtd("2023-24")
        val outcome = Right(ResponseWrapper(correlationId, retrieveCodingOutResponse))

        willGet(
          url = s"$baseUrl/income-tax/accounts/self-assessment/collection/tax-code/${taxYear.asTysDownstream}/$nino"
        )
          .returns(Future.successful(outcome))

        await(connector.retrieveCodingOut(requestWithoutParams)) shouldBe outcome
      }
    }
  }

}
