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

package v4.retrieveBalanceAndTransactions

import play.api.Configuration
import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{DateRange, Nino}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v4.retrieveBalanceAndTransactions.def1.model.BalanceDetailsFixture.balanceDetails
import v4.retrieveBalanceAndTransactions.def1.model.CodingDetailsFixture.codingDetails
import v4.retrieveBalanceAndTransactions.def1.model.DocumentDetailsFixture.{documentDetails, documentDetailsWithoutDocDueDate}
import v4.retrieveBalanceAndTransactions.def1.model.FinancialDetailsFixture.financialDetailsFull
import v4.retrieveBalanceAndTransactions.model.request.RetrieveBalanceAndTransactionsRequestData
import v4.retrieveBalanceAndTransactions.model.response.RetrieveBalanceAndTransactionsResponse

import java.time.LocalDate
import scala.concurrent.Future

class RetrieveBalanceAndTransactionsConnectorSpec extends ConnectorSpec {

  private val nino                       = "AA123456A"
  private val docNumber                  = "anId"
  private val fromDate                   = "2018-08-13"
  private val toDate                     = "2019-08-13"
  private val onlyOpenItems              = false
  private val includeLocks               = false
  private val calculateAccruedInterest   = false
  private val removePOA                  = false
  private val customerPaymentInformation = false
  private val includeEstimatedCharges    = false

  private val validResponse: RetrieveBalanceAndTransactionsResponse =
    RetrieveBalanceAndTransactionsResponse(
      balanceDetails = balanceDetails,
      codingDetails = Some(List(codingDetails)),
      documentDetails = Some(List(documentDetails, documentDetailsWithoutDocDueDate)),
      financialDetails = Some(List(financialDetailsFull))
    )

  private val validRequest: RetrieveBalanceAndTransactionsRequestData = RetrieveBalanceAndTransactionsRequestData(
    nino = Nino(nino),
    docNumber = Some(docNumber),
    Some(DateRange(LocalDate.parse(fromDate), LocalDate.parse(toDate))),
    onlyOpenItems = onlyOpenItems,
    includeLocks = includeLocks,
    calculateAccruedInterest = calculateAccruedInterest,
    removePOA = removePOA,
    customerPaymentInformation = customerPaymentInformation,
    includeEstimatedCharges = includeEstimatedCharges
  )

  private val commonQueryIfsParams: Seq[(String, String)] = List(
    "onlyOpenItems"              -> onlyOpenItems.toString,
    "includeLocks"               -> includeLocks.toString,
    "calculateAccruedInterest"   -> calculateAccruedInterest.toString,
    "removePOA"                  -> removePOA.toString,
    "customerPaymentInformation" -> customerPaymentInformation.toString,
    "includeStatistical"         -> includeEstimatedCharges.toString
  )

  private val commonQueryHipParams: Seq[(String, String)] = List(
    "onlyOpenItems"              -> onlyOpenItems.toString,
    "includeLocks"               -> includeLocks.toString,
    "calculateAccruedInterest"   -> calculateAccruedInterest.toString,
    "removePaymentonAccount"     -> removePOA.toString,
    "customerPaymentInformation" -> customerPaymentInformation.toString,
    "includeStatistical"         -> includeEstimatedCharges.toString,
    "regimeType"                 -> "ITSA",
    "idType"                     -> "NINO",
    "idNumber"                   -> nino
  )

