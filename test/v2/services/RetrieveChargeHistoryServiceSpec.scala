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

package v2.services

import api.models.domain.{Nino, TransactionId}
import api.models.errors.{DownstreamErrorCode, DownstreamErrors, MtdError, _}
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v2.mocks.connectors.MockRetrieveChargeHistoryConnector
import v2.models.request.retrieveChargeHistory.RetrieveChargeHistoryRequestData
import v2.models.response.retrieveChargeHistory.{ChargeHistoryDetail, RetrieveChargeHistoryResponse}

import scala.concurrent.Future

class RetrieveChargeHistoryServiceSpec extends ServiceSpec {

  val chargeHistoryDetails: ChargeHistoryDetail =
    ChargeHistoryDetail(
      taxYear = Some("2019-20"),
      transactionId = "X123456790A",
      transactionDate = "2019-06-01",
      description = "Balancing Charge Debit",
      totalAmount = 600.01,
      changeDate = "2019-06-05",
      changeReason = "Example reason"
    )

  val retrieveChargeHistoryResponse: RetrieveChargeHistoryResponse =
    RetrieveChargeHistoryResponse(
      chargeHistoryDetails = Seq(chargeHistoryDetails)
    )

  private val nino          = Nino("AA123456A")
  private val transactionId = TransactionId("anId")

  private val requestData: RetrieveChargeHistoryRequestData =
    RetrieveChargeHistoryRequestData(
      nino = nino,
      transactionId = transactionId
    )

  "RetrieveChargeHistoryService" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockRetrieveChargeHistoryConnector
          .retrieveChargeHistory(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveChargeHistoryResponse))))

        private val result = await(service.retrieveChargeHistory(requestData))
        result shouldBe Right(ResponseWrapper(correlationId, retrieveChargeHistoryResponse))
      }
    }
  }

  "unsuccessful" must {
    "map errors according to spec" when {

      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockRetrieveChargeHistoryConnector
            .retrieveChargeHistory(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.retrieveChargeHistory(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors: Seq[(String, MtdError)] =
        Seq(
          "INVALID_CORRELATIONID" -> InternalError,
          "INVALID_IDTYPE"        -> InternalError,
          "INVALID_IDVALUE"       -> NinoFormatError,
          "INVALID_REGIME_TYPE"   -> InternalError,
          "INVALID_DOC_NUMBER"    -> TransactionIdFormatError,
          "INVALID_DATE_FROM"     -> InternalError,
          "INVALID_DATE_TO"       -> InternalError,
          "INVALID_DATE_RANGE"    -> InternalError,
          "INVALID_REQUEST"       -> InternalError,
          "REQUEST_NOT_PROCESSED" -> InternalError,
          "NO_DATA_FOUND"         -> NotFoundError,
          "SERVER_ERROR"          -> InternalError,
          "SERVICE_UNAVAILABLE"   -> InternalError
        )

      errors.foreach(args => (serviceError _).tupled(args))
    }
  }

  trait Test extends MockRetrieveChargeHistoryConnector {

    val service = new RetrieveChargeHistoryService(
      connector = mockRetrieveChargeHistoryConnector
    )

  }

}
