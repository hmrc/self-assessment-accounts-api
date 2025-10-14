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

package v3.retrieveChargeHistoryByChargeReference

import play.api.Configuration
import common.models.ChargeReference
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.Nino
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import shared.utils.DateUtils.isoDateTimeStamp
import v3.retrieveChargeHistoryByChargeReference.def1.model.request.Def1_RetrieveChargeHistoryByChargeReferenceRequestData
import v3.retrieveChargeHistoryByChargeReference.def1.model.response.RetrieveChargeHistoryFixture.validChargeHistoryResponseObject
import v3.retrieveChargeHistoryByChargeReference.model.request.RetrieveChargeHistoryByChargeReferenceRequestData
import v3.retrieveChargeHistoryByChargeReference.model.response.RetrieveChargeHistoryResponse

import scala.concurrent.Future

class RetrieveChargeHistoryByChargeReferenceConnectorSpec extends ConnectorSpec {

  val nino: String            = "AA123456A"
  val chargeReference: String = "anId"

  private trait Test {
    self: ConnectorTest =>

    private val connector: RetrieveChargeHistoryByChargeReferenceConnector =
      new RetrieveChargeHistoryByChargeReferenceConnector(mockHttpClient, mockSharedAppConfig)

    def connectorRequest(request: RetrieveChargeHistoryByChargeReferenceRequestData,
                         response: RetrieveChargeHistoryResponse,
                         queryParams: Seq[(String, String)],
                         hipTest: Boolean): Unit = {

      val outcome = Right(ResponseWrapper(correlationId, response))

      val url = if (hipTest) {
        url"$baseUrl/etmp/RESTAdapter/itsa/taxpayer/GetChargeHistory"
      } else {
        url"$baseUrl/cross-regime/charges/NINO/$nino/ITSA"
      }
      willGet(
        url = url,
        parameters = queryParams
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[RetrieveChargeHistoryResponse] = await(connector.retrieveChargeHistoryByChargeReference(request))
      result shouldBe outcome
    }

  }

  def hipQueryParams: Seq[(String, String)] =
    Seq("idType" -> "NINO", "idValue" -> nino, "chargeReference" -> chargeReference)

  private val validRequest: RetrieveChargeHistoryByChargeReferenceRequestData = Def1_RetrieveChargeHistoryByChargeReferenceRequestData(
    Nino(nino),
    ChargeReference(chargeReference)
  )

  "RetrieveChargeHistoryByChargeReferenceConnector" when {
    "the feature switch is enabled (HIP disabled)" must {
      "return a valid response" in new IfsTest with Test {
        MockedSharedAppConfig.featureSwitchConfig returns Configuration("ifs_hip_migration_1554.enabled" -> false)
        val queryParams: List[(String, String)] = List("chargeReference" -> chargeReference)
        connectorRequest(validRequest, validChargeHistoryResponseObject, queryParams, false)
      }
    }

    "the feature switch is enabled (HIP enabled)" must {
      "return a valid response" in new HipTestWithAdditionalContactHeaders {

        MockedSharedAppConfig.featureSwitchConfig returns Configuration("ifs_hip_migration_1554.enabled" -> true)
        connectorRequest(validRequest, validChargeHistoryResponseObject, hipQueryParams, true)
      }
    }
  }

  private trait HipTestWithAdditionalContactHeaders extends HipTest with Test {

    override val additionalContractHeaders: Seq[(String, String)] = List(
      "X-Message-Type"        -> "ETMPGetChargeHistory",
      "X-Originating-System"  -> "MDTP",
      "X-Receipt-Type"        -> "ITSA",
      "X-Receipt-Date"        -> isoDateTimeStamp,
      "X-Transmitting-System" -> "HIP"
    )

  }

}
