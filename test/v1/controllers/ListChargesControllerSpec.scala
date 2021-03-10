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

import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.fixtures.ListChargesFixture._
import v1.hateoas.HateoasLinks
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockListChargesRequestParser
import v1.models.errors._
import v1.models.hateoas.Method.GET
import v1.models.hateoas.RelType.{LIST_TRANSACTIONS, RETRIEVE_CHARGE_HISTORY, RETRIEVE_TRANSACTION_DETAILS, SELF}
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.listCharges.{ListChargesParsedRequest, ListChargesRawRequest}
import v1.models.response.listCharges.{ListChargesHateoasData, ListChargesResponse}
import v1.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockListChargesService, MockMtdIdLookupService}
import v1.models.audit.{AuditDetail, AuditError, AuditEvent, AuditResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListChargesControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockListChargesRequestParser
  with MockListChargesService
  with MockHateoasFactory
  with MockAuditService
  with HateoasLinks
  with MockIdGenerator {

  private val nino = "AA123456A"
  private val from = "2018-10-1"
  private val to = "2019-10-1"
  private val correlationId = "X-123"
  private val rawRequest = ListChargesRawRequest(nino, Some(from), Some(to))
  private val parsedRequest = ListChargesParsedRequest(Nino(nino), from, to)

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new ListChargesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockListChargesRequestParser,
      service = mockService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  def event(auditResponse: AuditResponse): AuditEvent =
    AuditEvent(
      auditType = "listSelfAssessmentCharges",
      transactionName = "list-self-assessment-charges",
      detail = AuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        nino = nino,
        response = auditResponse,
        `X-CorrelationId` = correlationId
      )
    )

  private val transactionDetailHateoasLink1 =
    Link(
      href = "/accounts/self-assessment/AA123456A/transactions/1234567890AB",
      method = GET,
      rel = RETRIEVE_TRANSACTION_DETAILS
    )

  private val transactionDetailHateoasLink2 =
    Link(
      href = "/accounts/self-assessment/AA123456A/transactions/1234567890AB",
      method = GET, rel = RETRIEVE_TRANSACTION_DETAILS
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
      HateoasWrapper(fullChargeModel,
        Seq(
          transactionDetailHateoasLink1
        )
      ),
      HateoasWrapper(fullChargeModel,
        Seq(
          transactionDetailHateoasLink2
        )
      )
    )
  )

  "retrieveList" should {
    "return a valid charges response" when {
      "a request sent has valid details" in new Test {

        MockListChargesRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockListChargesService
          .listCharges(parsedRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, mtdResponseObj))))

        MockHateoasFactory
          .wrapList(mtdResponseObj, ListChargesHateoasData(nino, from, to))
          .returns(HateoasWrapper(hateoasResponse, Seq(listChargesHateoasLink, listTransactionsHateoasLink)))

        val result: Future[Result] = controller.listCharges(nino, Some(from), Some(to))(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe ListChargesMtdResponseWithHateoas
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(OK, None, None)
        MockedAuditService.verifyAuditEvent(event(auditResponse)).once
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockListChargesRequestParser
              .parse(rawRequest)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.listCharges(nino, Some(from), Some(to))(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(error.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (FromDateFormatError, BAD_REQUEST),
          (ToDateFormatError, BAD_REQUEST),
          (MissingFromDateError, BAD_REQUEST),
          (MissingToDateError, BAD_REQUEST),
          (RuleDateRangeInvalidError, BAD_REQUEST),
          (RuleFromDateNotSupportedError, BAD_REQUEST),
          (RangeToDateBeforeFromDateError, BAD_REQUEST),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockListChargesRequestParser
              .parse(rawRequest)
              .returns(Right(parsedRequest))

            MockListChargesService
              .listCharges(parsedRequest)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.listCharges(nino, Some(from), Some(to))(fakeGetRequest)

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
          (RuleDateRangeInvalidError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}