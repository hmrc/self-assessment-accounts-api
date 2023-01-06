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

package v1.mocks.connectors

import api.connectors.DownstreamOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import uk.gov.hmrc.http.HeaderCarrier
import v1.connectors.RetrieveAllocationsConnector
import v1.models.request.retrieveAllocations.RetrieveAllocationsParsedRequest
import v1.models.response.retrieveAllocations.RetrieveAllocationsResponse
import v1.models.response.retrieveAllocations.detail.AllocationDetail

import scala.concurrent.{ExecutionContext, Future}

trait MockRetrieveAllocationsConnector extends MockFactory {

  val mockRetrieveAllocationsConnector: RetrieveAllocationsConnector = mock[RetrieveAllocationsConnector]

  object MockRetrieveAllocationsConnector {

    def retrieve(
        requestData: RetrieveAllocationsParsedRequest): CallHandler[Future[DownstreamOutcome[RetrieveAllocationsResponse[AllocationDetail]]]] = {
      (mockRetrieveAllocationsConnector
        .retrieveAllocations(_: RetrieveAllocationsParsedRequest)(_: HeaderCarrier, _: ExecutionContext, _: String))
        .expects(requestData, *, *, *)
    }

  }

}
