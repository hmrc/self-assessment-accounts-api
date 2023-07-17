/*
 * Copyright 2023 HM Revenue & Customs
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
import api.models.hateoas.RelType.SELF
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v1.fixtures.RetrieveBalanceFixture
import v1.mocks.requestParsers.MockRetrieveBalanceRequestParser
import v1.mocks.services.MockRetrieveBalanceService
import v1.models.request.retrieveBalance.{RetrieveBalanceParsedRequest, RetrieveBalanceRawRequest}
import v1.models.response.retrieveBalance.RetrieveBalanceHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveBalanceControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveBalanceService
    with MockHateoasFactory
    with MockRetrieveBalanceRequestParser {

  val balanceLink: Link =
    Link(
      href = s"/accounts/self-assessment/$nino/balance",
      method = GET,
      rel = SELF
    )
  private val rawRequest: RetrieveBalanceRawRequest =
    RetrieveBalanceRawRequest(nino = nino)
  private val parsedRequest: RetrieveBalanceParsedRequest =
    RetrieveBalanceParsedRequest(
      nino = Nino(nino)
    )
  private val retrieveBalanceResponse = RetrieveBalanceFixture.fullModel
  private val mtdResponseJson         = RetrieveBalanceFixture.fullMtdResponseJsonWithHateoas(nino)

  "retrieveBalance" should {
    "return OK" when {
      "the request is valid" in new Test {
        MockRetrieveBalanceRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockRetrieveBalanceService
          .retrieveBalance(parsedRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveBalanceResponse))))

        MockHateoasFactory
          .wrap(retrieveBalanceResponse, RetrieveBalanceHateoasData(nino))
          .returns(HateoasWrapper(retrieveBalanceResponse, Seq(balanceLink)))

        runOkTestWithAudit(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponseJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockRetrieveBalanceRequestParser
          .parse(rawRequest)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTestWithAudit(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveBalanceRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockRetrieveBalanceService
          .retrieveBalance(parsedRequest)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, NinoFormatError))))

        runErrorTestWithAudit(NinoFormatError)
      }
    }
  }

  private trait Test extends ControllerTest with AuditEventChecking {

    val controller = new RetrieveBalanceController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRetrieveBalanceRequestParser,
      service = mockRetrieveBalanceService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.retrieveBalance(nino)(fakeGetRequest)

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "retrieveASelfAssessmentBalance",
        transactionName = "retrieve-a-self-assessment-balance",
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
