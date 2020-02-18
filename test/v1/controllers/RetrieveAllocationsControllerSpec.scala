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

package v1.controllers

import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.fixtures.retrieveAllocations.RetrieveAllocationsResponseFixture
import v1.mocks.requestParsers.MockRetrieveAllocationsRequestParser
import v1.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveAllocationsService}
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveAllocations.{RetrieveAllocationsParsedRequest, RetrieveAllocationsRawRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveAllocationsControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveAllocationsService
    with MockRetrieveAllocationsRequestParser {

  private val nino = "AA123456A"
  private val paymentId = "anId-anotherId"
  private val paymentLot = "anId"
  private val paymentLotItem = "anotherId"
  private val correlationId = "X-123"

  private val rawRequest: RetrieveAllocationsRawRequest =
    RetrieveAllocationsRawRequest(
      nino = nino,
      paymentId = paymentId
    )

  private val parsedRequest: RetrieveAllocationsParsedRequest =
    RetrieveAllocationsParsedRequest(
      nino = Nino(nino),
      paymentLot = paymentLot,
      paymentLotItem = paymentLotItem
    )

  private val retrieveAllocationsResponse = RetrieveAllocationsResponseFixture.paymentDetails
  private val mtdResponse = RetrieveAllocationsResponseFixture.mtdJson

  trait Test {
    val hc = HeaderCarrier()

    val controller = new RetrieveAllocationsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRetrieveAllocationsRequestParser,
      service = mockRetrieveAllocationsService,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  "retrieveAllocations" should {
    "return OK" when {
      "happy path" in new Test {

        MockRetrieveAllocationsRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockRetrieveAllocationsService
          .retrieveAllocations(parsedRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveAllocationsResponse))))

        val result: Future[Result] = controller.retrieveAllocations(nino, paymentId)(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)

      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockRetrieveAllocationsRequestParser
              .parse(rawRequest)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.retrieveAllocations(nino, paymentId)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (PaymentIdFormatError, BAD_REQUEST),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockRetrieveAllocationsRequestParser
              .parse(rawRequest)
              .returns(Right(parsedRequest))

            MockRetrieveAllocationsService
              .retrieveAllocations(parsedRequest)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.retrieveAllocations(nino, paymentId)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (PaymentIdFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}