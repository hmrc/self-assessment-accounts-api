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

import play.api.libs.json.Json
import play.api.mvc.Result
import v1.models.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.MockIdGenerator
import v1.mocks.requestParsers.MockDeleteCodingOutParser
import v1.mocks.services.{MockDeleteCodingOutService, MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.audit.{AuditError, AuditEvent, AuditResponse, GenericAuditDetail}
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.deleteCodingOut._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteCodingOutControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockDeleteCodingOutService
    with MockDeleteCodingOutParser
    with MockIdGenerator
    with MockAuditService {

  private val nino = "AA123456A"
  private val taxYear = "2019-20"
  private val correlationId = "X-123"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new DeleteCodingOutController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRequestDataParser,
      service = mockDeleteCodingOutService,
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
      auditType = "DeleteCodingOutUnderpayments",
      transactionName = "delete-coding-out-underpayments",
      detail = GenericAuditDetail(
        userType = "Individual",
        agentReferenceNumber = None,
        params = Map("nino" -> nino, "taxYear" -> taxYear),
        requestBody = None,
        `X-CorrelationId` = correlationId,
        auditResponse = auditResponse
      )
    )

  private val rawData = DeleteCodingOutRawRequest(nino, taxYear)
  private val requestData = DeleteCodingOutParsedRequest(Nino(nino), taxYear)

  "handleRequest" should {
    "return NoContent" when {
      "the request received is valid" in new Test {

        MockDeleteCodingOutParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteCodingOutService
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)

        status(result) shouldBe NO_CONTENT
        header("X-CorrelationId", result) shouldBe Some(correlationId)

        val auditResponse: AuditResponse = AuditResponse(NO_CONTENT, None, None)
        MockedAuditService.verifyAuditEvent(event(auditResponse)).once

      }
    }
    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockDeleteCodingOutParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)

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
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockDeleteCodingOutParser
              .parse(rawData)
              .returns(Right(requestData))

            MockDeleteCodingOutService
              .delete(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)

            val auditResponse: AuditResponse = AuditResponse(expectedStatus, Some(Seq(AuditError(mtdError.code))), None)
            MockedAuditService.verifyAuditEvent(event(auditResponse)).once

          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (CodingOutNotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}