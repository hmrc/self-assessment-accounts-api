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
import v2.fixtures.listPaymentsAndAllocationDetails.ResponseFixtures.responseObject
import v2.mocks.connectors.MockListPaymentsAndAllocationDetailsConnector
import v2.models.request.listPaymentsAndAllocationDetails.ListPaymentsAndAllocationDetailsRequest
import v2.services.RetrieveBalanceAndTransactionsService.downstreamErrorMap

import scala.concurrent.Future

class ListPaymentsAndAllocationDetailsServiceSpec extends ServiceSpec {

  private val nino = "AA123456A"
  private val dateFrom = "2018-08-13"
  private val dateTo = "2019-08-13"
  private val paymentLot = "081203010024"
  private val paymentLotItem = "000001"

  private val validRequest: ListPaymentsAndAllocationDetailsRequest =
    ListPaymentsAndAllocationDetailsRequest(Nino(nino), Some(dateFrom), Some(dateTo), Some(paymentLot), Some(paymentLotItem))

  "ListPaymentsAndAllocationDetailsService" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockListPaymentsAndAllocationDetailsConnector
          .listPaymentsAndAllocationDetails(validRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseObject))))

        val result = await(service.listPaymentsAndAllocationDetails(validRequest))
        result shouldBe Right(ResponseWrapper(correlationId, responseObject))
      }
    }

    "map errors according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockListPaymentsAndAllocationDetailsConnector
            .listPaymentsAndAllocationDetails(validRequest)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          private val result = await(service.listPaymentsAndAllocationDetails(validRequest))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      downstreamErrorMap.foreach(args => (serviceError _).tupled(args))
    }

  }

  trait Test extends MockListPaymentsAndAllocationDetailsConnector {

    implicit val hc: HeaderCarrier = HeaderCarrier()

    implicit val logContext: EndpointLogContext =
      EndpointLogContext("ListPaymentsAndAllocationDetailsController", "ListPaymentsAndAllocationDetails")

    val service = new ListPaymentsAndAllocationDetailsService(
      connector = mockListPaymentsAndAllocationDetailsConnector
    )
  }
}