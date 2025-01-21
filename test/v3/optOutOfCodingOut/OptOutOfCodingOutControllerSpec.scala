/*
 * Copyright 2024 HM Revenue & Customs
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

package v3.optOutOfCodingOut

import common.errors.RuleBusinessPartnerNotExistError
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.TaxYear
import shared.models.errors.{ErrorWrapper, NinoFormatError}
import shared.models.outcomes.ResponseWrapper
import shared.routing.{Version, Version3}
import v3.optOutOfCodingOut.def1.model.request.Def1_OptOutOfCodingOutRequestData
import v3.optOutOfCodingOut.def1.model.response.Def1_OptOutOfCodingOutResponse
import v3.optOutOfCodingOut.model.request.OptOutOfCodingOutRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class OptOutOfCodingOutControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockOptOutOfCodingOutService
    with MockOptOutOfCodingOutValidatorFactory {

  override val apiVersion: Version = Version3

  "OptOutOfCodingOutController" should {
    "return 204 NO_CONTENT" when {
      "happy path" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedOptOutOfCodingOutService.optOutOfCodingOut(requestData) returns
          Future.successful(Right(ResponseWrapper(correlationId, response)))

        runOkTestWithAudit(expectedStatus = NO_CONTENT)
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedOptOutOfCodingOutService.optOutOfCodingOut(requestData) returns
          Future.successful(Left(ErrorWrapper(correlationId, RuleBusinessPartnerNotExistError)))

        runErrorTestWithAudit(RuleBusinessPartnerNotExistError)
      }
    }
  }

  private trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {
    private val taxYear = "2023-24"

    protected val requestData: OptOutOfCodingOutRequestData = Def1_OptOutOfCodingOutRequestData(
      nino = parsedNino,
      taxYear = TaxYear.fromMtd(taxYear)

    )

    protected val response: Def1_OptOutOfCodingOutResponse = Def1_OptOutOfCodingOutResponse(processingDate = "2020-12-17T09:30:47Z")

    override protected val controller = new OptOutOfCodingOutController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockOptOutOfCodingOutValidatorFactory,
      service = mockOptOutOfCodingOutService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator)

    MockedSharedAppConfig.featureSwitchConfig returns Configuration.empty
    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.optOutOfCodingOut(validNino, taxYear)(fakeGetRequest)

    override protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "OptOutOfCodingOut",
        transactionName = "opt-out-of-coding-out",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          versionNumber = "3.0",
          params = Map("nino" -> validNino, "taxYear" -> taxYear),
          requestBody = None,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
