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

package v3.controllers

import config.MockAppConfig
import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{ErrorWrapper, NinoFormatError}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.JsValue
import play.api.mvc.Result
import play.api.Configuration
import v3.controllers.validators.MockRetrieveCodingOutStatusValidatorFactory
import v3.models.request.retrieveCodingOutStatus.RetrieveCodingOutStatusRequestData
import v3.services.MockRetrieveCodingOutStatusService
import v3.fixtures.retrieveCodingOutStatus.ResponseFixture.mtdResponseJson
import v3.models.errors.RuleBusinessPartnerNotExistError
import v3.models.response.retrieveCodingOutStatus.RetrieveCodingOutStatusResponse
import routing.{Version, Version3}

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

  private val requestData = RetrieveCodingOutStatusRequestData(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  private val downstreamResponse: RetrieveCodingOutStatusResponse =
    RetrieveCodingOutStatusResponse(processingDate = processingDate, nino = nino, taxYear = TaxYear.fromMtd(taxYear), optOutIndicator = true)

  "RetrieveCodingOutStatusController" should {
    "return OK" when {
      "happy path" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedRetrieveCodingOutStatusService
          .retrieveCodingOutStatus(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamResponse))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponseJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedRetrieveCodingOutStatusService
          .retrieveCodingOutStatus(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleBusinessPartnerNotExistError))))

        runErrorTest(RuleBusinessPartnerNotExistError)
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
          userType = "Agent",
          agentReferenceNumber = Some("123456"),
          versionNumber = "3.0",
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
