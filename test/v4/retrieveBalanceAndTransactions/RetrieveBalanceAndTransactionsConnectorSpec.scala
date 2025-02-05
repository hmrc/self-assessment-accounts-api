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

package v4.retrieveBalanceAndTransactions

import shared.connectors.{ConnectorSpec, DownstreamOutcome}
import shared.models.domain.{DateRange, Nino}
import shared.models.outcomes.ResponseWrapper
import v4.retrieveBalanceAndTransactions.def1.model.BalanceDetailsFixture.balanceDetails
import v4.retrieveBalanceAndTransactions.def1.model.CodingDetailsFixture.codingDetails
import v3.retrieveBalanceAndTransactions.def1.model.DocumentDetailsFixture.{documentDetails, documentDetailsWithoutDocDueDate}
import v3.retrieveBalanceAndTransactions.def1.model.FinancialDetailsFixture.financialDetailsFull
import v3.retrieveBalanceAndTransactions.model.request.RetrieveBalanceAndTransactionsRequestData
import v3.retrieveBalanceAndTransactions.model.response.RetrieveBalanceAndTransactionsResponse

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

  private val commonQueryParams: Seq[(String, String)] = List(
    "onlyOpenItems"              -> onlyOpenItems.toString,
    "includeLocks"               -> includeLocks.toString,
    "calculateAccruedInterest"   -> calculateAccruedInterest.toString,
    "removePOA"                  -> removePOA.toString,
    "customerPaymentInformation" -> customerPaymentInformation.toString,
    "includeStatistical"         -> includeEstimatedCharges.toString
  )

  "RetrieveBalanceAndTransactionsConnector" should {

    "return a valid response" when {

      "a valid request containing both docNumber and fromDate and dateTo is supplied" in new IfsTest with Test {
        val queryParams: Seq[(String, String)] =
          commonQueryParams ++ List(
            "docNumber" -> docNumber,
            "dateFrom"  -> fromDate,
            "dateTo"    -> toDate
          )

        connectorRequest(validRequest, validResponse, queryParams)
      }

      "a valid request containing docNumber and not fromDate or dateTo is supplied" in new IfsTest with Test {
        val request: RetrieveBalanceAndTransactionsRequestData = validRequest.copy(fromAndToDates = None)

        val queryParams: Seq[(String, String)] =
          commonQueryParams ++ List("docNumber" -> docNumber)

        connectorRequest(request, validResponse, queryParams)
      }

      "a valid request containing fromDate and dateTo and no docNumber is supplied" in new IfsTest with Test {
        val request: RetrieveBalanceAndTransactionsRequestData = validRequest.copy(docNumber = None)

        val queryParams: Seq[(String, String)] =
          commonQueryParams ++ List(
            "dateFrom" -> fromDate,
            "dateTo"   -> toDate
          )

        connectorRequest(request, validResponse, queryParams)
      }
    }
  }

  private trait Test { _: ConnectorTest =>

    private val connector: RetrieveBalanceAndTransactionsConnector =
      new RetrieveBalanceAndTransactionsConnector(mockHttpClient, mockSharedAppConfig)

    def connectorRequest(request: RetrieveBalanceAndTransactionsRequestData,
                         response: RetrieveBalanceAndTransactionsResponse,
                         queryParams: Seq[(String, String)]): Unit = {

      val outcome = Right(ResponseWrapper(correlationId, response))

      willGet(
        url = s"$baseUrl/enterprise/02.00.00/financial-data/NINO/$nino/ITSA",
        parameters = queryParams
      ).returns(Future.successful(outcome))

      val result: DownstreamOutcome[RetrieveBalanceAndTransactionsResponse] = await(connector.retrieveBalanceAndTransactions(request))
      result shouldBe outcome
    }

  }

}
