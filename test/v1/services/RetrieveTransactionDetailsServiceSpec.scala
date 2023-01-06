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

package v1.services

import api.models.domain.Nino
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v1.mocks.connectors.MockRetrieveTransactionDetailsConnector
import v1.models.request.retrieveTransactionDetails.RetrieveTransactionDetailsParsedRequest
import v1.models.response.retrieveTransactionDetails.{RetrieveTransactionDetailsResponse, SubItem, TransactionItem}

import scala.concurrent.Future

class RetrieveTransactionDetailsServiceSpec extends ServiceSpec {

  trait Test extends MockRetrieveTransactionDetailsConnector {

    private val transactionId = "0001"
    private val nino          = "AA123456A"

    val requestData: RetrieveTransactionDetailsParsedRequest = RetrieveTransactionDetailsParsedRequest(
      nino = Nino(nino),
      transactionId = transactionId
    )

    val service = new RetrieveTransactionDetailsService(
      connector = mockRetrieveTransactionDetailsConnector
    )

  }

  "RetrieveTransactionDetailsService" when {
    "retrieveTransactionDetails" must {

      "return a successful response" when {
        "received a valid response for the supplied request" in new Test {

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

          val response = Right(ResponseWrapper(correlationId, responseModel))

          MockRetrieveTransactionDetailsConnector
            .retrieveDetails(requestData)
            .returns(Future.successful(response))

          await(service.retrieveTransactionDetails(requestData)) shouldBe response
        }
      }

      "return NoTransactionDetailsFoundError response" when {
        "the transactionItems are empty" in new Test {

          val responseModel: RetrieveTransactionDetailsResponse = RetrieveTransactionDetailsResponse(
            transactionItems = Seq.empty[TransactionItem]
          )

          MockRetrieveTransactionDetailsConnector
            .retrieveDetails(requestData)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

          await(service.retrieveTransactionDetails(requestData)) shouldBe Left(ErrorWrapper(correlationId, NoTransactionDetailsFoundError, None))
        }
      }

      "return error response" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned" in new Test {

            MockRetrieveTransactionDetailsConnector
              .retrieveDetails(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

            await(service.retrieveTransactionDetails(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input: Seq[(String, MtdError)] = Seq(
          ("INVALID_IDTYPE", InternalError),
          ("INVALID_IDNUMBER", NinoFormatError),
          ("INVALID_REGIME_TYPE", InternalError),
          ("INVALID_DOC_NUMBER", TransactionIdFormatError),
          ("INVALID_ONLY_OPEN_ITEMS", InternalError),
          ("INVALID_INCLUDE_LOCKS", InternalError),
          ("INVALID_CALCULATE_ACCRUED_INTEREST", InternalError),
          ("INVALID_CUSTOMER_PAYMENT_INFORMATION", InternalError),
          ("INVALID_DATE_FROM", InternalError),
          ("INVALID_DATE_TO", InternalError),
          ("INVALID_DATE_RANGE", InternalError),
          ("INVALID_INCLUDE_STATISTICAL", InternalError),
          ("INVALID_REQUEST", InternalError),
          ("INVALID_REMOVE_PAYMENT_ON_ACCOUNT", InternalError),
          ("REQUEST_NOT_PROCESSED", InternalError),
          ("NO_DATA_FOUND", NotFoundError),
          ("SERVER_ERROR", InternalError),
          ("SERVICE_UNAVAILABLE", InternalError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
