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

package v3.retrieveCodingOutStatus

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{ErrorWrapper, NinoFormatError, RuleBusinessPartnerNotExistError}
import api.models.outcomes.ResponseWrapper
import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import routing.{Version, Version3}
import v3.retrieveCodingOutStatus.def1.model.request.Def1_RetrieveCodingOutStatusRequestData
import v3.retrieveCodingOutStatus.def1.model.response.Def1_RetrieveCodingOutStatusResponse
import v3.retrieveCodingOutStatus.model.response.RetrieveCodingOutStatusResponse
import v3.retrieveCodingOutStatus.model.responses.ResponseFixture.mtdResponseJson

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveCodingOutStatusControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveCodingOutStatusService
    with MockRetrieveCodingOutStatusValidatorFactory
    with MockAppConfig {

  override val apiVersion: Version = Version3

  override val nino                  = "AB123456A"
  private val taxYear                = "2023-24"
  private val processingDate: String = "2023-12-17T09:30:47Z"

  private val requestData = Def1_RetrieveCodingOutStatusRequestData(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  private val downstreamResponse: RetrieveCodingOutStatusResponse =
    Def1_RetrieveCodingOutStatusResponse(processingDate = processingDate, nino = nino, taxYear = TaxYear.fromMtd(taxYear), optOutIndicator = true)

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

    MockAppConfig.featureSwitches returns Configuration.empty
    MockAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.retrieveCodingOutStatus(nino, taxYear)(fakeGetRequest)

    override protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "RetrieveCodingOutStatus",
        transactionName = "retrieve-coding-out-status",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          versionNumber = "3.0",
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
