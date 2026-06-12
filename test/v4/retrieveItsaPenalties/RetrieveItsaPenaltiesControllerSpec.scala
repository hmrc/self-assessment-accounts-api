/*
 * Copyright 2026 HM Revenue & Customs
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

package v4.retrieveItsaPenalties

import play.api.Configuration
import play.api.mvc.Result
import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.errors.{ErrorWrapper, NinoFormatError, InternalError}
import api.models.outcomes.ResponseWrapper
import v4.retrieveItsaPenalties.model.response.RetrieveItsaPenaltiesFixture.*
import v4.retrieveItsaPenalties.model.request.RetrieveItsaPenaltiesRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveItsaPenaltiesControllerSpec
    extends ControllerTestRunner
    with MockRetrieveItsaPenaltiesService
    with MockRetrieveItsaPenaltiesValidatorFactory {

  private val requestData: RetrieveItsaPenaltiesRequestData =
    RetrieveItsaPenaltiesRequestData(nino = parsedNino)

  "retrieveItsaPenalties" should {
    "return 200 (OK)" when {
      "the request is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveItsaPenaltiesService
          .retrieveItsaPenalties(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdJson))
      }
    }

    "return validation error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveItsaPenaltiesService
          .retrieveItsaPenalties(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, InternalError))))

        runErrorTest(InternalError)
      }
    }

  }

  private trait Test extends ControllerTest {

    override protected val controller: RetrieveItsaPenaltiesController = new RetrieveItsaPenaltiesController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveItsaPenaltiesValidatorFactory,
      service = mockRetrieveItsaPenaltiesService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitchConfig.anyNumberOfTimes() returns Configuration.empty
    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.retrieveItsaPenalties(validNino)(fakeGetRequest)

  }

}
