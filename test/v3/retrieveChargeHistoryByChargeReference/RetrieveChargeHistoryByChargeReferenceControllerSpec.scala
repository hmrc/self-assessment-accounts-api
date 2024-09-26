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

package v3.retrieveChargeHistoryByChargeReference

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas
import api.hateoas.Method.GET
import api.hateoas.RelType.{RETRIEVE_TRANSACTION_DETAILS, SELF}
import api.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{ChargeReference, Nino}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc.Result
import routing.{Version, Version3}
import v3.retrieveChargeHistoryByChargeReference.def1.model.request.Def1_RetrieveChargeHistoryByChargeReferenceRequestData
import v3.retrieveChargeHistoryByChargeReference.def1.model.response.RetrieveChargeHistoryFixture._
import v3.retrieveChargeHistoryByChargeReference.model.request.RetrieveChargeHistoryByChargeReferenceRequestData
import v3.retrieveChargeHistoryByChargeReference.model.response.RetrieveChargeHistoryResponse
import v3.retrieveChargeHistoryByChargeReference.model.response.RetrieveChargeHistoryResponse.RetrieveChargeHistoryHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveChargeHistoryByChargeReferenceControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveChargeHistoryByChargeReferenceService
    with MockAppConfig
    with MockHateoasFactory
    with MockRetrieveChargeHistoryByChargeReferenceValidatorFactory {

  override val apiVersion: Version = Version3

  private val chargeReference = "anChargeReference"

  private val requestData: RetrieveChargeHistoryByChargeReferenceRequestData =
    Def1_RetrieveChargeHistoryByChargeReferenceRequestData(nino = Nino(nino), chargeReference = ChargeReference(chargeReference))

  val chargeHistoryLink: Link =
    hateoas.Link(
      href = s"/accounts/self-assessment/$nino/charges/$chargeReference",
      method = GET,
      rel = SELF
    )

  val transactionDetailsLink: Link =
    hateoas.Link(
      href = s"/accounts/self-assessment/$nino/transactions/$chargeReference",
      method = GET,
      rel = RETRIEVE_TRANSACTION_DETAILS
    )

  val response: RetrieveChargeHistoryResponse = validChargeHistoryResponseObject

  "retrieveChargeHistory" should {
    "return OK" when {
      "the request is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveChargeHistoryByChargeReferenceService
          .retrieveChargeHistoryByChargeReference(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        MockHateoasFactory
          .wrap(response, RetrieveChargeHistoryHateoasData(nino, chargeReference))
          .returns(
            HateoasWrapper(
              response,
              List(
                chargeHistoryLink,
                transactionDetailsLink
              )))

        val responseWithHateoas: JsObject = mtdMultipleResponseWithHateoas(nino, chargeReference)
        runOkTestWithAudit(expectedStatus = OK, maybeExpectedResponseBody = Some(responseWithHateoas),maybeAuditResponseBody = Some(responseWithHateoas))
      }
    }

    "return the error as per spec" when {
      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveChargeHistoryByChargeReferenceService
          .retrieveChargeHistoryByChargeReference(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, NinoFormatError))))

        runErrorTestWithAudit(NinoFormatError)
      }
    }

  }

  private trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    override protected val controller = new RetrieveChargeHistoryByChargeReferenceController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveChargeHistoryByChargeReferenceValidatorFactory,
      service = mockRetrieveChargeHistoryByChargeReferenceService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockAppConfig.featureSwitches returns Configuration.empty
    MockAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.retrieveChargeHistoryByChargeReference(nino, chargeReference)(fakeGetRequest)

    override protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "RetrieveAChargeHistoryByChargeReference",
        transactionName = "retrieve-a-charge-history-by-charge-reference",
        detail = GenericAuditDetail(
          userType = "Individual",
          agentReferenceNumber = None,
          versionNumber = apiVersion.name,
          params = Map("nino" -> nino, "chargeReference" -> chargeReference),
          requestBody = maybeRequestBody,
          `X-CorrelationId` = correlationId,
          auditResponse = auditResponse
        )
      )

  }

}