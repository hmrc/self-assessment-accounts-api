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

package v3.controllers

import api.config.MockAppConfig
import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.domain.{Nino, TaxYear}
import api.models.errors.{ErrorWrapper, NinoFormatError}
import api.models.outcomes.ResponseWrapper
import play.api.mvc.Result
import v3.controllers.validators.MockRetrieveAutocodingStatusValidatorFactory
import v3.models.request.retrieveAutocodingStatus.RetrieveAutocodingStatusRequestData
import v3.services.MockRetrieveAutocodingStatusService
import v3.fixtures.retrieveAutocodingStatus.ResponseFixture.mtdResponse
import v3.models.errors.BusinessPartnerNotExistError
import v3.models.response.retrieveAutocodingStatus.RetrieveAutocodingStatusResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveAutocodingStatusControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveAutocodingStatusService
    with MockRetrieveAutocodingStatusValidatorFactory
    with MockAppConfig {

  override val nino   = "AA123456A"
  private val taxYear = "2023-24"

  private val requestData = RetrieveAutocodingStatusRequestData(
    nino = Nino(nino),
    taxYear = TaxYear.fromMtd(taxYear)
  )

  private val mtdResponseJson = mtdResponse(nino, taxYear)

  private val downstreamResponse: RetrieveAutocodingStatusResponse =
    RetrieveAutocodingStatusResponse(processingDate = "2023-12-17T09:30:47Z", nino = nino, taxYear = TaxYear.fromMtd(taxYear), optOutIndicator = true)

  "RetrieveAutocodingStatusController" should {
    "return OK" when {
      "happy path" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockedRetrieveAutocodingStatusService
          .retrieveAutocodingStatus(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, downstreamResponse))))

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

        MockedRetrieveAutocodingStatusService
          .retrieveAutocodingStatus(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, BusinessPartnerNotExistError))))

        runErrorTest(BusinessPartnerNotExistError)
      }
    }
  }

  private trait Test extends ControllerTest {

    val controller = new RetrieveAutocodingStatusController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveAutocodingStatusValidatorFactory,
      service = mockRetrieveAutocodingStatusService,
      cc = cc,
      idGenerator = mockIdGenerator)

    protected def callController(): Future[Result] = controller.retrieveAutocodingStatus(nino, taxYear)(fakeGetRequest)

  }

}
