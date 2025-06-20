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
import common.hateoas.RelType.{CREATE_OR_AMEND_CODING_OUT_UNDERPAYMENTS, DELETE_CODING_OUT_UNDERPAYMENTS, SELF}
import common.models.MtdSource
import config.MockSaAccountsConfig
import play.api.Configuration
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.hateoas.Method.{DELETE, GET, PUT}
import shared.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import shared.models.domain.TaxYear
import shared.models.errors.{ErrorWrapper, NinoFormatError}
import shared.models.outcomes.ResponseWrapper
import shared.routing.{Version, Version3}
import v3.retrieveCodingOut.def1.MockRetrieveCodingOutValidatorFactory
import v3.retrieveCodingOut.def1.model.reponse.RetrieveCodingOutFixture._
import v3.retrieveCodingOut.def1.model.request.Def1_RetrieveCodingOutRequestData
import v3.retrieveCodingOut.model.response.RetrieveCodingOutHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveCodingOutControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveCodingOutService
    with MockRetrieveCodingOutValidatorFactory
    with MockSaAccountsConfig
    with MockHateoasFactory {

  override val apiVersion: Version = Version3
  private val taxYear              = "2021-22"
  private val source               = "hmrcHeld"

  private val requestData = Def1_RetrieveCodingOutRequestData(
    nino = parsedNino,
    taxYear = TaxYear.fromMtd(taxYear),
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

  private val deleteCodingOutLink = Link(
    href = s"/accounts/self-assessment/$validNino/$taxYear/collection/tax-code",
    method = DELETE,
    rel = DELETE_CODING_OUT_UNDERPAYMENTS
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
