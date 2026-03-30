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

package v4.retrieveChargeHistoryByTransactionId

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{Nino, TransactionId}
import shared.models.outcomes.ResponseWrapper
import shared.utils.DateUtils.isoDateTimeStamp
import uk.gov.hmrc.http.StringContextOps
import v4.retrieveChargeHistoryByTransactionId.def1.RetrieveChargeHistoryFixture.validChargeHistoryResponseObject
import v4.retrieveChargeHistoryByTransactionId.def1.models.request.Def1_RetrieveChargeHistoryByTransactionIdRequestData
import v4.retrieveChargeHistoryByTransactionId.model.request.RetrieveChargeHistoryByTransactionIdRequestData
import v4.retrieveChargeHistoryByTransactionId.model.response.RetrieveChargeHistoryResponse

import scala.concurrent.Future

class RetrieveChargeHistoryByTransactionIdConnectorSpec extends ConnectorSpec {

  val nino: String          = "AA123456A"
  val transactionId: String = "anId"

  private trait Test {
    self: ConnectorTest =>

    private val connector: RetrieveChargeHistoryByTransactionIdConnector =
      new RetrieveChargeHistoryByTransactionIdConnector(mockHttpClient, mockSharedAppConfig)

    def connectorRequest(request: RetrieveChargeHistoryByTransactionIdRequestData,
                         response: RetrieveChargeHistoryResponse,
                         queryParams: Seq[(String, String)]): Unit = {

      val outcome = Right(ResponseWrapper(correlationId, response))

      val url = url"$baseUrl/etmp/RESTAdapter/ITSA/TaxPayer/GetChargeHistory"

      willGet(
        url = url,
        parameters = queryParams
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[RetrieveChargeHistoryResponse] = await(connector.retrieveChargeHistoryByTransactionId(request))
      result shouldBe outcome
    }

  }

  def hipQueryParams: Seq[(String, String)] =
    Seq(
      "idType"            -> "NINO",
      "idValue"           -> nino,
      "sapDocumentNumber" -> transactionId
    )

  private val validRequest: RetrieveChargeHistoryByTransactionIdRequestData = Def1_RetrieveChargeHistoryByTransactionIdRequestData(
    Nino(nino),
    TransactionId(transactionId)
  )

  private trait HipTestWithAdditionalContactHeaders extends HipTest with Test {

    override val additionalContractHeaders: Seq[(String, String)] = List(
      "X-Message-Type"        -> "ETMPGetChargeHistory",
      "X-Originating-System"  -> "MDTP",
      "X-Regime-Type"         -> "ITSA",
      "X-Receipt-Date"        -> isoDateTimeStamp,
      "X-Transmitting-System" -> "HIP"
    )

  }

  "RetrieveChargeHistoryByTransactionIdConnector" when {
    "return a valid response" in new HipTestWithAdditionalContactHeaders {
      connectorRequest(validRequest, validChargeHistoryResponseObject, hipQueryParams)
    }
  }

}
