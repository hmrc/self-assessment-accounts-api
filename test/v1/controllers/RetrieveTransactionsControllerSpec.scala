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
import v1.mocks.requestParsers.MockRetrieveTransactionsRequestParser
import v1.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveTransactionsService}
import v1.models.audit.{AuditDetail, AuditError, AuditEvent, AuditResponse}
import v1.models.errors._
import v1.models.hateoas.Method.GET
import v1.models.hateoas.RelType._
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveTransactions.{RetrieveTransactionsParsedRequest, RetrieveTransactionsRawRequest}
import v1.models.response.retrieveTransaction.{RetrieveTransactionsHateoasData, RetrieveTransactionsResponse, TransactionItem}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveTransactionsControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockRetrieveTransactionsRequestParser
  with MockRetrieveTransactionsService
  with MockHateoasFactory
  with MockAppConfig
  with HateoasLinks
  with MockAuditService {

  private val nino = "AA123456A"
  private val chargeId = "X123456790A"
  private val paymentId = "081203010024-000001"
  private val from          = "2018-10-1"
  private val to            = "2019-10-1"
  private val correlationId = "X-123"
  private val rawRequest = RetrieveTransactionsRawRequest(nino, Some(from), Some(to))
  private val parsedRequest = RetrieveTransactionsParsedRequest(Nino(nino), from, to)

  private val chargeTransaction =
    TransactionItem(
      taxYear = Some("2019-20"),
      id = Some("X123456790A"),
      transactionDate = Some("2020-01-01"),
      `type` = Some("Balancing Charge Debit"),
      originalAmount = Some(12.34),
      outstandingAmount = Some(10.33),
      lastClearingDate = Some("2020-01-02"),
      lastClearingReason = Some("Refund"),
      lastClearedAmount = Some(2.01)
    )

  private val paymentTransaction =
    TransactionItem(
      taxYear = Some("2019-20"),
      id = Some("081203010024-000001"),
      transactionDate = Some("2020-01-01"),
      `type` = Some("Payment On Account"),
      originalAmount = Some(12.34),
      outstandingAmount = Some(10.33),
      lastClearingDate = Some("2020-01-02"),
      lastClearingReason = Some("Payment Allocation"),
      lastClearedAmount = Some(2.01)
    )

  private val mtdResponse = RetrieveTransactionsResponse(
    transactions = Seq(
      chargeTransaction,
      paymentTransaction
    )
  )

  private val chargeHistoryHateoasLink = Link(href = s"/accounts/self-assessment/$nino/charges/$chargeId", method = GET, rel = RETRIEVE_CHARGE_HISTORY)
  private val paymentAllocationHateoasLink =
    Link(href = s"/accounts/self-assessment/$nino/payments/$paymentId", method = GET, rel = RETRIEVE_PAYMENT_ALLOCATIONS)

  private val transactionsHateoasLink = Link(href = "/accounts/self-assessment/AA123456A/transactions", method = GET, rel = SELF)
  private val listPaymentsHateoasLink = Link(href = s"/accounts/self-assessment/$nino/payments", method = GET, rel = LIST_PAYMENTS)
  private val listChargesHateoasLink = Link(href = s"/accounts/self-assessment/$nino/charges", method = GET, rel = LIST_CHARGES)

  private val hateoasResponse = RetrieveTransactionsResponse(
    Seq(
      HateoasWrapper(
        payload = chargeTransaction,
        links = Seq(chargeHistoryHateoasLink)
      ),
      HateoasWrapper(
        payload = paymentTransaction,
        links = Seq(paymentAllocationHateoasLink)
      )
    )
  )

  private val mtdJson = Json.parse(
    """
      |{
      |   "transactions":[
      |      {
      |         "taxYear":"2019-20",
      |         "id":"X123456790A",
      |         "transactionDate":"2020-01-01",
      |         "type":"Balancing Charge Debit",
      |         "originalAmount":12.34,
      |         "outstandingAmount":10.33,
      |         "lastClearingDate":"2020-01-02",
      |         "lastClearingReason":"Refund",
      |         "lastClearedAmount":2.01,
      |         "links": [{
      |           "href": "/accounts/self-assessment/AA123456A/charges/X123456790A",
      |			      "method": "GET",
      |			      "rel": "retrieve-charge-history"
      |		      }]
      |      },
      |      {
      |         "taxYear":"2019-20",
      |         "id":"081203010024-000001",
      |         "transactionDate":"2020-01-01",
      |         "type":"Payment On Account",
      |         "originalAmount":12.34,
      |         "outstandingAmount":10.33,
      |         "lastClearingDate":"2020-01-02",
      |         "lastClearingReason":"Payment Allocation",
      |         "lastClearedAmount":2.01,
      |          "links": [{
      |             "href": "/accounts/self-assessment/AA123456A/payments/081203010024-000001",
      |			        "method": "GET",
      |			        "rel": "retrieve-payment-allocations"
      |		        }]
      |      }
      |   ],
      |   "links": [
      |      {
      |        "href": "/accounts/self-assessment/AA123456A/transactions",
      |			   "method": "GET",
      |			   "rel": "self"
      |		   },
      |      {
      |        "href": "/accounts/self-assessment/AA123456A/payments",
      |			   "method": "GET",
      |			   "rel": "list-payments"
      |		   },
      |      {
      |        "href": "/accounts/self-assessment/AA123456A/charges",
      |			   "method": "GET",
      |			   "rel": "list-charges"
      |		   }
      |     ]
      |
      |}
    """.stripMargin)

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveTransactionsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRetrieveTransactionsRequestParser,
      service = mockRetrieveTransactionsService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  def event(auditResponse: AuditResponse): AuditEvent =
    AuditEvent(
      auditType = "retrieveSelfAssessmentTransactions",
      listPayments = "retrieve-self-assessment-transactions",
      detail = AuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        nino = nino,
        response = auditResponse,
        `X-CorrelationId` = correlationId
      )
    )

  "retrieveTransactions" should {

    "return a valid transactions response" when {
      "a request sent has valid details" in new Test {

        MockRetrieveTransactionsRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockRetrieveTransactionsService
          .retrieveTransactions(parsedRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, mtdResponse))))

        MockHateoasFactory
          .wrapList(mtdResponse, RetrieveTransactionsHateoasData(nino))
          .returns(HateoasWrapper(hateoasResponse,
            Seq(transactionsHateoasLink, listPaymentsHateoasLink, listChargesHateoasLink)))

        val result: Future[Result] = controller.retrieveTransactions(nino, Some(from), Some(to))(fakeGetRequest)

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

            MockRetrieveTransactionsRequestParser
              .parse(rawRequest)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.retrieveTransactions(nino, Some(from), Some(to))(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (FromDateFormatError, BAD_REQUEST),
          (ToDateFormatError, BAD_REQUEST),
          (MissingFromDateError, BAD_REQUEST),
          (MissingToDateError, BAD_REQUEST),
          (RuleDateRangeInvalidError, BAD_REQUEST),
          (RuleFromDateNotSupportedError, BAD_REQUEST),
          (RangeToDateBeforeFromDateError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }
    }

    "service errors occur" must {
      def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
        s"a $mtdError error is returned from the service" in new Test {

          MockRetrieveTransactionsRequestParser
            .parse(rawRequest)
            .returns(Right(parsedRequest))

          MockRetrieveTransactionsService
            .retrieveTransactions(parsedRequest)
            .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

          val result: Future[Result] = controller.retrieveTransactions(nino, Some(from), Some(to))(fakeGetRequest)

          status(result) shouldBe expectedStatus
          contentAsJson(result) shouldBe Json.toJson(mtdError)
          header("X-CorrelationId", result) shouldBe Some(correlationId)

          val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
          MockedAuditService.verifyAuditEvent(event(auditResponse)).once
        }
      }

      val input = Seq(
        (NinoFormatError, BAD_REQUEST),
        (FromDateFormatError, BAD_REQUEST),
        (ToDateFormatError, BAD_REQUEST),
        (NotFoundError, NOT_FOUND),
        (NoTransactionsFoundError, NOT_FOUND),
        (DownstreamError, INTERNAL_SERVER_ERROR)
      )

      input.foreach(args => (serviceErrors _).tupled(args))
    }
  }

}
