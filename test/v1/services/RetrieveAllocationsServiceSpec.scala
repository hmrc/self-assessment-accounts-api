/*
 * Copyright 2020 HM Revenue & Customs
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

import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockRetrieveAllocationsConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.requestData.RetrieveAllocationsRequest
import v1.models.response.RetrieveAllocationsResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveAllocationsServiceSpec extends UnitSpec {

  private val nino = "AA123456A"
  private val paymentId = "anId"
  private val correlationId = "X-123"

  private val requestData: RetrieveAllocationsRequest = RetrieveAllocationsRequest(Nino(nino), paymentId)

  trait Test extends MockRetrieveAllocationsConnector {

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new RetrieveAllocationsService(
      retrieveAllocationsConnector = mockRetrieveAllocationsConnector
    )
  }

  "service" when {
    "service call successsful" must {
      "return mapped result" in new Test {
        MockRetrieveAllocationsConnector.retrieve(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, RetrieveAllocationsResponse(100)))))

        await(service.retrieve(requestData)) shouldBe Right(ResponseWrapper(correlationId, RetrieveAllocationsResponse(100)))
      }
    }

    "unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockRetrieveAllocationsConnector.retrieve(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.retrieve(requestData)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
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
          ("NO_DATA_FOUND", NotFoundError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
