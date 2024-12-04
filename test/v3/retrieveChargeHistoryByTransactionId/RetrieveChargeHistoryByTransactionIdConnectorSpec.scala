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

package v3.retrieveChargeHistoryByTransactionId

import api.connectors.{ConnectorSpec, MockHttpClient}
import api.models.domain.{Nino, TransactionId}
import api.models.outcomes.ResponseWrapper
import config.MockAppConfig
import org.scalamock.handlers.CallHandler0
import v3.retrieveChargeHistoryByTransactionId.def1.models.request.Def1_RetrieveChargeHistoryByTransactionIdRequestData
import v3.retrieveChargeHistoryByTransactionId.def1.models.response.ChargeHistoryDetail
import v3.retrieveChargeHistoryByTransactionId.model.request.RetrieveChargeHistoryByTransactionIdRequestData
import v3.retrieveChargeHistoryByTransactionId.model.response.RetrieveChargeHistoryResponse

import scala.concurrent.Future

class RetrieveChargeHistoryByTransactionIdConnectorSpec extends ConnectorSpec {

  val nino: String          = "AA123456A"
  val transactionId: String = "anId"

  val chargeHistoryDetails: ChargeHistoryDetail =
    ChargeHistoryDetail(
      taxYear = Some("2019-20"),
      transactionId = "X123456790A",
      transactionDate = "2019-06-01",
      description = "Balancing Charge Debit",
      totalAmount = 600.01,
      changeDate = "2019-06-05",
      changeReason = "Example reason",
      poaAdjustmentReason = Some("002")
    )

  val retrieveChargeHistoryResponse: RetrieveChargeHistoryResponse =
    RetrieveChargeHistoryResponse(
      chargeHistoryDetails = List(chargeHistoryDetails)
    )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: RetrieveChargeHistoryByTransactionIdConnector =
      new RetrieveChargeHistoryByTransactionIdConnector(http = mockHttpClient, appConfig = mockAppConfig)

    def setUpIfsMocks(): CallHandler0[Option[Seq[String]]] = {
      MockAppConfig.ifs1BaseUrl returns baseUrl
      MockAppConfig.ifs1Token returns "ifs1-token"
      MockAppConfig.ifs1Environment returns "ifs1-environment"
      MockAppConfig.ifs1EnvironmentHeaders returns Some(allowedIfs1Headers)
    }

  }

  "RetrieveChargeHistoryConnector" when {
    "retrieveChargeHistory" must {
      "return a valid response" in new Test {

        setUpIfsMocks()
        val request: RetrieveChargeHistoryByTransactionIdRequestData =
          Def1_RetrieveChargeHistoryByTransactionIdRequestData(Nino(nino), TransactionId(transactionId))
        private val outcome = Right(ResponseWrapper(correlationId, retrieveChargeHistoryResponse))

        MockedHttpClient
          .get(
            s"$baseUrl/cross-regime/charges/NINO/$nino/ITSA",
            dummyHeaderCarrierConfig,
            parameters = List("docNumber" -> transactionId),
            requiredIfs1Headers,
            List("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveChargeHistoryByTransactionId(request)) shouldBe outcome
      }
    }
  }

}
