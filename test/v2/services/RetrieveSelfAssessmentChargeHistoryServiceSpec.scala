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

import uk.gov.hmrc.http.HeaderCarrier
import api.controllers.EndpointLogContext
import v2.mocks.connectors.MockRetrieveSelfAssessmentChargeHistoryConnector
import api.models.domain.Nino
import api.models.errors.{DownstreamErrorCode, DownstreamErrors, MtdError}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import v2.models.request.retrieveChargeHistory.RetrieveChargeHistoryRequest
import v2.models.response.retrieveChargeHistory.{ChargeHistoryDetail, RetrieveChargeHistoryResponse}

import scala.concurrent.Future

class RetrieveSelfAssessmentChargeHistoryServiceSpec extends ServiceSpec {

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

  trait Test extends MockRetrieveSelfAssessmentChargeHistoryConnector {

    implicit val hc: HeaderCarrier = HeaderCarrier()

    implicit val logContext: EndpointLogContext =
      EndpointLogContext("RetrieveSelfAssessmentChargeHistoryController", "RetrieveSelfAssessmentChargeHistory")

    val service = new RetrieveSelfAssessmentChargeHistoryService(
      connector = mockRetrieveSelfAssessmentChargeHistoryConnector
    )

  }

  "RetrieveSelfAssessmentChargeHistoryService" when {
    "service call successful" must {}
    "return mapped result" in new Test {

      val connectorResponse: RetrieveChargeHistoryResponse = retrieveChargeHistoryResponse

      MockRetrieveSelfAssessmentChargeHistoryConnector
        .retrieveSelfAssessmentChargeHistory(requestData)
        .returns(Future.successful(Right(ResponseWrapper(correlationId, connectorResponse))))

      await(service.retrieveChargeHistory(requestData)) shouldBe Right(ResponseWrapper(correlationId, connectorResponse))
    }
  }

  "unsuccessful" must {
    "map errors according to spec" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the service" in new Test {

          MockRetrieveSelfAssessmentChargeHistoryConnector
            .retrieveSelfAssessmentChargeHistory(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

          await(service.retrieveChargeHistory(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input: Seq[(String, MtdError)] = Seq(
        ("INVALID_CORRELATIONID", InternalError),
        ("INVALID_IDTYPE", InternalError),
        ("INVALID_IDVALUE", NinoFormatError),
        ("INVALID_REGIME_TYPE", InternalError),
        ("INVALID_DOC_NUMBER", TransactionIdFormatError),
        ("INVALID_DATE_FROM", InternalError),
        ("INVALID_DATE_TO", InternalError),
        ("INVALID_DATE_RANGE", InternalError),
        ("INVALID_REQUEST", InternalError),
        ("REQUEST_NOT_PROCESSED", InternalError),
        ("NO_DATA_FOUND", NotFoundError),
        ("SERVER_ERROR", InternalError),
        ("SERVICE_UNAVAILABLE", InternalError)
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }

}
