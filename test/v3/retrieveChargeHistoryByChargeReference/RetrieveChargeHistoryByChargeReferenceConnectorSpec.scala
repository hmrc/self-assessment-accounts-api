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

package v3.retrieveChargeHistoryByChargeReference

import api.connectors.{ConnectorSpec, MockHttpClient}
import api.models.domain.{ChargeReference, Nino}
import api.models.outcomes.ResponseWrapper
import config.MockAppConfig
import org.scalamock.handlers.CallHandler0
import v3.retrieveChargeHistoryByChargeReference.def1.model.request.Def1_RetrieveChargeHistoryByChargeReferenceRequestData
import v3.retrieveChargeHistoryByChargeReference.def1.model.response.ChargeHistoryDetail
import v3.retrieveChargeHistoryByChargeReference.model.request.RetrieveChargeHistoryByChargeReferenceRequestData
import v3.retrieveChargeHistoryByChargeReference.model.response.RetrieveChargeHistoryResponse

import scala.concurrent.Future

class RetrieveChargeHistoryByChargeReferenceConnectorSpec extends ConnectorSpec {

  val nino: String            = "AA123456A"
  val chargeReference: String = "anId"

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

    val connector: RetrieveChargeHistoryByChargeReferenceConnector =
      new RetrieveChargeHistoryByChargeReferenceConnector(http = mockHttpClient, appConfig = mockAppConfig)

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
        val request: RetrieveChargeHistoryByChargeReferenceRequestData =
          Def1_RetrieveChargeHistoryByChargeReferenceRequestData(Nino(nino), ChargeReference(chargeReference))
        private val outcome = Right(ResponseWrapper(correlationId, retrieveChargeHistoryResponse))

        MockedHttpClient
          .get(
            s"$baseUrl/cross-regime/charges/NINO/$nino/ITSA",
            dummyHeaderCarrierConfig,
            parameters = List("chargeReference" -> chargeReference),
            requiredIfs1Headers,
            List("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveChargeHistoryByChargeReference(request)) shouldBe outcome
      }
    }
  }

}
