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
import v1.fixtures.ListTransactionsFixture._
import v1.mocks.requestParsers.MockListTransactionsRequestParser
import v1.mocks.services.MockListTransactionsService
import api.models.errors._
import api.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.hateoas.Method.GET
import api.models.hateoas.RelType.{LIST_CHARGES, LIST_PAYMENTS, RETRIEVE_PAYMENT_ALLOCATIONS, RETRIEVE_TRANSACTION_DETAILS, SELF}
import api.models.outcomes.ResponseWrapper
import v1.models.request.listTransactions._
import v1.models.response.listTransaction.ListTransactionsResponse
import v1.models.response.listTransaction.ListTransactionsHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListTransactionsControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockListTransactionsRequestParser
    with MockListTransactionsService
    with MockHateoasFactory
    with HateoasLinks
    with MockAuditService
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val transactionId = "X1234567890A"
  private val paymentId     = "081203010024-000001"
  private val from          = "2018-05-05"
  private val to            = "2019-12-05"
  private val correlationId = "X-123"

  private val rawRequest    = ListTransactionsRawRequest(nino, Some(from), Some(to))
  private val parsedRequest = ListTransactionsParsedRequest(Nino(nino), from, to)

  private val listTransactionsResponse = fullMultipleItemsListTransactionsModel

  private val transactionDetailsHateoasLink =
    Link(
      href = s"/accounts/self-assessment/$nino/transactions/$transactionId",
      method = GET,
      rel = RETRIEVE_TRANSACTION_DETAILS
    )

  private val paymentAllocationHateoasLink: Link =
    Link(
      href = s"/accounts/self-assessment/$nino/payments/$paymentId",
      method = GET,
      rel = RETRIEVE_PAYMENT_ALLOCATIONS
    )

  private val listTransactionsHateoasLink: Link =
    Link(
      href = s"/accounts/self-assessment/$nino/transactions?from=$from&to=$to",
      method = GET,
      rel = SELF
    )

  private val listPaymentsHateoasLink: Link =
    Link(
      href = s"/accounts/self-assessment/$nino/payments?from=$from&to=$to",
      method = GET,
      rel = LIST_PAYMENTS
    )

  private val listChargesHateoasLink: Link =
    Link(
      href = s"/accounts/self-assessment/$nino/charges?from=$from&to=$to",
      method = GET,
      rel = LIST_CHARGES
    )

  private val hateoasResponse = ListTransactionsResponse(
    Seq(
      HateoasWrapper(
        payload = chargesTransactionItemModel,
        links = Seq(transactionDetailsHateoasLink)
      ),
      HateoasWrapper(
        payload = paymentsTransactionItemModel,
        links = Seq(paymentAllocationHateoasLink, transactionDetailsHateoasLink)
      )
    )
  )

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new ListTransactionsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockListTransactionsRequestParser,
      service = mockListTransactionsService,
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
      auditType = "listSelfAssessmentTransactions",
      transactionName = "list-self-assessment-transactions",
      detail = GenericAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        params = Map("nino" -> nino),
        requestBody = None,
        auditResponse = auditResponse,
        `X-CorrelationId` = correlationId
      )
    )

  "listTransactions" should {
    "return a valid transactions response" when {
      "a request sent has valid details" in new Test {

        MockListTransactionsRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockListTransactionsService
          .listTransactions(parsedRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, listTransactionsResponse))))

        MockHateoasFactory
          .wrapList(listTransactionsResponse, ListTransactionsHateoasData(nino, from, to))
          .returns(HateoasWrapper(hateoasResponse, Seq(listTransactionsHateoasLink, listChargesHateoasLink, listPaymentsHateoasLink)))

        val result: Future[Result] = controller.listTransactions(nino, Some(from), Some(to))(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe listTransactionsMtdResponseWithHateoas
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, None)
        MockedAuditService.verifyAuditEvent(event(auditResponse)).once
      }
    }

    "return the correct errors" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockListTransactionsRequestParser
              .parse(rawRequest)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.listTransactions(nino, Some(from), Some(to))(fakeGetRequest)

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
    }

    "service errors occur" must {
      def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
        s"a $mtdError error is returned from the service" in new Test {

          MockListTransactionsRequestParser
            .parse(rawRequest)
            .returns(Right(parsedRequest))

          MockListTransactionsService
            .listTransactions(parsedRequest)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

          val result: Future[Result] = controller.listTransactions(nino, Some(from), Some(to))(fakeGetRequest)

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
        (RuleDateRangeInvalidError, BAD_REQUEST),
        (NotFoundError, NOT_FOUND),
        (DownstreamError, INTERNAL_SERVER_ERROR)
      )

      input.foreach(args => (serviceErrors _).tupled(args))
    }
  }

}
