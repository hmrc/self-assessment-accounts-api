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
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.Nino
import api.models.errors._
import api.models.hateoas.Method.GET
import api.models.hateoas.RelType.{RETRIEVE_TRANSACTION_DETAILS, SELF}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.RetrieveChargeHistoryController
import v1.fixtures.RetrieveChargeHistoryFixture
import v1.mocks.requestParsers.MockRetrieveChargeHistoryRequestParser
import v1.mocks.services.MockRetrieveChargeHistoryService
import v1.models.request.retrieveChargeHistory.{RetrieveChargeHistoryParsedRequest, RetrieveChargeHistoryRawRequest}
import v1.models.response.retrieveChargeHistory.RetrieveChargeHistoryHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveChargeHistoryControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveChargeHistoryService
    with MockHateoasFactory
    with MockRetrieveChargeHistoryRequestParser
    with HateoasLinks
    with MockAuditService
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val transactionId = "anId"
  private val correlationId = "X-123"

  private val rawRequest: RetrieveChargeHistoryRawRequest =
    RetrieveChargeHistoryRawRequest(
      nino = nino,
      transactionId = transactionId
    )

  private val parsedRequest: RetrieveChargeHistoryParsedRequest =
    RetrieveChargeHistoryParsedRequest(
      nino = Nino(nino),
      transactionId = transactionId
    )

  val chargeHistoryLink: Link =
    Link(
      href = s"/accounts/self-assessment/$nino/charges/$transactionId",
      method = GET,
      rel = SELF
    )

  val transactionDetailsLink: Link =
    Link(
      href = s"/accounts/self-assessment/$nino/transactions/$transactionId",
      method = GET,
      rel = RETRIEVE_TRANSACTION_DETAILS
    )

  private val retrieveChargeHistoryResponse = RetrieveChargeHistoryFixture.retrieveChargeHistoryResponseMultiple
  private val mtdResponse                   = RetrieveChargeHistoryFixture.mtdResponseMultipleWithHateoas(nino, transactionId)

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveChargeHistoryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRetrieveChargeHistoryRequestParser,
      service = mockRetrieveChargeHistoryService,
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
      auditType = "retrieveASelfAssessmentChargesHistory",
      transactionName = "retrieve-a-self-assessment-charges-history",
      detail = GenericAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        params = Map("nino" -> nino),
        requestBody = None,
        auditResponse = auditResponse,
        `X-CorrelationId` = correlationId
      )
    )

  "retrieveChargeHistory" should {
    "return OK" when {
      "happy path" in new Test {

        MockRetrieveChargeHistoryRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockRetrieveChargeHistoryService
          .retrieveChargeHistory(parsedRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveChargeHistoryResponse))))

        MockHateoasFactory
          .wrap(retrieveChargeHistoryResponse, RetrieveChargeHistoryHateoasData(nino, transactionId))
          .returns(
            HateoasWrapper(
              retrieveChargeHistoryResponse,
              Seq(
                chargeHistoryLink,
                transactionDetailsLink
              )))

        val result: Future[Result] = controller.retrieveChargeHistory(nino, transactionId)(fakeGetRequest)

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

            MockRetrieveChargeHistoryRequestParser
              .parse(rawRequest)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.retrieveChargeHistory(nino, transactionId)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TransactionIdFormatError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockRetrieveChargeHistoryRequestParser
              .parse(rawRequest)
              .returns(Right(parsedRequest))

            MockRetrieveChargeHistoryService
              .retrieveChargeHistory(parsedRequest)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.retrieveChargeHistory(nino, transactionId)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TransactionIdFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
