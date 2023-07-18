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
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.JsValue
import play.api.mvc.Result
import v1.mocks.requestParsers.MockDeleteCodingOutParser
import v1.mocks.services.MockDeleteCodingOutService
import v1.models.request.deleteCodingOut._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteCodingOutControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockDeleteCodingOutService
    with MockDeleteCodingOutParser {

  private val taxYear = "2019-20"

  private val rawData     = DeleteCodingOutRawRequest(nino, taxYear)
  private val requestData = DeleteCodingOutParsedRequest(Nino(nino), TaxYear.fromMtd(taxYear))

  "handleRequest" should {
    "return NoContent" when {
      "the request is valid" in new Test {
        MockDeleteCodingOutParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteCodingOutService
          .delete(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(expectedStatus = NO_CONTENT, maybeExpectedResponseBody = None)
      }
    }
    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockDeleteCodingOutParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTestWithAudit(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockDeleteCodingOutParser
          .parse(rawData)
          .returns(Right(requestData))

        MockDeleteCodingOutService
          .delete(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, CodingOutNotFoundError))))

        runErrorTestWithAudit(CodingOutNotFoundError)
      }
    }
  }

  private trait Test extends ControllerTest with AuditEventChecking {

    val controller = new DeleteCodingOutController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRequestDataParser,
      service = mockDeleteCodingOutService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.handleRequest(nino, taxYear)(fakeRequest)

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeleteCodingOutUnderpayments",
        transactionName = "delete-coding-out-underpayments",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
