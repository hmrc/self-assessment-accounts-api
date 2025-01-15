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

package v3.retrieveCodingOut

import common.errors.CodingOutNotFoundError
import play.api.Configuration
import play.api.mvc.Result
import shared.config.MockSharedAppConfig
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.hateoas.Method.{DELETE, GET, PUT}
import shared.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.{ErrorWrapper, NinoFormatError}
import shared.models.outcomes.ResponseWrapper
import shared.routing.{Version, Version2}
import v3.common.models.MtdSource
import v3.hateoas.RelType.{CREATE_OR_AMEND_CODING_OUT_UNDERPAYMENTS, DELETE_CODING_OUT_UNDERPAYMENTS, SELF}
import v3.retrieveCodingOut.def1.MockRetrieveCodingOutValidatorFactory
import v3.retrieveCodingOut.def1.model.reponse.RetrieveCodingOutFixture.mtdResponseWithHateoas
import v3.retrieveCodingOut.def1.model.request.Def1_RetrieveCodingOutRequestData
import v3.retrieveCodingOut.def1.model.response._
import v3.retrieveCodingOut.model.response.RetrieveCodingOutHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveCodingOutControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveCodingOutService
    with MockRetrieveCodingOutValidatorFactory
    with MockSharedAppConfig
    with MockHateoasFactory {

  override val apiVersion: Version = Version2
  private val taxYear              = "2021-22"
  private val source               = "hmrcHeld"

  private val requestData = Def1_RetrieveCodingOutRequestData(
    nino = Nino(validNino),
    taxYear = TaxYear.currentTaxYear()
,
    source = Some(MtdSource.parser(source))
  )



  private val createOrAmendCodingOutLink = Link(
    href = s"/accounts/self-assessment/$validNino/$taxYear/collection/tax-code",
    method = PUT,
    rel = CREATE_OR_AMEND_CODING_OUT_UNDERPAYMENTS
  )

  private val retrieveCodingOutLink = Link(
    href = s"/accounts/self-assessment/$validNino/$taxYear/collection/tax-code",
    method = GET,
    rel = SELF
  )

  private val deleteCodingOutLink= Link(
    href = s"/accounts/self-assessment/$validNino/$taxYear/collection/tax-code",
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
      Some(List(taxCodeComponents)),
      Some(List(taxCodeComponents)),
      Some(List(taxCodeComponents)),
      Some(taxCodeComponents)
    )

  val unmatchedCustomerSubmissionsObject: UnmatchedCustomerSubmissionsObject =
    UnmatchedCustomerSubmissionsObject(
      Some(List(unmatchedCustomerSubmissions)),
      Some(List(unmatchedCustomerSubmissions)),
      Some(List(unmatchedCustomerSubmissions)),
      Some(unmatchedCustomerSubmissions)
    )

  val retrieveCodingOutResponse: Def1_RetrieveCodingOutResponse =
    Def1_RetrieveCodingOutResponse(
      Some(taxCodeComponentObject),
      Some(unmatchedCustomerSubmissionsObject)
    )

  private val mtdResponseJson = mtdResponseWithHateoas(validNino, taxYear, source)

  "RetrieveCodingOutController" should {
    "return OK" when {
      "happy path" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveCodingOutService
          .retrieveCodingOut(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveCodingOutResponse))))

        MockHateoasFactory
          .wrap(retrieveCodingOutResponse, RetrieveCodingOutHateoasData(validNino, taxYear))
          .returns(
            HateoasWrapper(
              retrieveCodingOutResponse,
              List(
                createOrAmendCodingOutLink,
                retrieveCodingOutLink,
                deleteCodingOutLink
              )))

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

        MockRetrieveCodingOutService
          .retrieveCodingOut(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, CodingOutNotFoundError))))

        runErrorTest(CodingOutNotFoundError)
      }
    }
  }

  private trait Test extends ControllerTest {

    override protected val controller = new RetrieveCodingOutController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveCodingOutValidatorFactory,
      service = mockRetrieveCodingOutService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig returns Configuration.empty
    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false
    protected def callController(): Future[Result] = controller.retrieveCodingOut(validNino, taxYear, Some(source))(fakeGetRequest)
  }

}
