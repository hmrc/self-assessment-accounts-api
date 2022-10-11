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

package v2.controllers

import api.controllers.ControllerBaseSpec
import api.mocks.MockIdGenerator
import api.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.domain.Nino
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.requestParsers.MockListPaymentsAndAllocationDetailsRequestParser
import v2.mocks.services.MockListPaymentsAndAllocationDetailsService
import v2.fixtures.listPaymentsAndAllocationDetails.ResponseFixtures._
import v2.models.request.listPaymentsAndAllocationDetails.{ListPaymentsAndAllocationDetailsRawData, ListPaymentsAndAllocationDetailsRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListPaymentsAndAllocationDetailsControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockListPaymentsAndAllocationDetailsRequestParser
    with MockListPaymentsAndAllocationDetailsService
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val correlationId = "X-123"
  private val rawRequest = ListPaymentsAndAllocationDetailsRawData(nino, Some("fromDate"), Some("toDate"), Some("paymentLot"), Some("paymentLotItem"))

  private val parsedRequest =
    ListPaymentsAndAllocationDetailsRequest(Nino(nino), Some("fromDate"), Some("toDate"), Some("paymentLot"), Some("paymentLotItem"))

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new ListPaymentsAndAllocationDetailsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockListPaymentsAndAllocationDetailsRequestParser,
      service = mockListPaymentsAndAllocationDetailsService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  "retrieveList" should {
    "return a payments and allocation details response" when {
      "a request sent has valid details" in new Test {

        MockListPaymentsAndAllocationDetailsRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockListPaymentsAndAllocationDetailsService
          .listPaymentsAndAllocationDetails(parsedRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseObject))))

        val result: Future[Result] =
          controller.listPayments(nino, Some("fromDate"), Some("toDate"), Some("paymentLot"), Some("paymentLotItem"))(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe responseMtdJson
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockListPaymentsAndAllocationDetailsRequestParser
              .parse(rawRequest)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] =
              controller.listPayments(nino, Some("fromDate"), Some("toDate"), Some("paymentLot"), Some("paymentLotItem"))(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (DateFromFormatError, BAD_REQUEST),
          (DateToFormatError, BAD_REQUEST),
          (RuleDateToBeforeDateFromError, BAD_REQUEST),
          (MissingFromDateError, BAD_REQUEST),
          (MissingToDateError, BAD_REQUEST),
          (RuleDateRangeInvalidError, BAD_REQUEST),
          (PaymentLotFormatError, BAD_REQUEST),
          (PaymentLotItemFormatError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockListPaymentsAndAllocationDetailsRequestParser
              .parse(rawRequest)
              .returns(Right(parsedRequest))

            MockListPaymentsAndAllocationDetailsService
              .listPaymentsAndAllocationDetails(parsedRequest)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] =
              controller.listPayments(nino, Some("fromDate"), Some("toDate"), Some("paymentLot"), Some("paymentLotItem"))(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (DateFromFormatError, BAD_REQUEST),
          (DateToFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
