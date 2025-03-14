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

package v4.retrieveCodingOutStatus

import common.errors.RuleBusinessPartnerNotExistError
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.domain.TaxYear
import shared.models.errors.{ErrorWrapper, NinoFormatError}
import shared.models.outcomes.ResponseWrapper
import shared.routing.{Version, Version4}
import v4.retrieveCodingOutStatus.def1.model.request.Def1_RetrieveCodingOutStatusRequestData
import v4.retrieveCodingOutStatus.def1.model.response.Def1_RetrieveCodingOutStatusResponse
import v4.retrieveCodingOutStatus.model.response.RetrieveCodingOutStatusResponse
import v4.retrieveCodingOutStatus.model.responses.ResponseFixture.mtdResponseJson

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveCodingOutStatusControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveCodingOutStatusService
    with MockRetrieveCodingOutStatusValidatorFactory {

  override val apiVersion: Version = Version4

  override val validNino             = "AB123456A"
  private val taxYear                = "2023-24"
  private val processingDate: String = "2023-12-17T09:30:47Z"

  private val requestData = Def1_RetrieveCodingOutStatusRequestData(
    nino = parsedNino,
    taxYear = TaxYear.fromMtd(taxYear)
  )

  private val downstreamResponse: RetrieveCodingOutStatusResponse =
    Def1_RetrieveCodingOutStatusResponse(
      processingDate = processingDate,
      nino = validNino,
      taxYear = TaxYear.fromMtd(taxYear),
      optOutIndicator = true
    )

  "RetrieveCodingOutStatusController" should {
    "return OK" when {
      "happy path" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedRetrieveCodingOutStatusService
          .retrieveCodingOutStatus(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamResponse))))

        runOkTestWithAudit(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponseJson), maybeAuditResponseBody = Some(mtdResponseJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTestWithAudit(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedRetrieveCodingOutStatusService
          .retrieveCodingOutStatus(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleBusinessPartnerNotExistError))))

        runErrorTestWithAudit(RuleBusinessPartnerNotExistError)
      }
    }
  }

  private trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    override protected val controller = new RetrieveCodingOutStatusController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveCodingOutStatusValidatorFactory,
      service = mockRetrieveCodingOutStatusService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator)

    MockedSharedAppConfig.featureSwitchConfig returns Configuration.empty
    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.retrieveCodingOutStatus(validNino, taxYear)(fakeGetRequest)

    override protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "RetrieveCodingOutStatus",
        transactionName = "retrieve-coding-out-status",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          versionNumber = "4.0",
          params = Map("nino" -> validNino, "taxYear" -> taxYear),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
