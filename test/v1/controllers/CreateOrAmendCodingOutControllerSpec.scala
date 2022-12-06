/*
 * Copyright 2022 HM Revenue & Customs
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
import api.mocks.hateoas.MockHateoasFactory
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.domain.Nino
import api.models.errors._
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v1.mocks.requestParsers.MockCreateOrAmendCodingOutRequestParser
import v1.mocks.services.MockCreateOrAmendCodingOutService
import v1.models.request.createOrAmendCodingOut._
import v1.models.response.createOrAmendCodingOut.CreateOrAmendCodingOutHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateOrAmendCodingOutControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockCreateOrAmendCodingOutService
    with MockCreateOrAmendCodingOutRequestParser
    with MockHateoasFactory {

  private val taxYear = "2019-20"

  private val testHateoasLinks = Seq(
    Link(href = s"/accounts/self-assessment/$nino/$taxYear/collection/tax-code", method = PUT, rel = "create-or-amend-coding-out-underpayments"),
    Link(href = s"/accounts/self-assessment/$nino/$taxYear/collection/tax-code", method = GET, rel = "self"),
    Link(href = s"/accounts/self-assessment/$nino/$taxYear/collection/tax-code", method = DELETE, rel = "delete-coding-out-underpayments")
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

  private val requestBody = CreateOrAmendCodingOutRequestBody(taxCodeComponents = TaxCodeComponents(
    payeUnderpayment = Some(Seq(TaxCodeComponent(id = 12345, amount = 123.45))),
    selfAssessmentUnderpayment = Some(Seq(TaxCodeComponent(id = 12345, amount = 123.45))),
    debt = Some(Seq(TaxCodeComponent(id = 12345, amount = 123.45))),
    inYearAdjustment = Some(TaxCodeComponent(id = 12345, amount = 123.45))
  ))

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

  private val rawData     = CreateOrAmendCodingOutRawRequest(nino, taxYear, requestJson)
  private val requestData = CreateOrAmendCodingOutParsedRequest(Nino(nino), taxYear, requestBody)

  "handleRequest" should {
    "return OK" when {
      "the request is valid" in new Test {
        MockCreateOrAmendCodingOutRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

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
        MockCreateOrAmendCodingOutRequestParser
          .parseRequest(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTestWithAudit(NinoFormatError, maybeAuditRequestBody = Some(requestJson))
      }

      "the service returns an error" in new Test {
        MockCreateOrAmendCodingOutRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockCreateOrAmendCodingOutService
          .amend(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, RuleTaxYearNotEndedError))))

        runErrorTestWithAudit(RuleTaxYearNotEndedError, maybeAuditRequestBody = Some(requestJson))
      }
    }
  }

  private trait Test extends ControllerTest with AuditEventChecking {

    val controller = new CreateOrAmendCodingOutController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockCreateOrAmendCodingOutRequestParser,
      service = mockCreateOrAmendCodingOutService,
      hateoasFactory = mockHateoasFactory,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.createOrAmendCodingOut(nino, taxYear)(fakePostRequest(requestJson))

    protected def event(auditResponse: AuditResponse, maybeRequestBody: Option[JsValue]): AuditEvent[GenericAuditDetail] =
      AuditEvent(
        auditType = "CreateAmendCodingOutUnderpayment",
        transactionName = "create-amend-coding-out-underpayment",
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
