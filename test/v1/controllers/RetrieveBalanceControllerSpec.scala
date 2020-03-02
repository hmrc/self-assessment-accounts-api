/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.controllers

import mocks.MockAppConfig
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.fixtures.RetrieveBalanceFixture
import v1.hateoas.HateoasLinks
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.requestParsers.MockRetrieveBalanceRequestParser
import v1.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveBalanceService}
import v1.models.errors._
import v1.models.hateoas.HateoasWrapper
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveBalance.{RetrieveBalanceParsedRequest, RetrieveBalanceRawRequest}
import v1.models.response.retrieveBalance.RetrieveBalanceHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveBalanceControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveBalanceService
    with MockHateoasFactory
    with MockAppConfig
    with MockRetrieveBalanceRequestParser
    with HateoasLinks {

  private val nino = "AA123456A"
  private val correlationId = "X-123"

  private val rawRequest: RetrieveBalanceRawRequest =
    RetrieveBalanceRawRequest(nino = nino)

  private val parsedRequest: RetrieveBalanceParsedRequest =
    RetrieveBalanceParsedRequest(
      nino = Nino(nino)
    )

  private val retrieveBalanceResponse = RetrieveBalanceFixture.fullModel
  private val mtdResponse = RetrieveBalanceFixture.fullMtdResponseJsonWithHateoas(nino)

  trait Test {
    val hc = HeaderCarrier()

    val controller = new RetrieveBalanceController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRetrieveBalanceRequestParser,
      service = mockRetrieveBalanceService,
      hateoasFactory = mockHateoasFactory,
      cc = cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  "retrieveBalance" should {
    "return OK" when {
      "happy path" in new Test {

        MockedAppConfig.apiGatewayContext returns "accounts/self-assessment" anyNumberOfTimes()

        MockRetrieveBalanceRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockRetrieveBalanceService
          .retrieveBalance(parsedRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveBalanceResponse))))

        MockHateoasFactory
          .wrap(retrieveBalanceResponse, RetrieveBalanceHateoasData(nino))
          .returns(HateoasWrapper(retrieveBalanceResponse, Seq(retrieveBalance(mockAppConfig, nino, isSelf = true),
            retrieveTransactions(mockAppConfig, nino, isSelf = false))))

        val result: Future[Result] = controller.retrieveBalance(nino)(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockRetrieveBalanceRequestParser
              .parse(rawRequest)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.retrieveBalance(nino)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockRetrieveBalanceRequestParser
              .parse(rawRequest)
              .returns(Right(parsedRequest))

            MockRetrieveBalanceService
              .retrieveBalance(parsedRequest)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.retrieveBalance(nino)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}





