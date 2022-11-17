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
import api.models.hateoas.RelType.{RETRIEVE_CHARGE_HISTORY, RETRIEVE_TRANSACTION_DETAILS, SELF}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v1.fixtures.retrieveAllocations.RetrieveAllocationsResponseFixture
import v1.mocks.requestParsers.MockRetrieveAllocationsRequestParser
import v1.mocks.services.MockRetrieveAllocationsService
import v1.models.request.retrieveAllocations.{RetrieveAllocationsParsedRequest, RetrieveAllocationsRawRequest}
import v1.models.response.retrieveAllocations.RetrieveAllocationsHateoasData
import v1.models.response.retrieveAllocations.detail.AllocationDetail

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveAllocationsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveAllocationsService
    with MockHateoasFactory
    with MockRetrieveAllocationsRequestParser {

  private val paymentId      = "aLot-anItem"
  private val paymentLot     = "aLot"
  private val paymentLotItem = "anItem"

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
  private val mtdResponseJson             = RetrieveAllocationsResponseFixture.mtdJsonWithHateoas

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

  "retrieveAllocations" should {
    "return OK" when {
      "the request is valid" in new RunControllerTest {

        protected def setupMocks(): Unit = {
          MockRetrieveAllocationsRequestParser
            .parse(rawRequest)
            .returns(Right(parsedRequest))

          MockRetrieveAllocationsService
            .retrieveAllocations(parsedRequest)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveAllocationsResponse))))

          MockHateoasFactory
            .wrapList(retrieveAllocationsResponse, RetrieveAllocationsHateoasData(nino, paymentId))
            .returns(HateoasWrapper(hateoasResponse, Seq(paymentAllocationsLink)))
        }

        runOkTestWithAudit(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponseJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new RunControllerTest {

        protected def setupMocks(): Unit = {
          MockRetrieveAllocationsRequestParser
            .parse(rawRequest)
            .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))
        }

        runErrorTestWithAudit(NinoFormatError)
      }

      "the service returns an error" in new RunControllerTest {

        protected def setupMocks(): Unit = {
          MockRetrieveAllocationsRequestParser
            .parse(rawRequest)
            .returns(Right(parsedRequest))

          MockRetrieveAllocationsService
            .retrieveAllocations(parsedRequest)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, PaymentIdFormatError))))
        }

        runErrorTestWithAudit(PaymentIdFormatError)
      }
    }
  }

  private trait RunControllerTest extends RunTest with AuditEventChecking {

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

    protected def callController(): Future[Result] = controller.retrieveAllocations(nino, paymentId)(fakeGetRequest)

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "retrieveASelfAssessmentPaymentsAllocationDetails",
        transactionName = "retrieve-a-self-assessment-payments-allocation-details",
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
