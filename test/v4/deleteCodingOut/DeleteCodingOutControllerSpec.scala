/*
 * Copyright 2025 HM Revenue & Customs
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

package v4.deleteCodingOut

import config.MockSaAccountsConfig
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.TaxYear
import shared.models.errors.{ErrorWrapper, NinoFormatError, RuleTaxYearNotEndedError}
import shared.models.outcomes.ResponseWrapper
import shared.routing.{Version, Version4}
import v4.deleteCodingOut.def1.model.request.Def1_DeleteCodingOutRequestData
import v4.deleteCodingOut.def1.models.MockDeleteCodingOutValidatorFactory
import v4.deleteCodingOut.model.request.DeleteCodingOutRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteCodingOutControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockDeleteCodingOutService
    with MockDeleteCodingOutValidatorFactory
    with MockSaAccountsConfig {

  override val apiVersion: Version =  Version4

  private val taxYear = "2019-20"

  private val requestData: DeleteCodingOutRequestData = Def1_DeleteCodingOutRequestData(parsedNino, TaxYear.fromMtd(taxYear))

  "handleRequest" should {
    "return NO_CONTENT" when {

      "the request is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockDeleteCodingOutService
          .deleteCodingOut(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        runOkTestWithAudit(
          expectedStatus = NO_CONTENT
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))
        runErrorTestWithAudit(NinoFormatError)
      }
    }

    "the service returns an error" in new Test {
      willUseValidator(returningSuccess(requestData))

      MockDeleteCodingOutService.deleteCodingOut(requestData)
        .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotEndedError))))

      runErrorTestWithAudit(RuleTaxYearNotEndedError)
    }
  }

  private trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    override protected val controller =
      new DeleteCodingOutController(
        mockEnrolmentsAuthService,
        mockMtdIdLookupService,
        mockDeleteCodingOutValidatorFactory,
        mockDeleteCodingOutService,
        mockAuditService,
        cc,
        mockIdGenerator
      )

    MockedSharedAppConfig.featureSwitchConfig.returns(Configuration("allowTemporalValidationSuspension.enabled" -> true)).anyNumberOfTimes()
    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false


    protected def callController(): Future[Result] = controller.handleRequest(validNino, taxYear)(fakeRequest)

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "DeleteCodingOutUnderpayments",
        transactionName = "delete-coding-out-underpayments",
        detail = GenericAuditDetail(
          versionNumber = apiVersion.name,
          userType = "Individual",
          agentReferenceNumber = None,
          params = Map("nino" -> validNino, "taxYear" -> taxYear),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )
  }

}
