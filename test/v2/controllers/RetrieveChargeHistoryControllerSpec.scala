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
import api.mocks.hateoas.MockHateoasFactory
import api.models.domain.Nino
import api.models.errors._
import api.models.hateoas.Method.GET
import api.models.hateoas.RelType.{RETRIEVE_TRANSACTION_DETAILS, SELF}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.mvc.Result
import v2.fixtures.retrieveChargeHistory.RetrieveChargeHistoryFixture._
import v2.mocks.requestParsers.MockRetrieveChargeHistoryRequestParser
import v2.mocks.services.MockRetrieveChargeHistoryService
import v2.models.request.retrieveChargeHistory.{RetrieveChargeHistoryRawData, RetrieveChargeHistoryRequest}
import v2.models.response.retrieveChargeHistory.RetrieveChargeHistoryResponse
import v2.models.response.retrieveChargeHistory.RetrieveChargeHistoryResponse.RetrieveChargeHistoryHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveChargeHistoryControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveChargeHistoryService
    with MockHateoasFactory
    with MockRetrieveChargeHistoryRequestParser {

  private val transactionId = "anId"

  private val rawRequest: RetrieveChargeHistoryRawData =
    RetrieveChargeHistoryRawData(nino = nino, transactionId = transactionId)

  private val parsedRequest: RetrieveChargeHistoryRequest =
    RetrieveChargeHistoryRequest(nino = Nino(nino), transactionId = transactionId)

  val chargeHistoryLink: Link =
    Link(
      href = s"/accounts/self-assessment/$nino/charges/$transactionId",
      method = GET,
      rel = SELF
    )

  val transactionDetailsLink: Link =
    Link(
      href = s"/accounts/self-assessment/$nino/transactions/$transactionId",
      method = GET,
      rel = RETRIEVE_TRANSACTION_DETAILS
    )

  val response: RetrieveChargeHistoryResponse = validChargeHistoryResponseObject

  "retrieveChargeHistory" should {
    "return OK" when {
      "the request is valid" in new Test {
        MockRetrieveChargeHistoryRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockRetrieveChargeHistoryService
          .retrieveChargeHistory(parsedRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        MockHateoasFactory
          .wrap(response, RetrieveChargeHistoryHateoasData(nino, transactionId))
          .returns(
            HateoasWrapper(
              response,
              Seq(
                chargeHistoryLink,
                transactionDetailsLink
              )))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(mtdMultipleResponseWithHateoas(nino, transactionId)))
      }
    }
    "return the error as per spec" when {
      "the service returns an error" in new Test {
        MockRetrieveChargeHistoryRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockRetrieveChargeHistoryService
          .retrieveChargeHistory(parsedRequest)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, NinoFormatError))))

        runErrorTest(NinoFormatError)
      }
    }

  }

  private trait Test extends ControllerTest {

    private val controller = new RetrieveChargeHistoryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRetrieveChargeHistoryRequestParser,
      service = mockRetrieveChargeHistoryService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = controller.retrieveChargeHistory(nino, transactionId)(fakeGetRequest)
  }

}
