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

package v1.controllers

import api.controllers.ControllerBaseSpec
import api.hateoas.HateoasLinks
import api.mocks.MockIdGenerator
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.hateoas.{HateoasWrapper, Link}
import play.api.libs.json.Json
import play.api.mvc.Result
import api.models.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.fixtures.ListPaymentsFixture._
import v1.mocks.requestParsers.MockListPaymentsRequestParser
import v1.mocks.services.MockListPaymentsService
import api.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.errors._
import api.models.hateoas.Method.GET
import api.models.hateoas.RelType.{LIST_TRANSACTIONS, RETRIEVE_PAYMENT_ALLOCATIONS, SELF}
import api.models.outcomes.ResponseWrapper
import v1.models.request.listPayments._
import v1.models.response.listPayments.{ListPaymentsHateoasData, ListPaymentsResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListPaymentsControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockListPaymentsRequestParser
    with MockListPaymentsService
    with MockHateoasFactory
    with HateoasLinks
    with MockAuditService
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val from          = "2018-10-01"
  private val to            = "2019-10-01"
  private val correlationId = "X-123"
  private val rawRequest    = ListPaymentsRawRequest(nino, Some(from), Some(to))
  private val parsedRequest = ListPaymentsParsedRequest(Nino(nino), from, to)

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new ListPaymentsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockListPaymentsRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  def event(auditResponse: AuditResponse): AuditEvent[GenericAuditDetail] =
    AuditEvent(
      auditType = "listSelfAssessmentPayments",
      transactionName = "list-self-assessment-payments",
      detail = GenericAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        params = Map("nino" -> nino),
        requestBody = None,
        auditResponse = auditResponse,
        `X-CorrelationId` = correlationId
      )
    )

  private val paymentHateoasLink1 =
    Link(
      href = "/accounts/self-assessment/AA123456A/payments/123456789012-123456",
      method = GET,
      rel = RETRIEVE_PAYMENT_ALLOCATIONS
    )

  private val paymentHateoasLink2 =
    Link(
      href = "/accounts/self-assessment/AA123456A/payments/223456789012-123456",
      method = GET,
      rel = RETRIEVE_PAYMENT_ALLOCATIONS
    )

  private val listPaymentsHateoasLink =
    Link(
      href = s"/accounts/self-assessment/AA123456A/payments?from=$from&to=$to",
      method = GET,
      rel = SELF
    )

  private val transactionsHateoasLink =
    Link(
      href = s"/accounts/self-assessment/AA123456A/transactions?from=$from&to=$to",
      method = GET,
      rel = LIST_TRANSACTIONS
    )

  private val hateoasResponse =
    ListPaymentsResponse(
      Seq(HateoasWrapper(payment1, Seq(paymentHateoasLink1)), HateoasWrapper(payment2, Seq(paymentHateoasLink2)))
    )

  "retrieveList" should {
    "return a valid payments response" when {
      "a request sent has valid details" in new Test {

        MockListPaymentsRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockListPaymentsService
          .listPayments(parsedRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, mtdResponseObj))))

        MockHateoasFactory
          .wrapList(mtdResponseObj, ListPaymentsHateoasData(nino, from, to))
          .returns(HateoasWrapper(hateoasResponse, Seq(listPaymentsHateoasLink, transactionsHateoasLink)))

        val result: Future[Result] = controller.listPayments(nino, Some(from), Some(to))(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, None)
        MockedAuditService.verifyAuditEvent(event(auditResponse)).once
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockListPaymentsRequestParser
              .parse(rawRequest)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.listPayments(nino, Some(from), Some(to))(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (V1_FromDateFormatError, BAD_REQUEST),
          (V1_ToDateFormatError, BAD_REQUEST),
          (V1_MissingFromDateError, BAD_REQUEST),
          (V1_MissingToDateError, BAD_REQUEST),
          (RuleDateRangeInvalidError, BAD_REQUEST),
          (RuleFromDateNotSupportedError, BAD_REQUEST),
          (V1_RangeToDateBeforeFromDateError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockListPaymentsRequestParser
              .parse(rawRequest)
              .returns(Right(parsedRequest))

            MockListPaymentsService
              .listPayments(parsedRequest)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.listPayments(nino, Some(from), Some(to))(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (V1_FromDateFormatError, BAD_REQUEST),
          (V1_ToDateFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
