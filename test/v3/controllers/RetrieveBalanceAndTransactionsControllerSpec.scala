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
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.Configuration
import play.api.mvc.Result
import v3.controllers.validators.MockRetrieveBalanceAndTransactionsValidatorFactory
import v3.fixtures.retrieveBalanceAndTransactions.RequestFixture._
import v3.fixtures.retrieveBalanceAndTransactions.ResponseFixture.{mtdResponseJsonWithoutPOAAmountJson, response}
import v3.models.request.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsRequestData
import v3.services.MockRetrieveBalanceAndTransactionsService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveBalanceAndTransactionsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockAppConfig
    with MockRetrieveBalanceAndTransactionsService
    with MockRetrieveBalanceAndTransactionsValidatorFactory {

  private val requestData: RetrieveBalanceAndTransactionsRequestData = requestEverythingTrue

  "retrieveBalanceAndTransactions" should {
    "return OK" when {
      "the request is valid" in new Test {

        MockAppConfig.featureSwitches.returns(Configuration("isPOARelevantAmount.enabled" -> false)).anyNumberOfTimes()
        willUseValidator(returningSuccess(requestData))

        MockedRetrieveBalanceAndTransactionsService
          .retrieveBalanceAndTransactions(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponseJsonWithoutPOAAmountJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        MockAppConfig.featureSwitches.returns(Configuration("isPOARelevantAmount.enabled" -> true)).anyNumberOfTimes()
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        MockAppConfig.featureSwitches.returns(Configuration("isPOARelevantAmount.enabled" -> true)).anyNumberOfTimes()
        willUseValidator(returningSuccess(requestData))

        MockedRetrieveBalanceAndTransactionsService
          .retrieveBalanceAndTransactions(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, DocNumberFormatError))))

        runErrorTest(DocNumberFormatError)
      }
    }
  }

  trait Test extends ControllerTest {

    lazy val controller = new RetrieveBalanceAndTransactionsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveBalanceAndTransactionsValidatorFactory,
      service = mockRetrieveBalanceAndTransactionsService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = {
      controller.retrieveBalanceAndTransactions(
        nino = nino,
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
