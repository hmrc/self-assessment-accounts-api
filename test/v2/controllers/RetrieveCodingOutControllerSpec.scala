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

package v2.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.mocks.hateoas.MockHateoasFactory
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.hateoas.Method.{DELETE, GET, PUT}
import api.models.hateoas.RelType.{CREATE_OR_AMEND_CODING_OUT_UNDERPAYMENTS, DELETE_CODING_OUT_UNDERPAYMENTS, SELF}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.mvc.Result
import v2.fixtures.RetrieveCodingOutFixture.mtdResponseWithHateoas
import v2.mocks.requestParsers.MockRetrieveCodingOutRequestParser
import v2.mocks.services.MockRetrieveCodingOutService
import v2.models.request.retrieveCodingOut.{RetrieveCodingOutParsedRequest, RetrieveCodingOutRawRequest}
import v2.models.response.retrieveCodingOut.{RetrieveCodingOutHateoasData, RetrieveCodingOutResponse, TaxCodeComponents, TaxCodeComponentsObject, UnmatchedCustomerSubmissions, UnmatchedCustomerSubmissionsObject}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveCodingOutControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveCodingOutService
    with MockHateoasFactory
    with MockRetrieveCodingOutRequestParser {

  private val taxYear = "2021-22"
  private val source  = "hmrcHeld"

  private val rawData = RetrieveCodingOutRawRequest(
    nino = nino,
    taxYear = taxYear,
    source = Some(source)
  )

  private val requestData = RetrieveCodingOutParsedRequest(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear),
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
      Some(BigInt(12345678910L))
    )
  val taxCodeComponents: TaxCodeComponents =
    TaxCodeComponents(
      0,
      Some("2021-22"),
      "2021-08-24T14:15:22Z",
      "hmrcHeld",
      Some(BigInt(12345678910L))
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

  private val mtdResponseJson = mtdResponseWithHateoas(nino, taxYear, source)

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
          .returns(
            HateoasWrapper(
              retrieveCodingOutResponse,
              Seq(
                createOrAmendCodingOutLink,
                retrieveCodingOutLink,
                deleteCodingOutLink
              )))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponseJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockRetrieveCodingOutRequestParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockRetrieveCodingOutRequestParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveCodingOutService
          .retrieveCodingOut(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, CodingOutNotFoundError))))

        runErrorTest(CodingOutNotFoundError)
      }
    }
  }

  private trait Test extends ControllerTest {

    val controller = new RetrieveCodingOutController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRetrieveCodingOutRequestParser,
      service = mockRetrieveCodingOutService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.retrieveCodingOut(nino, taxYear, Some(source))(fakeGetRequest)
  }

}
