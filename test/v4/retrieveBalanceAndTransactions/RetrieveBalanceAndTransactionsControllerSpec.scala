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

package v4.retrieveBalanceAndTransactions

import common.errors.DocNumberFormatError
import play.api.Configuration
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.models.errors.{ErrorWrapper, NinoFormatError}
import shared.models.outcomes.ResponseWrapper
import shared.routing.{Version, Version3}
import v4.retrieveBalanceAndTransactions.def1.model.RequestFixture._
import v4.retrieveBalanceAndTransactions.def1.model.ResponseFixture.{mtdResponseJson, response}
import v4.retrieveBalanceAndTransactions.model.request.RetrieveBalanceAndTransactionsRequestData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveBalanceAndTransactionsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveBalanceAndTransactionsService
    with MockRetrieveBalanceAndTransactionsValidatorFactory {

  override val apiVersion: Version = Version3

  private val requestData: RetrieveBalanceAndTransactionsRequestData = requestEverythingTrue

  "retrieveBalanceAndTransactions" should {
    "return OK" when {
      "the request is valid" in new Test {

        willUseValidator(returningSuccess(requestData))

        MockedRetrieveBalanceAndTransactionsService
          .retrieveBalanceAndTransactions(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

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

        MockedRetrieveBalanceAndTransactionsService
          .retrieveBalanceAndTransactions(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, DocNumberFormatError))))

        runErrorTest(DocNumberFormatError)
      }
    }
  }

  trait Test extends ControllerTest {

    override protected val controller = new RetrieveBalanceAndTransactionsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveBalanceAndTransactionsValidatorFactory,
      service = mockRetrieveBalanceAndTransactionsService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig returns Configuration.empty
    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = {
      controller.retrieveBalanceAndTransactions(
        nino = validNino,
        docNumber = Some(validDocNumber),
        fromDate = None,
        toDate = None,
        onlyOpenItems = Some("true"),
        includeLocks = Some("true"),
        calculateAccruedInterest = Some("true"),
        removePOA = Some("true"),
        customerPaymentInformation = Some("true"),
        includeEstimatedCharges = Some("true")
      )(fakeGetRequest)
    }

  }

}
