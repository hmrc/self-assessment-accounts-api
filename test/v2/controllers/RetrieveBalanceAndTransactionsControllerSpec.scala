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

package v2.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.mvc.Result
import v2.fixtures.retrieveBalanceAndTransactions.RequestFixture._
import v2.fixtures.retrieveBalanceAndTransactions.ResponseFixture.{mtdResponseJson, response}
import v2.mocks.requestParsers.MockRetrieveBalanceAndTransactionsRequestParser
import v2.mocks.services.MockRetrieveBalanceAndTransactionsService
import v2.models.request.retrieveBalanceAndTransactions.{RetrieveBalanceAndTransactionsRawData, RetrieveBalanceAndTransactionsRequest}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveBalanceAndTransactionsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveBalanceAndTransactionsService
    with MockRetrieveBalanceAndTransactionsRequestParser {

  private val rawRequest: RetrieveBalanceAndTransactionsRawData    = inputDataEverythingTrue
  private val parsedRequest: RetrieveBalanceAndTransactionsRequest = requestEverythingTrue

  "retrieveBalanceAndTransactions" should {
    "return OK" when {
      "the request is valid" in new RunControllerTest {

        protected def setupMocks(): Unit = {
          MockRetrieveBalanceAndTransactionsRequestParser
            .parse(rawRequest)
            .returns(Right(parsedRequest))

          MockRetrieveBalanceAndTransactionsService
            .retrieveBalanceAndTransactions(parsedRequest)
            .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))
        }

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdResponseJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new RunControllerTest {

        protected def setupMocks(): Unit = {
          MockRetrieveBalanceAndTransactionsRequestParser
            .parse(rawRequest)
            .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))
        }

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new RunControllerTest {

        protected def setupMocks(): Unit = {
          MockRetrieveBalanceAndTransactionsRequestParser
            .parse(rawRequest)
            .returns(Right(parsedRequest))

          MockRetrieveBalanceAndTransactionsService
            .retrieveBalanceAndTransactions(parsedRequest)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, DocNumberFormatError))))
        }

        runErrorTest(DocNumberFormatError)
      }
    }
  }

  trait RunControllerTest extends RunTest {

    val controller = new RetrieveBalanceAndTransactionsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRetrieveBalanceAndTransactionsRequestParser,
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
