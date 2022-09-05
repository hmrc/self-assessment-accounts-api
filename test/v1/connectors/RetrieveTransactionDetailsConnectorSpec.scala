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

package v1.connectors

import api.connectors.ConnectorSpec
import api.mocks.MockHttpClient
import mocks.MockAppConfig
import uk.gov.hmrc.http.HeaderCarrier
import api.models.domain.Nino
import api.models.outcomes.ResponseWrapper
import v1.models.request.retrieveTransactionDetails.RetrieveTransactionDetailsParsedRequest
import v1.models.response.retrieveTransactionDetails.{RetrieveTransactionDetailsResponse, SubItem, TransactionItem}

import scala.concurrent.Future

class RetrieveTransactionDetailsConnectorSpec extends ConnectorSpec {

  class Test extends MockHttpClient with MockAppConfig {

    val transactionId: String = "0001"
    val nino: String          = "AA123456A"

    val requestData: RetrieveTransactionDetailsParsedRequest = RetrieveTransactionDetailsParsedRequest(
      nino = Nino(nino),
      transactionId = transactionId
    )

    val connector: RetrieveTransactionDetailsConnector = new RetrieveTransactionDetailsConnector(
      http = mockHttpClient,
      appConfig = mockAppConfig
    )

    val queryParams: Seq[(String, String)] = Seq(
      "docNumber"                  -> transactionId,
      "onlyOpenItems"              -> "false",
      "includeLocks"               -> "true",
      "calculateAccruedInterest"   -> "true",
      "removePOA"                  -> "false",
      "customerPaymentInformation" -> "true",
      "includeStatistical"         -> "false"
    )

    MockAppConfig.desBaseUrl returns baseUrl
    MockAppConfig.desToken returns "des-token"
    MockAppConfig.desEnvironment returns "des-environment"
    MockAppConfig.desEnvironmentHeaders returns Some(allowedDesHeaders)
  }

  "RetrieveTransactionDetailsConnector" when {
    "retrieveTransactionDetails (payment)" should {
      "return a valid response" in new Test {

        implicit val hc: HeaderCarrier = HeaderCarrier(otherHeaders = otherHeaders)

        val responseModel: RetrieveTransactionDetailsResponse = RetrieveTransactionDetailsResponse(
          transactionItems = Seq(
            TransactionItem(
              transactionItemId = Some("0001"),
              `type` = Some("Payment on account"),
              taxPeriodFrom = None,
              taxPeriodTo = None,
              originalAmount = Some(-5000),
              outstandingAmount = Some(0),
              dueDate = None,
              paymentMethod = None,
              paymentId = None,
              subItems = Seq(
                SubItem(
                  subItemId = Some("001"),
                  amount = None,
                  clearingDate = Some("2021-01-31"),
                  clearingReason = Some("Payment allocation"),
                  outgoingPaymentMethod = None,
                  paymentAmount = Some(-1100),
                  dueDate = None,
                  paymentMethod = None,
                  paymentId = None
                )
              )
            )
          )
        )

        val outcome = Right(ResponseWrapper(correlationId, responseModel))

        MockHttpClient
          .parameterGet(
            url = s"$baseUrl/enterprise/02.00.00/financial-data/NINO/$nino/ITSA",
            queryParams,
            dummyIfsHeaderCarrierConfig,
            requiredDesHeaders,
            Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveTransactionDetails(requestData)) shouldBe outcome
      }
    }

    "retrieveTransactionDetails (charges)" should {
      "return a valid response" in new Test {

        val responseModel: RetrieveTransactionDetailsResponse = RetrieveTransactionDetailsResponse(
          transactionItems = Seq(
            TransactionItem(
              transactionItemId = Some("0001"),
              `type` = Some("National Insurance Class 2"),
              taxPeriodFrom = Some("2019-04-06"),
              taxPeriodTo = Some("2020-04-05"),
              originalAmount = Some(100.45),
              outstandingAmount = Some(10.23),
              dueDate = None,
              paymentMethod = Some("BACS RECEIPTS"),
              paymentId = Some("P0101180112-000001"),
              subItems = Seq(
                SubItem(
                  subItemId = Some("001"),
                  amount = Some(100.11),
                  clearingDate = Some("2021-01-31"),
                  clearingReason = Some("Incoming payment"),
                  outgoingPaymentMethod = None,
                  paymentAmount = Some(100.11),
                  dueDate = None,
                  paymentMethod = Some("BACS RECEIPTS"),
                  paymentId = Some("P0101180112-000001")
                )
              )
            )
          )
        )

        val outcome = Right(ResponseWrapper(correlationId, responseModel))

        MockHttpClient
          .parameterGet(
            s"$baseUrl/enterprise/02.00.00/financial-data/NINO/$nino/ITSA",
            queryParams,
            dummyDesHeaderCarrierConfig,
            requiredDesHeaders,
            Seq("AnotherHeader" -> "HeaderValue")
          )
          .returns(Future.successful(outcome))

        await(connector.retrieveTransactionDetails(requestData)) shouldBe outcome
      }
    }
  }

}
