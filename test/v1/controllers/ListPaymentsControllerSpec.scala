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

import mocks.MockAppConfig
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.hateoas.HateoasLinks
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockListPaymentsRequestDataParser
import v1.mocks.services.{MockEnrolmentsAuthService, MockListPaymentsService, MockMtdIdLookupService}
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.listPayments.{ListPaymentsParsedRequest, ListPaymentsRawRequest}
import v1.fixtures.ListPaymentsFixture._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListPaymentsControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockListPaymentsRequestDataParser
  with MockListPaymentsService
  with MockHateoasFactory
  with MockAppConfig
  with HateoasLinks {

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new ListPaymentsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  private val nino          = "AA123456A"
  private val from       = "2018-10-1"
  private val to       = "2019-10-1"
  private val correlationId = "X-123"
  private val rawRequest = ListPaymentsRawRequest(nino, Some(from), Some(to))
  private val parsedRequest = ListPaymentsParsedRequest(Nino(nino), from, to)

  "retrieveList" should {
    "return a valid payments response" when {
      "a request sent has valid details" in new Test {

        MockListPaymentsRequestDataParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockListPaymentsService
          .listPayments(parsedRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, mtdResponseObj))))

        val result: Future[Result] = controller.retrieveList(nino, Some(from), Some(to))(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockListPaymentsRequestDataParser
              .parse(rawRequest)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.retrieveList(nino, Some(from), Some(to))(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (FromDateFormatError, BAD_REQUEST),
          (ToDateFormatError, BAD_REQUEST),
          (MissingFromDateError, BAD_REQUEST),
          (MissingToDateError, BAD_REQUEST),
          (RuleDateRangeInvalidError, BAD_REQUEST),
          (RuleFromDateNotSupportedError, BAD_REQUEST),
          (RangeToDateBeforeFromDateError, BAD_REQUEST),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockListPaymentsRequestDataParser
              .parse(rawRequest)
              .returns(Right(parsedRequest))

            MockListPaymentsService
              .listPayments(parsedRequest)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.retrieveList(nino, Some(from), Some(to))(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (FromDateFormatError, BAD_REQUEST),
          (ToDateFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (NoPaymentsFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
