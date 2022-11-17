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
import api.hateoas.HateoasLinks
import api.mocks.hateoas.MockHateoasFactory
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.Nino
import api.models.errors._
import api.models.hateoas.Method.GET
import api.models.hateoas.RelType.{LIST_TRANSACTIONS, RETRIEVE_PAYMENT_ALLOCATIONS, SELF}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v1.fixtures.ListPaymentsFixture._
import v1.mocks.requestParsers.MockListPaymentsRequestParser
import v1.mocks.services.MockListPaymentsService
import v1.models.request.listPayments._
import v1.models.response.listPayments.{ListPaymentsHateoasData, ListPaymentsResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListPaymentsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockListPaymentsRequestParser
    with MockListPaymentsService
    with MockHateoasFactory
    with HateoasLinks {

  private val from = "2018-10-01"
  private val to   = "2019-10-01"

  private val rawRequest    = ListPaymentsRawRequest(nino, Some(from), Some(to))
  private val parsedRequest = ListPaymentsParsedRequest(Nino(nino), from, to)

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
      "a request sent has valid details" in new RunControllerTest {

        protected def setupMocks(): Unit = {
          MockListPaymentsRequestParser
            .parse(rawRequest)
            .returns(Right(parsedRequest))

          MockListPaymentsService
            .listPayments(parsedRequest)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, mtdResponseObj))))

          MockHateoasFactory
            .wrapList(mtdResponseObj, ListPaymentsHateoasData(nino, from, to))
            .returns(HateoasWrapper(hateoasResponse, Seq(listPaymentsHateoasLink, transactionsHateoasLink)))
        }

        runOkTestWithAudit(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponseJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new RunControllerTest {

        protected def setupMocks(): Unit = {
          MockListPaymentsRequestParser
            .parse(rawRequest)
            .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))
        }

        runErrorTestWithAudit(NinoFormatError)
      }

      "the service returns an error" in new RunControllerTest {

        protected def setupMocks(): Unit = {
          MockListPaymentsRequestParser
            .parse(rawRequest)
            .returns(Right(parsedRequest))

          MockListPaymentsService
            .listPayments(parsedRequest)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, V1_FromDateFormatError))))
        }

        runErrorTestWithAudit(V1_FromDateFormatError)
      }
    }
  }

  private trait RunControllerTest extends RunTest with AuditEventChecking {

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

    protected def callController(): Future[Result] = controller.listPayments(nino, Some(from), Some(to))(fakeGetRequest)

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "listSelfAssessmentPayments",
        transactionName = "list-self-assessment-payments",
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
