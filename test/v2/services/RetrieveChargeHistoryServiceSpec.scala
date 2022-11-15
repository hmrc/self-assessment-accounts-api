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

package v2.services

import api.controllers.EndpointLogContext
import api.models.domain.Nino
import api.models.errors.{DownstreamErrorCode, DownstreamErrors, MtdError, _}
import api.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.connectors.MockRetrieveChargeHistoryConnector
import v2.models.request.retrieveChargeHistory.RetrieveChargeHistoryRequest
import v2.models.response.retrieveChargeHistory.{ChargeHistoryDetail, RetrieveChargeHistoryResponse}

import scala.concurrent.Future

class RetrieveChargeHistoryServiceSpec extends ServiceSpec {

  private val nino = Nino("AA123456A")

  private val requestData: RetrieveChargeHistoryRequest =
    RetrieveChargeHistoryRequest(
      nino = nino,
      transactionId = "anId"
    )

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
          "INVALID_CORRELATIONID" -> StandardDownstreamError,
          "INVALID_IDTYPE"        -> StandardDownstreamError,
          "INVALID_IDVALUE"       -> NinoFormatError,
          "INVALID_REGIME_TYPE"   -> StandardDownstreamError,
          "INVALID_DOC_NUMBER"    -> TransactionIdFormatError,
          "INVALID_DATE_FROM"     -> StandardDownstreamError,
          "INVALID_DATE_TO"       -> StandardDownstreamError,
          "INVALID_DATE_RANGE"    -> StandardDownstreamError,
          "INVALID_REQUEST"       -> StandardDownstreamError,
          "REQUEST_NOT_PROCESSED" -> StandardDownstreamError,
          "NO_DATA_FOUND"         -> NotFoundError,
          "SERVER_ERROR"          -> StandardDownstreamError,
          "SERVICE_UNAVAILABLE"   -> StandardDownstreamError
        )

      errors.foreach(args => (serviceError _).tupled(args))
    }
  }

  trait Test extends MockRetrieveChargeHistoryConnector {

    implicit val hc: HeaderCarrier = HeaderCarrier()

    implicit val logContext: EndpointLogContext =
      EndpointLogContext("RetrieveChargeHistoryController", "RetrieveChargeHistory")

    val service = new RetrieveChargeHistoryService(
      connector = mockRetrieveChargeHistoryConnector
    )

  }

}
