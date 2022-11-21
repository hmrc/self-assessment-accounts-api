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
import api.models.hateoas.RelType.{RETRIEVE_TRANSACTION_DETAILS, SELF}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v1.fixtures.RetrieveChargeHistoryFixture
import v1.mocks.requestParsers.MockRetrieveChargeHistoryRequestParser
import v1.mocks.services.MockRetrieveChargeHistoryService
import v1.models.request.retrieveChargeHistory.{RetrieveChargeHistoryParsedRequest, RetrieveChargeHistoryRawRequest}
import v1.models.response.retrieveChargeHistory.RetrieveChargeHistoryHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveChargeHistoryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveChargeHistoryService
    with MockHateoasFactory
    with MockRetrieveChargeHistoryRequestParser {

  private val transactionId = "anId"

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
  private val mtdResponseJson               = RetrieveChargeHistoryFixture.mtdResponseMultipleWithHateoas(nino, transactionId)

  "retrieveChargeHistory" should {
    "return OK" when {
      "the request is valid" in new Test {
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

        runOkTestWithAudit(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponseJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockRetrieveChargeHistoryRequestParser
          .parse(rawRequest)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTestWithAudit(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveChargeHistoryRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockRetrieveChargeHistoryService
          .retrieveChargeHistory(parsedRequest)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, TransactionIdFormatError))))

        runErrorTestWithAudit(TransactionIdFormatError)
      }
    }
  }

  private trait Test extends ControllerTest with AuditEventChecking {

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

    protected def callController(): Future[Result] = controller.retrieveChargeHistory(nino, transactionId)(fakeGetRequest)

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "retrieveASelfAssessmentChargesHistory",
        transactionName = "retrieve-a-self-assessment-charges-history",
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
