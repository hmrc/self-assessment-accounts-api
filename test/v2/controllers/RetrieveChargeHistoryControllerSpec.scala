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

import config.MockAppConfig
import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.hateoas
import api.hateoas.Method.GET
import api.hateoas.RelType.{RETRIEVE_TRANSACTION_DETAILS, SELF}
import api.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import api.models.domain.{Nino, TransactionId}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.Configuration
import play.api.mvc.Result
import v2.controllers.validators.MockRetrieveChargeHistoryValidatorFactory
import v2.fixtures.retrieveChargeHistory.RetrieveChargeHistoryFixture._
import v2.models.request.retrieveChargeHistory.RetrieveChargeHistoryRequestData
import v2.models.response.retrieveChargeHistory.RetrieveChargeHistoryResponse
import v2.models.response.retrieveChargeHistory.RetrieveChargeHistoryResponse.RetrieveChargeHistoryHateoasData
import v2.services.MockRetrieveChargeHistoryService
import routing.{Version, Version2}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveChargeHistoryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveChargeHistoryService
    with MockAppConfig
    with MockHateoasFactory
    with MockRetrieveChargeHistoryValidatorFactory {

  override val apiVersion: Version = Version2
  private val transactionId        = "anId"

  private val requestData: RetrieveChargeHistoryRequestData =
    RetrieveChargeHistoryRequestData(nino = Nino(nino), transactionId = TransactionId(transactionId), chargeReference = None)

  val chargeHistoryLink: Link =
    hateoas.Link(
      href = s"/accounts/self-assessment/$nino/charges/$transactionId",
      method = GET,
      rel = SELF
    )

  val transactionDetailsLink: Link =
    hateoas.Link(
      href = s"/accounts/self-assessment/$nino/transactions/$transactionId",
      method = GET,
      rel = RETRIEVE_TRANSACTION_DETAILS
    )

  val response: RetrieveChargeHistoryResponse = validChargeHistoryResponseObject

  "retrieveChargeHistory" should {
    "return OK" when {
      "the request is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveChargeHistoryService
          .retrieveChargeHistory(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        MockHateoasFactory
          .wrap(response, RetrieveChargeHistoryHateoasData(nino, transactionId))
          .returns(
            HateoasWrapper(
              response,
              List(
                chargeHistoryLink,
                transactionDetailsLink
              )))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdMultipleResponseWithHateoas(nino, transactionId)))
      }
    }
    "return the error as per spec" when {
      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveChargeHistoryService
          .retrieveChargeHistory(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, NinoFormatError))))

        runErrorTest(NinoFormatError)
      }
    }

  }

  private trait Test extends ControllerTest {

    override protected val controller = new RetrieveChargeHistoryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveChargeHistoryValidatorFactory,
      service = mockRetrieveChargeHistoryService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockAppConfig.featureSwitches returns Configuration.empty
    MockAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false
    protected def callController(): Future[Result] = controller.retrieveChargeHistory(nino, transactionId, None)(fakeGetRequest)
  }

}
