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

package v3.createOrAmendCodingOut

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas
import api.hateoas.Method.{DELETE, GET, PUT}
import api.hateoas.{HateoasWrapper, MockHateoasFactory}
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.MockAuditService
import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import routing.{Version, Version2}
import v3.createOrAmendCodingOut.def1.MockCreateOrAmendCodingOutValidatorFactory
import v3.createOrAmendCodingOut.def1.model.request.{Def1_CreateOrAmendCodingOutRequestBody, Def1_CreateOrAmendCodingOutRequestData, TaxCodeComponent, TaxCodeComponents}
import v3.createOrAmendCodingOut.model.response.CreateOrAmendCodingOutHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateOrAmendCodingOutControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockCreateOrAmendCodingOutService
    with MockCreateOrAmendCodingOutValidatorFactory
    with MockAuditService
    with MockAppConfig
    with MockHateoasFactory {

  override val apiVersion: Version = Version2

  private val taxYear = "2019-20"

  private val testHateoasLinks = List(
    hateoas.Link(
      href = s"/accounts/self-assessment/$nino/$taxYear/collection/tax-code",
      method = PUT,
      rel = "create-or-amend-coding-out-underpayments"),
    hateoas.Link(href = s"/accounts/self-assessment/$nino/$taxYear/collection/tax-code", method = GET, rel = "self"),
    hateoas.Link(href = s"/accounts/self-assessment/$nino/$taxYear/collection/tax-code", method = DELETE, rel = "delete-coding-out-underpayments")
  )

  private val requestJson = Json.parse(
    s"""|{
        |  "taxCodeComponents": {
        |    "payeUnderpayment": [
        |      {
        |        "amount": 123.45,
        |        "id": 12345
        |      }
        |    ],
        |    "selfAssessmentUnderpayment": [
        |      {
        |        "amount": 123.45,
        |        "id": 12345
        |      }
        |    ],
        |    "debt": [
        |      {
        |        "amount": 123.45,
        |        "id": 12345
        |      }
        |    ],
        |    "inYearAdjustment": {
        |      "amount": 123.45,
        |      "id": 12345
        |    }
        |  }
        |}
        |""".stripMargin
  )

  private val requestBody = Def1_CreateOrAmendCodingOutRequestBody(taxCodeComponents = TaxCodeComponents(
    payeUnderpayment = Some(List(TaxCodeComponent(id = 12345, amount = 123.45))),
    selfAssessmentUnderpayment = Some(List(TaxCodeComponent(id = 12345, amount = 123.45))),
    debt = Some(List(TaxCodeComponent(id = 12345, amount = 123.45))),
    inYearAdjustment = Some(TaxCodeComponent(id = 12345, amount = 123.45))
  ))

  private val requestData = Def1_CreateOrAmendCodingOutRequestData(Nino(nino), TaxYear.fromMtd(taxYear), requestBody)

  val mtdResponseJson: JsValue =
    Json.parse(s"""{
                  |  "links": [
                  |    {
                  |      "href": "/accounts/self-assessment/$nino/$taxYear/collection/tax-code",
                  |      "method": "PUT",
                  |      "rel": "create-or-amend-coding-out-underpayments"
                  |    },
                  |    {
                  |      "href": "/accounts/self-assessment/$nino/$taxYear/collection/tax-code",
                  |      "method": "GET",
                  |      "rel": "self"
                  |    },
                  |    {
                  |      "href": "/accounts/self-assessment/$nino/$taxYear/collection/tax-code",
                  |      "method": "DELETE",
                  |      "rel": "delete-coding-out-underpayments"
                  |    }
                  |  ]
                  |}
                  |""".stripMargin)

  "handleRequest" should {
    "return OK" when {

      "the request is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateOrAmendCodingOutService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), CreateOrAmendCodingOutHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), testHateoasLinks))

        runOkTestWithAudit(
          expectedStatus = OK,
          maybeExpectedResponseBody = Some(mtdResponseJson),
          maybeAuditRequestBody = Some(requestJson),
          maybeAuditResponseBody = Some(mtdResponseJson)
        )
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))
        runErrorTestWithAudit(NinoFormatError, maybeAuditRequestBody = Some(requestJson))
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockCreateOrAmendCodingOutService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotEndedError))))

        runErrorTestWithAudit(RuleTaxYearNotEndedError, maybeAuditRequestBody = Some(requestJson))
      }
    }
  }

  private trait Test extends ControllerTest with AuditEventChecking[GenericAuditDetail] {

    override protected val controller = new CreateOrAmendCodingOutController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockCreateOrAmendCodingOutValidatorFactory,
      service = mockCreateOrAmendCodingOutService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockAppConfig.featureSwitches.returns(Configuration("allowTemporalValidationSuspension.enabled" -> true)).anyNumberOfTimes()
    MockAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.createOrAmendCodingOut(nino, taxYear)(fakePostRequest(requestJson))

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendCodingOutUnderpayment",
        transactionName = "create-amend-coding-out-underpayment",
        detail = GenericAuditDetail(
          versionNumber = apiVersion.name,
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
