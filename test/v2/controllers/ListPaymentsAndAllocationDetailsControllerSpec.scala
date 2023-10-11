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

package v2.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.domain.Nino
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.mvc.Result
import v2.fixtures.listPaymentsAndAllocationDetails.ResponseFixtures._
import v2.mocks.requestParsers.MockListPaymentsAndAllocationDetailsRequestParser
import v2.mocks.services.MockListPaymentsAndAllocationDetailsService
import v2.models.request.listPaymentsAndAllocationDetails.{ListPaymentsAndAllocationDetailsRawData, ListPaymentsAndAllocationDetailsRequestData}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListPaymentsAndAllocationDetailsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockListPaymentsAndAllocationDetailsRequestParser
    with MockListPaymentsAndAllocationDetailsService {

  private val rawRequest = ListPaymentsAndAllocationDetailsRawData(nino, Some("fromDate"), Some("toDate"), Some("paymentLot"), Some("paymentLotItem"))

  private val parsedRequest =
    ListPaymentsAndAllocationDetailsRequestData(Nino(nino), Some("fromDate"), Some("toDate"), Some("paymentLot"), Some("paymentLotItem"))

  "retrieveList" should {
    "return a payments and allocation details response" when {
      "the request is valid" in new Test {
        MockListPaymentsAndAllocationDetailsRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockListPaymentsAndAllocationDetailsService
          .listPaymentsAndAllocationDetails(parsedRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseObject))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponseJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockListPaymentsAndAllocationDetailsRequestParser
          .parse(rawRequest)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockListPaymentsAndAllocationDetailsRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockListPaymentsAndAllocationDetailsService
          .listPaymentsAndAllocationDetails(parsedRequest)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, PaymentLotFormatError))))

        runErrorTest(PaymentLotFormatError)
      }
    }
  }

  private trait Test extends ControllerTest {

    private val controller = new ListPaymentsAndAllocationDetailsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockListPaymentsAndAllocationDetailsRequestParser,
      service = mockListPaymentsAndAllocationDetailsService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] =
      controller.listPayments(nino, Some("fromDate"), Some("toDate"), Some("paymentLot"), Some("paymentLotItem"))(fakeGetRequest)

  }

}
