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

package v3.retrieveChargeHistoryByTransactionId

import common.hateoas.RelType.{RETRIEVE_TRANSACTION_DETAILS, SELF}
import play.api.Configuration
import play.api.mvc.Result
import shared.controllers.{ControllerBaseSpec, ControllerTestRunner}
import shared.hateoas.Method.GET
import shared.hateoas.{HateoasWrapper, Link, MockHateoasFactory}
import shared.models.domain.TransactionId
import shared.models.errors.{ErrorWrapper, NinoFormatError}
import shared.models.outcomes.ResponseWrapper
import shared.routing.{Version, Version3}
import v3.retrieveChargeHistoryByTransactionId.def1.RetrieveChargeHistoryFixture._
import v3.retrieveChargeHistoryByTransactionId.def1.models.request.Def1_RetrieveChargeHistoryByTransactionIdRequestData
import v3.retrieveChargeHistoryByTransactionId.model.request.RetrieveChargeHistoryByTransactionIdRequestData
import v3.retrieveChargeHistoryByTransactionId.model.response.RetrieveChargeHistoryResponse
import v3.retrieveChargeHistoryByTransactionId.model.response.RetrieveChargeHistoryResponse.RetrieveChargeHistoryHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveChargeHistoryByTransactionIdControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveChargeHistoryByTransactionIdService
    with MockHateoasFactory
    with MockRetrieveChargeHistoryByTransactionIdValidatorFactory {

  override val apiVersion: Version = Version3

  private val transactionId = "anId"

  private val requestData: RetrieveChargeHistoryByTransactionIdRequestData =
    Def1_RetrieveChargeHistoryByTransactionIdRequestData(nino = parsedNino, transactionId = TransactionId(transactionId))

  val chargeHistoryLink: Link = Link(
    href = s"/accounts/self-assessment/$validNino/charges/$transactionId",
    method = GET,
    rel = SELF
  )

  val transactionDetailsLink: Link = Link(
    href = s"/accounts/self-assessment/$validNino/transactions/$transactionId",
    method = GET,
    rel = RETRIEVE_TRANSACTION_DETAILS
  )

  val response: RetrieveChargeHistoryResponse = validChargeHistoryResponseObject

  "retrieveChargeHistory" should {
    "return OK" when {
      "the request is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveChargeHistoryByTransactionIdService
          .retrieveChargeHistoryByTransactionId(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        MockHateoasFactory
          .wrap(response, RetrieveChargeHistoryHateoasData(validNino, transactionId))
          .returns(
            HateoasWrapper(
              response,
              List(
                chargeHistoryLink,
                transactionDetailsLink
              )))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdMultipleResponseWithHateoas(validNino, transactionId)))
      }
    }
    "return the error as per spec" when {
      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveChargeHistoryByTransactionIdService
          .retrieveChargeHistoryByTransactionId(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, NinoFormatError))))

        runErrorTest(NinoFormatError)
      }
    }

  }

  private trait Test extends ControllerTest {

    override protected val controller = new RetrieveChargeHistoryByTransactionIdController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockRetrieveChargeHistoryByTransactionIdValidatorFactory,
      service = mockRetrieveChargeHistoryByTransactionIdService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedSharedAppConfig.featureSwitchConfig returns Configuration.empty
    MockedSharedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = controller.retrieveChargeHistoryByTransactionId(validNino, transactionId)(fakeGetRequest)
  }

}