  "RetrieveBalanceAndTransactionsConnector" when {

    "the feature switch is disabled (IFS enabled)" should {

    "return a valid response" when {

        "a valid request containing both docNumber and fromDate and dateTo is supplied" in new IfsTest with Test {

          val queryParams: Seq[(String, String)] =
            commonQueryIfsParams ++ List(
              "docNumber" -> docNumber,
              "dateFrom"  -> fromDate,
              "dateTo"    -> toDate
            )

          MockedSharedAppConfig.featureSwitchConfig returns Configuration("ifs_hip_migration_1553.enabled" -> false)
          connectorRequest(validRequest, validResponse, queryParams, false)
        }

        "a valid request containing docNumber and not fromDate or dateTo is supplied" in new IfsTest with Test {
          val request: RetrieveBalanceAndTransactionsRequestData = validRequest.copy(fromAndToDates = None)

          val queryParams: Seq[(String, String)] =
            commonQueryIfsParams ++ List("docNumber" -> docNumber)

          MockedSharedAppConfig.featureSwitchConfig returns Configuration("ifs_hip_migration_1553.enabled" -> false)
          connectorRequest(request, validResponse, queryParams, false)
        }

        "a valid request containing fromDate and dateTo and no docNumber is supplied" in new IfsTest with Test {
          val request: RetrieveBalanceAndTransactionsRequestData = validRequest.copy(docNumber = None)

          val queryParams: Seq[(String, String)] =
            commonQueryIfsParams ++ List(
              "dateFrom" -> fromDate,
              "dateTo"   -> toDate
            )

          MockedSharedAppConfig.featureSwitchConfig returns Configuration("ifs_hip_migration_1553.enabled" -> false)
          connectorRequest(request, validResponse, queryParams, false)
        }
      }
    }

    "the feature switch is enabled (HIP enabled)" should {

      "return a valid response" when {

        "a valid request containing both docNumber and fromDate and dateTo is supplied" in new HipEtmpTest with Test {

          val queryParams: Seq[(String, String)] =
            commonQueryHipParams ++ List(
              "sapDocumentNumber" -> docNumber,
              "dateFrom"  -> fromDate,
              "dateTo"    -> toDate
            )

          MockedSharedAppConfig.featureSwitchConfig returns Configuration("ifs_hip_migration_1553.enabled" -> true)
          connectorRequest(validRequest, validResponse, queryParams, true)
        }

        "a valid request containing docNumber and not fromDate or dateTo is supplied" in new HipEtmpTest with Test {
          val request: RetrieveBalanceAndTransactionsRequestData = validRequest.copy(fromAndToDates = None)

          val queryParams: Seq[(String, String)] =
            commonQueryHipParams ++ List("sapDocumentNumber" -> docNumber)

          MockedSharedAppConfig.featureSwitchConfig returns Configuration("ifs_hip_migration_1553.enabled" -> true)
          connectorRequest(request, validResponse, queryParams, true)
        }

        "a valid request containing fromDate and dateTo and no docNumber is supplied" in new HipEtmpTest with Test {
          val request: RetrieveBalanceAndTransactionsRequestData = validRequest.copy(docNumber = None)

          val queryParams: Seq[(String, String)] =
            commonQueryHipParams ++ List(
              "dateFrom" -> fromDate,
              "dateTo"   -> toDate
            )

          MockedSharedAppConfig.featureSwitchConfig returns Configuration("ifs_hip_migration_1553.enabled" -> true)
          connectorRequest(request, validResponse, queryParams, true)
        }
      }
    }
  }

  private trait Test { _: ConnectorTest =>

    private val connector: RetrieveBalanceAndTransactionsConnector =
      new RetrieveBalanceAndTransactionsConnector(mockHttpClient, mockSharedAppConfig)

    def connectorRequest(request: RetrieveBalanceAndTransactionsRequestData,
                         response: RetrieveBalanceAndTransactionsResponse,
                         queryParams: Seq[(String, String)],
                         hipTest: Boolean): Unit = {

      val outcome = Right(ResponseWrapper(correlationId, response))

      val url = if(hipTest) {
        url"$baseUrl/etmp/RESTAdapter/itsa/taxpayer/financial-details"
      } else {
        url"$baseUrl/enterprise/02.00.00/financial-data/NINO/$nino/ITSA"
      }
      willGet(
        url = url,
        parameters = queryParams
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[RetrieveBalanceAndTransactionsResponse] = await(connector.retrieveBalanceAndTransactions(request))
      result shouldBe outcome
    }

  }

}
