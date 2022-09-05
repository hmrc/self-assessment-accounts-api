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

import play.api.libs.json.Json
import play.api.mvc.Result
import v1.models.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.fixtures.retrieveAllocations.RetrieveAllocationsResponseFixture
import v1.hateoas.HateoasLinks
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockRetrieveAllocationsRequestParser
import v1.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveAllocationsService}
import v1.models.audit.{GenericAuditDetail, AuditError, AuditEvent, AuditResponse}
import v1.models.errors._
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.hateoas.Method.GET
import v1.models.hateoas.RelType._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveAllocations.{RetrieveAllocationsParsedRequest, RetrieveAllocationsRawRequest}
import v1.models.response.retrieveAllocations.detail.AllocationDetail
import v1.models.response.retrieveAllocations.RetrieveAllocationsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveAllocationsControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveAllocationsService
    with MockHateoasFactory
    with MockRetrieveAllocationsRequestParser
    with HateoasLinks
    with MockAuditService
    with MockIdGenerator {

  private val nino           = "AA123456A"
  private val paymentId      = "aLot-anItem"
  private val paymentLot     = "aLot"
  private val paymentLotItem = "anItem"
  private val correlationId  = "X-123"

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

  private val paymentAllocationsLink =
    Link(
      href = s"/accounts/self-assessment/$nino/payments/$paymentId",
      method = GET,
      rel = SELF
    )

  private val chargeHateoasLink =
    Link(
      href = "/accounts/self-assessment/AA123456A/charges/someID",
      method = GET,
      rel = RETRIEVE_CHARGE_HISTORY
    )

  private val transactionDetailHateoasLink =
    Link(
      href = "/accounts/self-assessment/AA123456A/transactions/someID",
      method = GET,
      rel = RETRIEVE_TRANSACTION_DETAILS
    )

  private val retrieveAllocationsResponse = RetrieveAllocationsResponseFixture.paymentDetails
  private val mtdResponse                 = RetrieveAllocationsResponseFixture.mtdJsonWithHateoas

  private val hateoasResponse =
    retrieveAllocationsResponse.copy(
      allocations = Seq(
        HateoasWrapper(
          AllocationDetail(
            Some("someID"),
            Some("another date"),
            Some("an even later date"),
            Some("some type thing"),
            Some(600.00),
            Some(100.00)
          ),
          Seq(
            chargeHateoasLink,
            transactionDetailHateoasLink
          )
        )
      )
    )

  def event(auditResponse: AuditResponse): AuditEvent[GenericAuditDetail] =
    AuditEvent(
      auditType = "retrieveASelfAssessmentPaymentsAllocationDetails",
      transactionName = "retrieve-a-self-assessment-payments-allocation-details",
      detail = GenericAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        params = Map("nino" -> nino),
        requestBody = None,
        auditResponse = auditResponse,
        `X-CorrelationId` = correlationId
      )
    )

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveAllocationsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRetrieveAllocationsRequestParser,
      service = mockRetrieveAllocationsService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
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

        MockHateoasFactory
          .wrapList(retrieveAllocationsResponse, RetrieveAllocationsHateoasData(nino, paymentId))
          .returns(HateoasWrapper(hateoasResponse, Seq(paymentAllocationsLink)))

        val result: Future[Result] = controller.retrieveAllocations(nino, paymentId)(fakeGetRequest)

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

            MockRetrieveAllocationsRequestParser
              .parse(rawRequest)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.retrieveAllocations(nino, paymentId)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (PaymentIdFormatError, BAD_REQUEST)
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
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.retrieveAllocations(nino, paymentId)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
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
