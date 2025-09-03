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

import shared.connectors.ConnectorSpec
import shared.models.domain.{Nino, TransactionId}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v4.retrieveChargeHistoryByTransactionId.def1.RetrieveChargeHistoryFixture.validChargeHistoryResponseObject
import v4.retrieveChargeHistoryByTransactionId.def1.models.request.Def1_RetrieveChargeHistoryByTransactionIdRequestData
import v4.retrieveChargeHistoryByTransactionId.model.request.RetrieveChargeHistoryByTransactionIdRequestData

import scala.concurrent.Future

class RetrieveChargeHistoryByTransactionIdConnectorSpec extends ConnectorSpec {

  val nino: String          = "AA123456A"
  val transactionId: String = "anId"

  trait Test {  _: ConnectorTest =>

    val connector: RetrieveChargeHistoryByTransactionIdConnector =
      new RetrieveChargeHistoryByTransactionIdConnector(http = mockHttpClient, appConfig = mockSharedAppConfig)
  }

  "RetrieveChargeHistoryByTransactionIdConnector" when {
    "retrieveChargeHistoryByTransactionId" must {
      "return a valid response" in new IfsTest with Test {

        val request: RetrieveChargeHistoryByTransactionIdRequestData =
          Def1_RetrieveChargeHistoryByTransactionIdRequestData(Nino(nino), TransactionId(transactionId))
        private val outcome = Right(ResponseWrapper(correlationId, validChargeHistoryResponseObject))

        willGet(
          url = url"$baseUrl/cross-regime/charges/NINO/$nino/ITSA",
          parameters = List("docNumber" -> transactionId)
        ).returns(Future.successful(outcome))

        await(connector.retrieveChargeHistoryByTransactionId(request)) shouldBe outcome
      }
    }
  }

}
