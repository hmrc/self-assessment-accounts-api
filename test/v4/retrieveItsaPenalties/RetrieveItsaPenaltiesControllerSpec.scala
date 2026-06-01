/*
 * Copyright 2026 HM Revenue & Customs
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

package v4.retrieveItsaPenalties

import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import shared.models.errors.{ErrorWrapper, NinoFormatError}
import shared.models.outcomes.ResponseWrapper
import shared.routing.{Version, Version4}
import v4.retrieveItsaPenalties.def1.model.response.RetrieveItsaPenaltiesFixture.*
import v4.retrieveItsaPenalties.model.request.RetrieveItsaPenaltiesRequestData
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveItsaPenaltiesControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveItsaPenaltiesService
    with MockRetrieveItsaPenaltiesValidatorFactory {

  override val apiVersion: Version = Version4

  private val requestData: RetrieveItsaPenaltiesRequestData =
    RetrieveItsaPenaltiesRequestData(nino = parsedNino)

  "retrieveItsaPenalties" should {
    "return OK" when {
      "the request is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveItsaPenaltiesService
          .retrieveItsaPenalties(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        runOkTestWithAudit(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdJsonResponse), maybeAuditResponseBody = Some(mtdJsonResponse))
      }
    }

    "return the error as per spec" when {
      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveItsaPenaltiesService
          .retrieveItsaPenalties(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, NinoFormatError))))

        runErrorTestWithAudit(NinoFormatError)
      }
    }

  }

  private trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    override protected val controller: RetrieveItsaPenaltiesController = new RetrieveItsaPenaltiesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveItsaPenaltiesValidatorFactory,
      service = mockRetrieveItsaPenaltiesService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration.empty
    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.retrieveItsaPenalties(validNino)(fakeGetRequest)

    override protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "RetrieveItsaPenalties",
        transactionName = "retrieve-itsa-penalties",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          versionNumber = apiVersion.name,
          params = Map("nino" -> validNino),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}
