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
import v1.mocks.requestParsers.MockRetrieveTransactionDetailsRequestParser
import v1.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveTransactionDetailsService}
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveTransactionDetails.{RetrieveTransactionDetailsParsedRequest, RetrieveTransactionDetailsRawRequest}
import v1.fixtures.RetrieveTransactionDetailsFixture._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveTransactionDetailsControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockRetrieveTransactionDetailsRequestParser
  with MockRetrieveTransactionDetailsService
  with MockHateoasFactory
  with MockAppConfig
  with HateoasLinks {

  private val nino = "AA123456A"
  private val transactionId = "0001"
  private val correlationId = "X-123"
  private val rawRequest = RetrieveTransactionDetailsRawRequest(nino, transactionId)
  private val parsedRequest = RetrieveTransactionDetailsParsedRequest(Nino(nino), transactionId)

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveTransactionDetailsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRetrieveTransactionDetailsRequestParser,
      service = mockRetrieveTransactionDetailsService,
      hateoasFactory = mockHateoasFactory,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  "retrieveTransactionDetails" should {

    "return a valid TransactionDetails response" when {
      "a request sent has valid details" in new Test {

        MockRetrieveTransactionDetailsRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockRetrieveTransactionDetailsService
          .retrieveTransactionDetails(parsedRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, mtdResponse))))


        val result: Future[Result] = controller.retrieveTransactionDetails(nino, transactionId)(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdJson
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the correct errors" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int, multipleErrors: Option[Seq[MtdError]]): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockRetrieveTransactionDetailsRequestParser
              .parse(rawRequest)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.retrieveTransactionDetails(nino, transactionId)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST, None),
          (TransactionIdFormatError, BAD_REQUEST, None),
          (BadRequestError, BAD_REQUEST, Some(Seq(NinoFormatError, TransactionIdFormatError))),
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }
    }

    "service errors occur" must {
      def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
        s"a $mtdError error is returned from the service" in new Test {

          MockRetrieveTransactionDetailsRequestParser
            .parse(rawRequest)
            .returns(Right(parsedRequest))

          MockRetrieveTransactionDetailsService
            .retrieveTransactionDetails(parsedRequest)
            .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

          val result: Future[Result] = controller.retrieveTransactionDetails(nino, transactionId)(fakeGetRequest)

          status(result) shouldBe expectedStatus
          contentAsJson(result) shouldBe Json.toJson(mtdError)
          header("X-CorrelationId", result) shouldBe Some(correlationId)
        }
      }

      val input = Seq(
        (NinoFormatError, BAD_REQUEST),
        (TransactionIdFormatError, BAD_REQUEST),
        (NotFoundError, NOT_FOUND),
        (NoTransactionDetailsFoundError, NOT_FOUND),
        (DownstreamError, INTERNAL_SERVER_ERROR)
      )

      input.foreach(args => (serviceErrors _).tupled(args))
    }
  }

}
