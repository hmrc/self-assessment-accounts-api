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
import api.models.hateoas.RelType.{RETRIEVE_PAYMENT_ALLOCATIONS, SELF}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import mocks.MockAppConfig
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v1.mocks.requestParsers.MockRetrieveTransactionDetailsRequestParser
import v1.mocks.services.MockRetrieveTransactionDetailsService
import v1.models.request.retrieveTransactionDetails.{RetrieveTransactionDetailsParsedRequest, RetrieveTransactionDetailsRawRequest}
import v1.models.response.retrieveTransactionDetails.{
  RetrieveTransactionDetailsHateoasData,
  RetrieveTransactionDetailsResponse,
  SubItem,
  TransactionItem
}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveTransactionDetailsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveTransactionDetailsRequestParser
    with MockRetrieveTransactionDetailsService
    with MockHateoasFactory
    with MockAppConfig {

  private val transactionId = "11111"
  private val paymentId     = "081203010024-000001"

  private val rawRequest    = RetrieveTransactionDetailsRawRequest(nino, transactionId)
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
    method = GET,
    rel = RETRIEVE_PAYMENT_ALLOCATIONS
  )

  private val transactionsHateoasLink = Link(
    href = "/accounts/self-assessment/AA123456A/transactions",
    method = GET,
    rel = SELF
  )

  private val hateoasResponse = RetrieveTransactionDetailsResponse(Seq(paymentTransaction))

  private val mtdResponseJson = Json.parse(
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

  "retrieveTransactionDetails" should {
    "return a valid transactions response" when {
      "the request is valid" in new Test {
        MockRetrieveTransactionDetailsRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockRetrieveTransactionDetailsService
          .retrieveTransactionDetails(parsedRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, mtdResponse))))

        MockHateoasFactory
          .wrap(mtdResponse, RetrieveTransactionDetailsHateoasData(nino, transactionId, Some(paymentId)))
          .returns(HateoasWrapper(hateoasResponse, Seq(transactionsHateoasLink, paymentAllocationHateoasLink)))

        runOkTestWithAudit(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponseJson))
      }
    }

    "return the correct errors" when {
      "the parser validation fails" in new Test {
        MockRetrieveTransactionDetailsRequestParser
          .parse(rawRequest)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTestWithAudit(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveTransactionDetailsRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockRetrieveTransactionDetailsService
          .retrieveTransactionDetails(parsedRequest)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, NoTransactionDetailsFoundError))))

        runErrorTestWithAudit(NoTransactionDetailsFoundError)
      }
    }
  }

  private trait Test extends ControllerTest with AuditEventChecking {

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

    protected def callController(): Future[Result] = controller.retrieveTransactionDetails(nino, transactionId)(fakeGetRequest)

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "retrieveASelfAssessmentTransactionsDetail",
        transactionName = "retrieve-a-self-assessment-transactions-detail",
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
