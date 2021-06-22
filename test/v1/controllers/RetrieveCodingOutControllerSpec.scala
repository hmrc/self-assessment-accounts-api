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

import play.api.libs.json.Json
import play.api.mvc.Result
import v1.models.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.fixtures.RetrieveCodingOutFixture.mtdResponseWithHateoas
import v1.mocks.MockIdGenerator
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockRetrieveCodingOutRequestParser
import v1.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveCodingOutService}
import v1.models.errors._
import v1.models.hateoas.{HateoasWrapper, Link}
import v1.models.hateoas.Method.{DELETE, GET, PUT}
import v1.models.hateoas.RelType.{CREATE_OR_AMEND_CODING_OUT_UNDERPAYMENTS, DELETE_CODING_OUT_UNDERPAYMENTS, SELF}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveCodingOut.{RetrieveCodingOutParsedRequest, RetrieveCodingOutRawRequest}
import v1.models.response.retrieveCodingOut._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveCodingOutControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockRetrieveCodingOutService
  with MockHateoasFactory
  with MockRetrieveCodingOutRequestParser
  with MockIdGenerator {

  private val nino = "AA123456A"
  private val taxYear = "2021-22"
  private val correlationId = "a1e8057e-fbbc-47a8-a8b4-78d9f015c253"
  private val source = "hmrcHeld"

  private val rawData = RetrieveCodingOutRawRequest(
    nino = nino,
    taxYear = taxYear,
    source = Some(source)
  )

  private val requestData = RetrieveCodingOutParsedRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    source = Some(source)
  )

  private val createOrAmendCodingOutLink = Link(
    href = s"/accounts/self-assessment/$nino/$taxYear/collection/tax-code",
    method = PUT,
    rel = CREATE_OR_AMEND_CODING_OUT_UNDERPAYMENTS
  )

  private val retrieveCodingOutLink = Link(
    href = s"/accounts/self-assessment/$nino/$taxYear/collection/tax-code",
    method = GET,
    rel = SELF
  )

  private val deleteCodingOutLink = Link(
    href = s"/accounts/self-assessment/$nino/$taxYear/collection/tax-code",
    method = DELETE,
    rel = DELETE_CODING_OUT_UNDERPAYMENTS
  )

  val unmatchedCustomerSubmissions: UnmatchedCustomerSubmissions =
    UnmatchedCustomerSubmissions(
      0,
      "2021-08-24T14:15:22Z",
      BigInt(12345678910L)
    )

  val taxCodeComponents: TaxCodeComponents =
    TaxCodeComponents(
      0,
      Some("2021-22"),
      "2021-08-24T14:15:22Z",
      "hmrcHeld",
      BigInt(12345678910L)
    )

  val taxCodeComponentObject: TaxCodeComponentsObject =
    TaxCodeComponentsObject(
      Some(Seq(taxCodeComponents)),
      Some(Seq(taxCodeComponents)),
      Some(Seq(taxCodeComponents)),
      Some(taxCodeComponents)
    )

  val unmatchedCustomerSubmissionsObject: UnmatchedCustomerSubmissionsObject =
    UnmatchedCustomerSubmissionsObject(
      Some(Seq(unmatchedCustomerSubmissions)),
      Some(Seq(unmatchedCustomerSubmissions)),
      Some(Seq(unmatchedCustomerSubmissions)),
      Some(unmatchedCustomerSubmissions)
    )

  val retrieveCodingOutResponse: RetrieveCodingOutResponse =
    RetrieveCodingOutResponse(
      Some(taxCodeComponentObject),
      Some(unmatchedCustomerSubmissionsObject)
    )

  private val mtdResponse = mtdResponseWithHateoas(nino, taxYear, source)

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveCodingOutController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRetrieveCodingOutRequestParser,
      service = mockRetrieveCodingOutService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  "RetrieveCodingOutController" should {
    "return OK" when {
      "happy path" in new Test {

        MockRetrieveCodingOutRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveCodingOutService
          .retrieveCodingOut(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveCodingOutResponse))))

        MockHateoasFactory
          .wrap(retrieveCodingOutResponse, RetrieveCodingOutHateoasData(nino, taxYear))
          .returns(HateoasWrapper(retrieveCodingOutResponse,
            Seq(
              createOrAmendCodingOutLink,
              retrieveCodingOutLink,
              deleteCodingOutLink
            )
          ))

        val result: Future[Result] = controller.retrieveCodingOut(nino, taxYear, Some(source))(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockRetrieveCodingOutRequestParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(correlationId, error, None)))

            val result: Future[Result] = controller.retrieveCodingOut(nino, taxYear, Some(source))(fakeGetRequest)

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
          (SourceFormatError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockRetrieveCodingOutRequestParser
              .parse(rawData)
              .returns(Right(requestData))

            MockRetrieveCodingOutService
              .retrieveCodingOut(requestData)
              .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

            val result: Future[Result] = controller.retrieveCodingOut(nino, taxYear, Some(source))(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (SourceFormatError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (CodingOutNotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}