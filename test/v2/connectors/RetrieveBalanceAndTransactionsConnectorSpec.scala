/*
 * Copyright 2022 HM Revenue & Customs
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

import api.models.domain.Nino
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import v2.mocks.MockHttpClient
import v2.models.request.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsRequest
import v2.models.response.retrieveBalanceAndTransactions._

import scala.concurrent.Future

class RetrieveBalanceAndTransactionsConnectorSpec extends ConnectorSpec {

  val nino: String = "AA123456A"

  val balanceDetails: BalanceDetails =
    BalanceDetails(
      payableAmount = -99999999999.99,
      payableDueDate = Some("1920-02-29"),
      pendingChargeDueAmount = -99999999999.99,
      pendingChargeDueDate = Some("1920-02-29"),
      overdueAmount = -99999999999.99,
      earliestPaymentDateOverDue = Some("1920-02-29"),
      totalBalance = -99999999999.99,
      amountCodedOut = Some(3456.67),
      bcdBalancePerYear = Some(Seq(balancePerYear)),
      totalBcdBalance = Some(2600.24),
      unallocatedCredit = Some(3456.67),
      allocatedCredit = Some(2345.67),
      totalCredit = Some(4567.67),
      firstPendingAmountRequested = Some(1234.67),
      secondPendingAmountRequested = Some(3100.67),
      availableCredit = Some(1212.67)
    )

  val balancePerYear: BalancePerYear =
    BalancePerYear(
      taxYear = Some("2022"),
      bcdAmount = Some(1300.12)
    )

  val documentDetails: DocumentDetails =
    DocumentDetails(
      taxYear = Some("2020-21"),
      documentId = "1455",
      formBundleNumber = Some("88888888"),
      creditReason = Some("Voluntary Payment"),
      documentDate = "2018-04-05",
      documentText = Some("ITSA- Bal Charge"),
      documentDueDate = "2021-04-05",
      documentDescription = Some("ITSA- POA 1"),
      originalAmount = 5009.99,
      outstandingAmount = 5009.99,
      lastClearing = Some(lastClearing),
      isStatistical = true,
      informationCode = Some("Coding Out"),
      paymentLot = Some("AB1023456789"),
      paymentLotItem = Some("000001"),
      effectiveDateOfPayment = Some("2021-04-05"),
      latePaymentInterest = Some(latePaymentInterest),
      amountCodedOut = Some(5009.99),
      reducedCharge = Some(reducedCharge)
    )

  val lastClearing: LastClearing =
    LastClearing(
      lastClearingDate = Some("2018-04-05"),
      lastClearingReason = Some("Incoming Payment"),
      lastClearedAmount = Some(5009.99)
    )

  val latePaymentInterest: LatePaymentInterest =
    LatePaymentInterest(
      latePaymentInterestId = Some("1234567890123456"),
      accruingInterestAmount = Some(5009.99),
      interestRate = Some(1.23),
      interestStartDate = Some("2020-04-01"),
      interestEndDate = Some("2020-04-05"),
      interestAmount = Some(5009.99),
      interestDunningLockAmount = Some(5009.99),
      interestOutstandingAmount = Some(5009.99)
    )

  val reducedCharge: ReducedCharge =
    ReducedCharge(
      chargeType = Some("???"),
      documentNumber = Some("???"),
      amendmentDate = Some("2018-04-05"),
      taxYear = Some("2018")
    )

  val financeDetails: FinanceDetails =
    FinanceDetails(
      taxYear = "2016-17",
      documentId = "12345678901234568",
      chargeType = Some("PAYE"),
      mainType = Some("2100"),
      taxPeriodFrom = Some("2018-08-13"),
      taxPeriodTo = Some("2018-08-14"),
      contractAccountCategory = Some("02"),
      contractAccount = Some("X"),
      documentNumber = Some("1040000872"),
      documentNumberItem = Some("XM00"),
      chargeReference = Some("XM002610011594"),
      mainTransaction = Some("1234"),
      subTransaction = Some("5678"),
      originalAmount = Some(10000),
      outstandingAmount = Some(10000),
      clearedAmount = Some(10000),
      accruedInterest = Some(10000),
      items = Seq(financialDetailsItem)
    )

  val financialDetailsItem: FinancialDetailsItem =
    FinancialDetailsItem(
      subItem = Some("001"),
      dueDate = Some("2018-08-13"),
      amount = Some(10000),
      clearingDate = Some("2018-08-13"),
      clearingReason = Some("01"),
      outgoingPaymentMethod = Some("outgoing Payment"),
      paymentLock = Some("paymentLock"),
      clearingLock = Some("clearingLock"),
      interestLock = Some("interestLock"),
      dunningLock = Some("dunningLock"),
      isReturn = Some(true),
      paymentReference = Some("Ab12453535"),
      paymentAmount = Some(10000),
      paymentMethod = Some("Payment"),
      paymentLot = Some("81203010024"),
      paymentLotItem = Some("000001"),
      isStatistical = Some("G"),
      returnReason = Some("ABCA")
    )

  val retrieveBalanceAndTransactionsResponse: RetrieveBalanceAndTransactionsResponse =
    RetrieveBalanceAndTransactionsResponse(
      balanceDetails = balanceDetails,
      //codingDetails = Option[Seq[codingDetails]],
      documentDetails = Some(Seq(documentDetails)),
      financeDetails = Some(Seq(financeDetails))
    )

  class Test extends MockHttpClient with MockAppConfig {

    val connector: RetrieveBalanceAndTransactionsConnector =
      new RetrieveBalanceAndTransactionsConnector(http = mockHttpClient, appConfig = mockAppConfig)

    MockAppConfig.ifsBaseUrl returns baseUrl
    MockAppConfig.ifsToken returns "ifs-token"
    MockAppConfig.ifsEnvironment returns "ifs-environment"
    MockAppConfig.ifsEnvironmentHeaders returns Some(allowedIfsHeaders)
  }

  "RetrieveBalanceAndTransactionsConnector" when {
    "retrieveBalanceAndTransactions" must {
      val request: RetrieveBalanceAndTransactionsRequest = RetrieveBalanceAndTransactionsRequest(Nino(nino),) //add query params

      "return a valid response" in new Test {

        val outcome = Right(ResponseWrapper(correlationId, retrieveBalanceAndTransactionsResponse))

        MockHttpClient
          .parameterGet(
            s"$baseUrl/enterprise/02.00.00/financial-data/NINO/$nino/ITSA",
            Seq("docNumber" ->),
            dummyIfsHeaderCarrierConfig,
            requiredIfsHeaders,
            Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveBalanceAndTransactions(request)) shouldBe outcome
      }
    }
  }

}
