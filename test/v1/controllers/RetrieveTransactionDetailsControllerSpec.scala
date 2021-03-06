/*
 * Copyright 2021 HM Revenue & Customs
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
import v1.models.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.hateoas.HateoasLinks
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockRetrieveTransactionDetailsRequestParser
import v1.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveTransactionDetailsService}
import v1.models.audit.{AuditDetail, AuditError, AuditEvent, AuditResponse}
import v1.models.errors._
import v1.models.hateoas.Method.GET
import v1.models.hateoas.RelType._
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveTransactionDetails.{RetrieveTransactionDetailsParsedRequest, RetrieveTransactionDetailsRawRequest}
import v1.models.response.retrieveTransactionDetails.{RetrieveTransactionDetailsHateoasData, RetrieveTransactionDetailsResponse, SubItem, TransactionItem}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveTransactionDetailsControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockRetrieveTransactionDetailsRequestParser
  with MockRetrieveTransactionDetailsService
  with MockHateoasFactory
  with MockAppConfig
  with HateoasLinks
  with MockAuditService
  with MockIdGenerator {

  private val nino = "AA123456A"
  private val transactionId = "11111"
  private val paymentId = "081203010024-000001"
  private val correlationId = "X-123"

  private val rawRequest = RetrieveTransactionDetailsRawRequest(nino, transactionId)
  private val parsedRequest = RetrieveTransactionDetailsParsedRequest(Nino(nino), transactionId)

  private val paymentTransaction = TransactionItem(
    transactionItemId = Some("2019-20"),
    `type` = Some("Payment On Account"),
    originalAmount = Some(12.34),
    outstandingAmount = Some(10.33),
    taxPeriodFrom = None,
    taxPeriodTo = None,
    dueDate = None,
    paymentMethod = None,
    paymentId = Some(paymentId),
    subItems = Seq(
      SubItem(
        subItemId = Some("001"),
        amount = Some(100.11),
        clearingDate = Some("2021-01-31"),
        clearingReason = Some("Incoming payment"),
        outgoingPaymentMethod = None,
        paymentAmount = Some(100.11),
        dueDate = None,
        paymentMethod = Some("BACS RECEIPTS"),
        paymentId = Some("P0101180112-000001")
      )
    )
  )
  private val mtdResponse = RetrieveTransactionDetailsResponse(transactionItems = Seq(paymentTransaction))

  private val paymentAllocationHateoasLink = Link(
    href = s"/accounts/self-assessment/$nino/payments/$paymentId",
    method = GET, rel = RETRIEVE_PAYMENT_ALLOCATIONS
  )

  private val transactionsHateoasLink = Link(
    href = "/accounts/self-assessment/AA123456A/transactions", method = GET, rel = SELF
  )

  private val hateoasResponse = RetrieveTransactionDetailsResponse(Seq(paymentTransaction))

  private val mtdJson = Json.parse(
    """
      |{
      |	"transactionItems": [{
      |		"transactionItemId": "2019-20",
      |		"type": "Payment On Account",
      |		"originalAmount": 12.34,
      |		"outstandingAmount": 10.33,
      |		"paymentId": "081203010024-000001",
      |		"subItems": [{
      |			"subItemId": "001",
      |			"amount": 100.11,
      |			"clearingReason": "Incoming payment",
      |			"paymentId": "P0101180112-000001",
      |			"clearingDate": "2021-01-31",
      |			"paymentMethod": "BACS RECEIPTS",
      |			"paymentAmount": 100.11
      |		}]
      |	}],
      |	"links": [{
      |		"href": "/accounts/self-assessment/AA123456A/transactions",
      |		"method": "GET",
      |		"rel": "self"
      |	}, {
      |		"href": "/accounts/self-assessment/AA123456A/payments/081203010024-000001",
      |		"method": "GET",
      |		"rel": "retrieve-payment-allocations"
      |	}]
      |}
    """.stripMargin
  )

  def event(auditResponse: AuditResponse): AuditEvent = AuditEvent(
    auditType = "retrieveASelfAssessmentTransactionsDetail",
    transactionName = "retrieve-a-self-assessment-transactions-detail",
    detail = AuditDetail(
      userType = "Individual",
      agentReferenceNumber = None,
      nino = nino,
      response = auditResponse,
      `X-CorrelationId` = correlationId
    )
  )

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveTransactionDetailsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      auditService = mockAuditService,
      requestParser = mockRetrieveTransactionDetailsRequestParser,
      service = mockRetrieveTransactionDetailsService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  "retrieveTransactionDetails" should {
    "return a valid transactions response" when {
      "a request sent has valid details" in new Test {

        MockRetrieveTransactionDetailsRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockRetrieveTransactionDetailsService
          .retrieveTransactionDetails(parsedRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, mtdResponse))))

        MockHateoasFactory
          .wrap(mtdResponse, RetrieveTransactionDetailsHateoasData(nino, transactionId, Some(paymentId)))
          .returns(HateoasWrapper(hateoasResponse,
            Seq(transactionsHateoasLink, paymentAllocationHateoasLink)))

        val result: Future[Result] = controller.retrieveTransactionDetails(nino, transactionId)(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdJson
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, None)
        MockedAuditService.verifyAuditEvent(event(auditResponse)).once
      }
    }

    "return the correct errors" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockRetrieveTransactionDetailsRequestParser
              .parse(rawRequest)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.retrieveTransactionDetails(nino, transactionId)(fakeGetRequest)

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
    }

    "service errors occur" must {
      def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
        s"a $mtdError error is returned from the service" in new Test {

          MockRetrieveTransactionDetailsRequestParser
            .parse(rawRequest)
            .returns(Right(parsedRequest))

          MockRetrieveTransactionDetailsService
            .retrieveTransactionDetails(parsedRequest)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

          val result: Future[Result] = controller.retrieveTransactionDetails(nino, transactionId)(fakeGetRequest)

          status(result) shouldBe expectedStatus
          contentAsJson(result) shouldBe Json.toJson(mtdError)
          header("X-CorrelationId", result) shouldBe Some(correlationId)

          val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
          MockedAuditService.verifyAuditEvent(event(auditResponse)).once
        }
      }

      val input = Seq(
        (NotFoundError, NOT_FOUND),
        (NoTransactionDetailsFoundError, NOT_FOUND),
        (DownstreamError, INTERNAL_SERVER_ERROR)
      )

      input.foreach(args => (serviceErrors _).tupled(args))
    }
  }

}
