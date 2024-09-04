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

package v2.connectors

import config.MockAppConfig
import api.connectors.{ConnectorSpec, MockHttpClient}
import api.models.domain.{ChargeReference, Nino, TransactionId}
import api.models.outcomes.ResponseWrapper
import org.scalamock.handlers.CallHandler0
import play.api.Configuration
import v2.models.request.retrieveChargeHistory.RetrieveChargeHistoryRequestData
import v2.models.response.retrieveChargeHistory._

import scala.concurrent.Future

class RetrieveChargeHistoryConnectorSpec extends ConnectorSpec {

  val nino: String          = "AA123456A"
  val transactionId: String = "anId"
  val testChargeReference   = "TESTCHARGE1"

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

    val connector: RetrieveChargeHistoryConnector =
      new RetrieveChargeHistoryConnector(http = mockHttpClient, appConfig = mockAppConfig)

    def setUpIfsMocks(): CallHandler0[Option[Seq[String]]] = {
      MockAppConfig.featureSwitches returns Configuration("chargeReferencePoaAdjustmentChanges.enabled" -> true)
      MockAppConfig.ifs1BaseUrl returns baseUrl
      MockAppConfig.ifs1Token returns "ifs1-token"
      MockAppConfig.ifs1Environment returns "ifs1-environment"
      MockAppConfig.ifs1EnvironmentHeaders returns Some(allowedIfs1Headers)
    }

    def setUpDesMocks(): CallHandler0[Option[Seq[String]]] = {
      MockAppConfig.featureSwitches returns Configuration("chargeReferencePoaAdjustmentChanges.enabled" -> false)
      MockAppConfig.desBaseUrl returns baseUrl
      MockAppConfig.desToken returns "des-token"
      MockAppConfig.desEnvironment returns "des-environment"
      MockAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
    }

  }

  "RetrieveChargeHistoryConnector" when {
    "retrieveChargeHistory" must {

      "return a valid response" in new Test {

        setUpIfsMocks()
        val request: RetrieveChargeHistoryRequestData = RetrieveChargeHistoryRequestData(Nino(nino), TransactionId(transactionId), None)
        private val outcome                           = Right(ResponseWrapper(correlationId, retrieveChargeHistoryResponse))

        MockedHttpClient
          .get(
            s"$baseUrl/cross-regime/charges/NINO/$nino/ITSA",
            dummyHeaderCarrierConfig,
            parameters = List("docNumber" -> transactionId),
            requiredIfs1Headers,
            List("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveChargeHistory(request)) shouldBe outcome
      }

      "return a valid response when the charge reference is supplied" in new Test {

        setUpIfsMocks()
        val request: RetrieveChargeHistoryRequestData =
          RetrieveChargeHistoryRequestData(Nino(nino), TransactionId(transactionId), Some(ChargeReference(testChargeReference)))
        private val outcome = Right(ResponseWrapper(correlationId, retrieveChargeHistoryResponse))

        MockedHttpClient
          .get(
            s"$baseUrl/cross-regime/charges/NINO/$nino/ITSA",
            dummyHeaderCarrierConfig,
            parameters = List("docNumber" -> transactionId, "chargeReference" -> testChargeReference),
            requiredIfs1Headers,
            List("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveChargeHistory(request)) shouldBe outcome
      }

      "return a valid response using DES config" when {
        "isChargeReferencePoaAdjustmentChanges is false" in new Test {

          setUpDesMocks()
          val request: RetrieveChargeHistoryRequestData =
            RetrieveChargeHistoryRequestData(Nino(nino), TransactionId(transactionId), Some(ChargeReference(testChargeReference)))
          private val outcome = Right(ResponseWrapper(correlationId, retrieveChargeHistoryResponse))

          MockedHttpClient
            .get(
              s"$baseUrl/cross-regime/charges/NINO/$nino/ITSA",
              dummyHeaderCarrierConfig,
              parameters = List("docNumber" -> transactionId),
              requiredDesHeaders,
              List("AnotherHeader" -> "HeaderValue")
            )
            .returns(Future.successful(outcome))

          await(connector.retrieveChargeHistory(request)) shouldBe outcome
        }
      }
    }
  }

}
