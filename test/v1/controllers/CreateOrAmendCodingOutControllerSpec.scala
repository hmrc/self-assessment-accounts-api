/*
 * Copyright 2021 HM Revenue & Customs
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

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v1.models.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockCreateOrAmendCodingOutRequestParser
import v1.mocks.services.{MockCreateOrAmendCodingOutService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.errors._
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.hateoas.Method.{DELETE, GET, PUT}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.createOrAmendCodingOut._
import v1.models.response.createOrAmendCodingOut.CreateOrAmendCodingOutHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CreateOrAmendCodingOutControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockCreateOrAmendCodingOutService
    with MockCreateOrAmendCodingOutRequestParser
    with MockHateoasFactory
    with MockIdGenerator {

  private val nino = "AA123456A"
  private val taxYear = "2019-20"
  private val correlationId = "X-123"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new CreateOrAmendCodingOutController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockCreateOrAmendCodingOutRequestParser,
      service = mockCreateOrAmendCodingOutService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  private val testHateoasLinks = Seq(
    Link(href = s"/accounts/self-assessment/$nino/$taxYear/collection/tax-code", method = PUT, rel = "create-or-amend-coding-out-underpayments"),
    Link(href = s"/accounts/self-assessment/$nino/$taxYear/collection/tax-code", method = GET, rel = "self"),
    Link(href = s"/accounts/self-assessment/$nino/$taxYear/collection/tax-code", method = DELETE, rel = "delete-coding-out-underpayments")
  )

  private val requestJson = Json.parse(
    s"""|{
        |  "payeUnderpayments": 987.93,
        |  "selfAssessmentUnderPayments": 179.00,
        |  "debts": 724.02,
        |  "inYearAdjustments": 342.87
        |}
        |""".stripMargin
  )

  private val requestBody = CreateOrAmendCodingOutRequestBody(Some(987.93), Some(179.00), Some(724.02), Some(342.87))

  val responseBody: JsValue = Json.parse(
    s"""|{
        |  "links": [
        |    {
        |      "href": "/accounts/self-assessment/{nino}/{taxYear}/collection/tax-code",
        |      "method": "PUT",
        |      "rel": "create-or-amend-coding-out-underpayments"
        |    },
        |    {
        |      "href": "/accounts/self-assessment/{nino}/{taxYear}/collection/tax-code",
        |      "method": "self",
        |      "rel": "GET"
        |    },
        |    {
        |      "href": "/accounts/self-assessment/{nino}/{taxYear}/collection/tax-code",
        |      "method": "delete-coding-out-underpayments",
        |      "rel": "DELETE"
        |    }
        |  ]
        |}
        |""".stripMargin)

  private val rawData = CreateOrAmendCodingOutRawRequest(nino, taxYear, requestJson)
  private val requestData = CreateOrAmendCodingOutParsedRequest(Nino(nino), taxYear, requestBody)

  "handleRequest" should {
    "return Ok" when {
      "the request received is valid" in new Test {

        MockCreateOrAmendCodingOutRequestParser
          .parseRequest(rawData)
          .returns(Right(requestData))

        MockCreateOrAmendCodingOutService
          .amend(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        MockHateoasFactory
          .wrap((), CreateOrAmendCodingOutHateoasData(nino, taxYear))
          .returns(HateoasWrapper((), testHateoasLinks))

        val result: Future[Result] = controller.createOrAmendCodingOut(nino, taxYear)(fakePostRequest(requestJson))
        status(result) shouldBe OK
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }
    "return the error as per spec" when {
      "parser errors occur" should {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockCreateOrAmendCodingOutRequestParser
              .parseRequest(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.createOrAmendCodingOut(nino, taxYear)(fakePostRequest(requestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleTaxYearNotEndedError, BAD_REQUEST),
          (ValueFormatError, BAD_REQUEST),
          (RuleIncorrectOrEmptyBodyError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" should {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockCreateOrAmendCodingOutRequestParser
              .parseRequest(rawData)
              .returns(Right(requestData))

            MockCreateOrAmendCodingOutService
              .amend(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.createOrAmendCodingOut(nino, taxYear)(fakePostRequest(requestJson))

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearNotEndedError, BAD_REQUEST),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}