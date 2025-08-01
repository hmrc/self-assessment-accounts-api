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

package v3.retrieveCodingOut

import common.models.MtdSource.hmrcHeld
import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TaxYear}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v3.retrieveCodingOut.def1.model.reponse.RetrieveCodingOutFixture._
import v3.retrieveCodingOut.def1.model.request.Def1_RetrieveCodingOutRequestData
import v3.retrieveCodingOut.model.request.RetrieveCodingOutRequestData

import scala.concurrent.Future

class RetrieveCodingOutConnectorSpec extends ConnectorSpec {

  private val nino        = Nino("AA123456A")
  private val taxYear     = TaxYear.fromMtd("2019-20")
  private val tysTaxYear  = TaxYear.fromMtd("2023-24")
  private val source      = hmrcHeld
  private val queryParams = "view" -> "HMRC-HELD"

  trait Test { _: ConnectorTest =>

    val connector = new RetrieveCodingOutConnector(mockHttpClient, mockSharedAppConfig)

    def connectorRequest(taxYear: TaxYear): Unit = {

      val request: RetrieveCodingOutRequestData = Def1_RetrieveCodingOutRequestData(nino, taxYear, Some(source))

      val outcome = Right(ResponseWrapper(correlationId, retrieveCodingOutResponse))

      val uri = if (taxYear.useTaxYearSpecificApi) {
        url"$baseUrl/income-tax/accounts/self-assessment/collection/tax-code/${taxYear.asTysDownstream}/$nino"
      } else {
        url"$baseUrl/income-tax/accounts/self-assessment/collection/tax-code/$nino/${taxYear.asMtd}"
      }

      willGet(uri, Seq(queryParams)).returns(Future.successful(outcome))

      await(connector.retrieveCodingOut(request)) shouldBe outcome
    }

  }

  "RetrieveCodingOutConnector" should {
    "return a valid response" when {
      "a valid request is supplied" in new IfsTest with Test {
        connectorRequest(taxYear)
      }

      "a valid TYS request is supplied" in new IfsTest with Test {
        connectorRequest(tysTaxYear)
      }
    }
  }

}
