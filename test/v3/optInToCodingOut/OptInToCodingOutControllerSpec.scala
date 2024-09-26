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

package v3.optInToCodingOut

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
import v3.optInToCodingOut.def1.model.request.Def1_OptInToCodingOutRequestData
import v3.optInToCodingOut.model.request.OptInToCodingOutRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class OptInToCodingOutControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockOptInToCodingOutService
    with MockOptInToCodingOutValidatorFactory
    with MockAppConfig {

  override val apiVersion: Version = Version3

  "OptInToCodingOutController" should {
    "return 204 NO_CONTENT" when {
      "happy path" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedOptInToCodingOutService.optInToCodingOut(requestData) returns
          Future.successful(Right(ResponseWrapper(correlationId, ())))

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

        MockedOptInToCodingOutService.optInToCodingOut(requestData) returns
          Future.successful(Left(ErrorWrapper(correlationId, RuleBusinessPartnerNotExistError)))

        runErrorTestWithAudit(RuleBusinessPartnerNotExistError)
      }
    }
  }

  private trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    val controller = new OptInToCodingOutController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockOptInToCodingOutValidatorFactory,
      service = mockOptInToCodingOutService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator)

    private val taxYear = "2023-24"

    protected val requestData: OptInToCodingOutRequestData = Def1_OptInToCodingOutRequestData(
      nino = Nino(nino),
      taxYear = TaxYear.fromMtd(taxYear)
    )

    MockAppConfig.featureSwitches returns Configuration.empty
    MockAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.optInToCodingOut(nino, taxYear)(fakeGetRequest)

    override protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "OptInToCodingOut",
        transactionName = "opt-in-to-coding-out",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          versionNumber = "3.0",
          params = Map("nino" -> nino, "taxYear" -> taxYear),
          requestBody = None,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}