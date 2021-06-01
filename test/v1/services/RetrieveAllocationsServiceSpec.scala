/*
 * Copyright 2021 HM Revenue & Customs
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

import v1.models.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockRetrieveAllocationsConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveAllocations.RetrieveAllocationsParsedRequest
import v1.models.response.retrieveAllocations.RetrieveAllocationsResponse
import v1.models.response.retrieveAllocations.detail.AllocationDetail

import scala.concurrent.Future

class RetrieveAllocationsServiceSpec extends ServiceSpec {

  private val nino = "AA123456A"
  private val paymentLot = "anId"
  private val paymentLotItem = "anotherId"

  private val requestData: RetrieveAllocationsParsedRequest =
    RetrieveAllocationsParsedRequest(
      nino = Nino(nino),
      paymentLot = paymentLot,
      paymentLotItem = paymentLotItem
    )

  trait Test extends MockRetrieveAllocationsConnector {

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveAllocationsService(
      connector = mockRetrieveAllocationsConnector
    )
  }

  "service" when {
    "service call successsful" must {
      "return mapped result" in new Test {

        val connectorResponse: RetrieveAllocationsResponse[AllocationDetail] =
          RetrieveAllocationsResponse(
            amount = Some(100.5),
            method = Some("Beanz"),
            transactionDate = Some("31/2/2003"),
            allocations = Seq.empty[AllocationDetail]
          )

        MockRetrieveAllocationsConnector.retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, connectorResponse))))

        await(service.retrieveAllocations(requestData)) shouldBe Right(ResponseWrapper(correlationId, connectorResponse))
      }
    }

    "unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockRetrieveAllocationsConnector.retrieve(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.retrieveAllocations(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input: Seq[(String, MtdError)] = Seq(
          ("INVALID_CORRELATIONID", DownstreamError),
          ("INVALID_IDTYPE", DownstreamError),
          ("INVALID_IDVALUE", NinoFormatError),
          ("INVALID_REGIME_TYPE", DownstreamError),
          ("INVALID_PAYMENT_LOT", PaymentIdFormatError),
          ("INVALID_PAYMENT_LOT_ITEM", PaymentIdFormatError),
          ("INVALID_CLEARING_DOC", DownstreamError),
          ("INVALID_DATE_FROM", DownstreamError),
          ("INVALID_DATE_TO", DownstreamError),
          ("REQUEST_NOT_PROCESSED", DownstreamError),
          ("PARTIALLY_MIGRATED", DownstreamError),
          ("NO_DATA_FOUND", NotFoundError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}