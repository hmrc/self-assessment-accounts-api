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
import api.hateoas.HateoasLinks
import api.mocks.MockIdGenerator
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.errors._
import api.models.hateoas.Link
import api.models.hateoas.Method.GET
import api.models.hateoas.RelType.SELF
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.fixtures.retrieveBalanceAndTransactions.RequestFixture._
import v2.fixtures.retrieveBalanceAndTransactions.ResponseFixture.{mtdResponseJson, response}
import v2.mocks.requestParsers.MockRetrieveBalanceAndTransactionsRequestParser
import v2.mocks.services.MockRetrieveBalanceAndTransactionsService
import v2.models.request.retrieveBalanceAndTransactions.{RetrieveBalanceAndTransactionsRawData, RetrieveBalanceAndTransactionsRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveBalanceAndTransactionsControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveBalanceAndTransactionsService
    with MockHateoasFactory
    with MockRetrieveBalanceAndTransactionsRequestParser
    with HateoasLinks
    with MockIdGenerator {

  private val nino          = validNino
  private val correlationId = "X-123"

  private val rawRequest: RetrieveBalanceAndTransactionsRawData = inputDataEverythingTrue

  private val parsedRequest: RetrieveBalanceAndTransactionsRequest = requestEverythingTrue

  val BalanceAndTransactionsLink: Link =
    Link(
      href = s"/accounts/self-assessment/$nino/balance-and-transactions",
      method = GET,
      rel = SELF
    )

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveBalanceAndTransactionsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRetrieveBalanceAndTransactionsRequestParser,
      service = mockRetrieveBalanceAndTransactionsService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  "retrieveBalanceAndTransactions" should {
    "return OK" when {
      "happy path" in new Test {

        MockRetrieveBalanceAndTransactionsRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockRetrieveBalanceAndTransactionsService
          .retrieveBalanceAndTransactions(parsedRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        val result: Future[Result] = controller.retrieveBalanceAndTransactions(
          nino = nino,
          docNumber = Some(validDocNumber),
          fromDate = None,
          toDate = None,
          onlyOpenItems = Some("true"),
          includeLocks = Some("true"),
          calculateAccruedInterest = Some("true"),
          removePOA = Some("true"),
          customerPaymentInformation = Some("true"),
          includeEstimatedCharges = Some("true")
        )(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdResponseJson
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockRetrieveBalanceAndTransactionsRequestParser
              .parse(rawRequest)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.retrieveBalanceAndTransactions(
              nino = nino,
              docNumber = Some(validDocNumber),
              fromDate = None,
              toDate = None,
              onlyOpenItems = Some("true"),
              includeLocks = Some("true"),
              calculateAccruedInterest = Some("true"),
              removePOA = Some("true"),
              customerPaymentInformation = Some("true"),
              includeEstimatedCharges = Some("true")
            )(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (DocNumberFormatError, BAD_REQUEST),
          (OnlyOpenItemsFormatError, BAD_REQUEST),
          (IncludeLocksFormatError, BAD_REQUEST),
          (CalculateAccruedInterestFormatError, BAD_REQUEST),
          (CustomerPaymentInformationFormatError, BAD_REQUEST),
          (FromDateFormatError, BAD_REQUEST),
          (ToDateFormatError, BAD_REQUEST),
          (InvalidDateRangeError, BAD_REQUEST),
          (RuleInconsistentQueryParamsError, BAD_REQUEST),
          (RemovePaymentOnAccountFormatError, BAD_REQUEST),
          (IncludeEstimatedChargesFormatError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }
    }

    "service errors occur" must {
      def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
        s"a $mtdError error is returned from the service" in new Test {

          MockRetrieveBalanceAndTransactionsRequestParser
            .parse(rawRequest)
            .returns(Right(parsedRequest))

          MockRetrieveBalanceAndTransactionsService
            .retrieveBalanceAndTransactions(parsedRequest)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

          val result: Future[Result] = controller.retrieveBalanceAndTransactions(
            nino = nino,
            docNumber = Some(validDocNumber),
            fromDate = None,
            toDate = None,
            onlyOpenItems = Some("true"),
            includeLocks = Some("true"),
            calculateAccruedInterest = Some("true"),
            removePOA = Some("true"),
            customerPaymentInformation = Some("true"),
            includeEstimatedCharges = Some("true")
          )(fakeGetRequest)

          status(result) shouldBe expectedStatus
          contentAsJson(result) shouldBe Json.toJson(mtdError)
          header("X-CorrelationId", result) shouldBe Some(correlationId)

        }
      }

      val input = Seq(
        (NinoFormatError, BAD_REQUEST),
        (DocNumberFormatError, BAD_REQUEST),
        (OnlyOpenItemsFormatError, BAD_REQUEST),
        (IncludeLocksFormatError, BAD_REQUEST),
        (CalculateAccruedInterestFormatError, BAD_REQUEST),
        (CustomerPaymentInformationFormatError, BAD_REQUEST),
        (FromDateFormatError, BAD_REQUEST),
        (ToDateFormatError, BAD_REQUEST),
        (InvalidDateRangeError, BAD_REQUEST),
        (RuleInconsistentQueryParamsError, BAD_REQUEST),
        (RemovePaymentOnAccountFormatError, BAD_REQUEST),
        (IncludeEstimatedChargesFormatError, BAD_REQUEST),
        (NotFoundError, NOT_FOUND),
        (InternalError, INTERNAL_SERVER_ERROR)
      )

      input.foreach(args => (serviceErrors _).tupled(args))
    }

  }

}
