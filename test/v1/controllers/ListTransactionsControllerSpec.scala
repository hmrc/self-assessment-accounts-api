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

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.mocks.hateoas.MockHateoasFactory
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.Nino
import api.models.errors._
import api.models.hateoas.Method.GET
import api.models.hateoas.RelType._
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v1.fixtures.ListTransactionsFixture._
import v1.mocks.requestParsers.MockListTransactionsRequestParser
import v1.mocks.services.MockListTransactionsService
import v1.models.request.listTransactions._
import v1.models.response.listTransaction.{ListTransactionsHateoasData, ListTransactionsResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListTransactionsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockListTransactionsRequestParser
    with MockListTransactionsService
    with MockHateoasFactory {

  private val transactionId = "X1234567890A"
  private val paymentId     = "081203010024-000001"
  private val from          = "2018-05-05"
  private val to            = "2019-12-05"

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

  "listTransactions" should {
    "return a valid transactions response" when {
      "the request is valid" in new RunControllerTest {

        protected def setupMocks(): Unit = {
          MockListTransactionsRequestParser
            .parse(rawRequest)
            .returns(Right(parsedRequest))

          MockListTransactionsService
            .listTransactions(parsedRequest)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, listTransactionsResponse))))

          MockHateoasFactory
            .wrapList(listTransactionsResponse, ListTransactionsHateoasData(nino, from, to))
            .returns(HateoasWrapper(hateoasResponse, Seq(listTransactionsHateoasLink, listChargesHateoasLink, listPaymentsHateoasLink)))
        }

        runOkTestWithAudit(expectedStatus = OK, maybeExpectedResponseBody = Some(listTransactionsMtdResponseWithHateoas))
      }
    }

    "return the correct errors" when {
      "the parser validation fails" in new RunControllerTest {

        protected def setupMocks(): Unit = {
          MockListTransactionsRequestParser
            .parse(rawRequest)
            .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))
        }

        runErrorTestWithAudit(NinoFormatError)
      }
    }

    "the service returns an error" in new RunControllerTest {

      protected def setupMocks(): Unit = {
        MockListTransactionsRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockListTransactionsService
          .listTransactions(parsedRequest)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleDateRangeInvalidError))))
      }

      runErrorTestWithAudit(RuleDateRangeInvalidError)
    }
  }

  private trait RunControllerTest extends RunTest with AuditEventChecking {

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

    protected def callController(): Future[Result] = controller.listTransactions(nino, Some(from), Some(to))(fakeGetRequest)

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "listSelfAssessmentTransactions",
        transactionName = "list-self-assessment-transactions",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino),
          requestBody = maybeRequestBody,
          auditResponse = auditResponse,
          `X-CorrelationId` = correlationId
        )
      )

  }

}
