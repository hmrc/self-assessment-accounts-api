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

import api.controllers.{ControllerBaseSpec, ControllerTestRunner, RequestHandlerFactory}
import api.mocks.hateoas.MockHateoasFactory
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.Nino
import api.models.errors._
import api.models.hateoas.Method.GET
import api.models.hateoas.RelType.{LIST_TRANSACTIONS, RETRIEVE_TRANSACTION_DETAILS, SELF}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v1.fixtures.ListChargesFixture._
import v1.mocks.requestParsers.MockListChargesRequestParser
import v1.mocks.services.MockListChargesService
import v1.models.request.listCharges._
import v1.models.response.listCharges.{ListChargesHateoasData, ListChargesResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListChargesControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockListChargesRequestParser
    with MockListChargesService
    with MockHateoasFactory {

  private val from = "2018-10-1"
  private val to   = "2019-10-1"

  private val rawRequest    = ListChargesRawRequest(nino, Some(from), Some(to))
  private val parsedRequest = ListChargesParsedRequest(Nino(nino), from, to)

  private val transactionDetailHateoasLink1 =
    Link(
      href = "/accounts/self-assessment/AA123456A/transactions/1234567890AB",
      method = GET,
      rel = RETRIEVE_TRANSACTION_DETAILS
    )

  private val transactionDetailHateoasLink2 =
    Link(
      href = "/accounts/self-assessment/AA123456A/transactions/1234567890AB",
      method = GET,
      rel = RETRIEVE_TRANSACTION_DETAILS
    )

  private val listChargesHateoasLink =
    Link(
      href = "/accounts/self-assessment/AA123456A/charges?from=2018-10-01&to=2019-10-01",
      method = GET,
      rel = SELF
    )

  private val listTransactionsHateoasLink =
    Link(
      href = "/accounts/self-assessment/AA123456A/transactions?from=2018-10-01&to=2019-10-01",
      method = GET,
      rel = LIST_TRANSACTIONS
    )

  private val hateoasResponse = ListChargesResponse(
    Seq(
      HateoasWrapper(
        fullChargeModel,
        Seq(
          transactionDetailHateoasLink1
        )),
      HateoasWrapper(
        fullChargeModel,
        Seq(
          transactionDetailHateoasLink2
        ))
    )
  )

  "retrieveList" should {
    "return a valid charges response" when {
      "the request is valid" in new Test {
        MockListChargesRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockListChargesService
          .listCharges(parsedRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, mtdResponseObj))))

        MockHateoasFactory
          .wrapList(mtdResponseObj, ListChargesHateoasData(nino, from, to))
          .returns(HateoasWrapper(hateoasResponse, Seq(listChargesHateoasLink, listTransactionsHateoasLink)))

        runOkTestWithAudit(expectedStatus = OK, maybeExpectedResponseBody = Some(ListChargesMtdResponseWithHateoas))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockListChargesRequestParser
          .parse(rawRequest)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTestWithAudit(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockListChargesRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockListChargesService
          .listCharges(parsedRequest)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleDateRangeInvalidError))))

        runErrorTestWithAudit(RuleDateRangeInvalidError)
      }
    }
  }

  private trait Test extends ControllerTest with AuditEventChecking {

    val controller = new ListChargesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockListChargesRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator,
      new RequestHandlerFactory
    )

    protected def callController(): Future[Result] = controller.listCharges(nino, Some(from), Some(to))(fakeGetRequest)

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "listSelfAssessmentCharges",
        transactionName = "list-self-assessment-charges",
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
