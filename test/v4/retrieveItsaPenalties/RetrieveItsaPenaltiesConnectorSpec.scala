/*
 * Copyright 2026 HM Revenue & Customs
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

package v4.retrieveItsaPenalties

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.Nino
import shared.models.outcomes.ResponseWrapper
import shared.utils.DateUtils.isoDateTimeStamp
import uk.gov.hmrc.http.StringContextOps
import v4.retrieveItsaPenalties.model.request.RetrieveItsaPenaltiesRequestData
import v4.retrieveItsaPenalties.model.response.RetrieveItsaPenaltiesResponse
import v4.retrieveItsaPenalties.model.response.RetrieveItsaPenaltiesFixture.responseModel
import scala.concurrent.Future

class RetrieveItsaPenaltiesConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"

  private trait Test {
    self: ConnectorTest =>

    val connector: RetrieveItsaPenaltiesConnector = new RetrieveItsaPenaltiesConnector(mockHttpClient, mockSharedAppConfig)

    val request: RetrieveItsaPenaltiesRequestData = RetrieveItsaPenaltiesRequestData(Nino(nino))
  }

  private trait HipTestWithAdditionalContractHeaders extends HipTest with Test {

    override val additionalContractHeaders: Seq[(String, String)] = List(
      "X-Originating-System"  -> "MDTP",
      "X-Receipt-Date"        -> isoDateTimeStamp,
      "X-Transmitting-System" -> "HIP"
    )

  }

  "RetrieveItsaPenaltiesConnector" should {
    "return a valid response" in new HipTestWithAdditionalContractHeaders {
      val outcome: DownstreamOutcome[RetrieveItsaPenaltiesResponse] = Right(ResponseWrapper(correlationId, responseModel))

      willGet(
        url = url"$baseUrl/etmp/RESTAdapter/cross-regime/taxpayer/penalties",
        parameters = Seq(
          "taxRegime" -> "ITSA",
          "idType"    -> "NINO",
          "idNumber"  -> nino
        )
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[RetrieveItsaPenaltiesResponse] = await(connector.retrieveItsaPenalties(request))

      result shouldBe outcome
    }
  }

}
