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

import shared.connectors.ConnectorSpec
import shared.models.domain.Nino
import shared.models.outcomes.ResponseWrapper
import v3.common.models.ChargeReference
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

  trait Test  { _: ConnectorTest =>

    val connector: RetrieveChargeHistoryByChargeReferenceConnector =
      new RetrieveChargeHistoryByChargeReferenceConnector(http = mockHttpClient, appConfig = mockSharedAppConfig)



  }

  "RetrieveChargeHistoryConnector" when {
    "retrieveChargeHistory" must {
      "return a valid response" in new IfsTest with Test {

        val request: RetrieveChargeHistoryByChargeReferenceRequestData =
          Def1_RetrieveChargeHistoryByChargeReferenceRequestData(Nino(nino), ChargeReference(chargeReference))
        private val outcome = Right(ResponseWrapper(correlationId, retrieveChargeHistoryResponse))

//        MockedHttpClient
//          .get(
//            s"$baseUrl/cross-regime/charges/NINO/$nino/ITSA",
//            dummyHeaderCarrierConfig,
//            parameters = List("chargeReference" -> chargeReference),
//            ???,
//            List("AnotherHeader" -> "HeaderValue")
//          )
//          .returns(Future.successful(outcome))
        willGet(
          url = s"$baseUrl/cross-regime/charges/NINO/$nino/ITSA",
          parameters = List("chargeReference" -> chargeReference)
        ).returns(Future.successful(outcome))
        await(connector.retrieveChargeHistoryByChargeReference(request)) shouldBe outcome
      }
    }
  }

}
