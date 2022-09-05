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

import api.controllers.ControllerBaseSpec
import api.hateoas.HateoasLinks
import api.mocks.MockIdGenerator
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import api.models.domain.Nino
import api.models.errors._
import api.models.hateoas.Method.GET
import api.models.hateoas.RelType.{RETRIEVE_TRANSACTION_DETAILS, SELF}
import api.models.hateoas.{HateoasWrapper, Link}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.fixtures.retrieveSelfAssessmentChargeHistory.RetrieveSelfAssessmentChargeHistoryFixture._
import v2.mocks.requestParsers.MockRetrieveSelfAssessmentChargeHistoryRequestParser
import v2.mocks.services.MockRetrieveSelfAssessmentChargeHistoryService
import v2.models.request.retrieveSelfAssessmentChargeHistory.{RetrieveSelfAssessmentChargeHistoryRawData, RetrieveSelfAssessmentChargeHistoryRequest}
import v2.models.response.retrieveSelfAssessmentChargeHistory.RetrieveSelfAssessmentChargeHistoryResponse
import v2.models.response.retrieveSelfAssessmentChargeHistory.RetrieveSelfAssessmentChargeHistoryResponse.RetrieveSelfAssessmentChargeHistoryHateoasData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveSelfAssessmentChargeHistoryControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveSelfAssessmentChargeHistoryService
    with MockHateoasFactory
    with MockRetrieveSelfAssessmentChargeHistoryRequestParser
    with HateoasLinks
    with MockIdGenerator {

  private val nino          = "AA123456A"
  private val transactionId = "anId"
  private val correlationId = "X-123"

  private val rawRequest: RetrieveSelfAssessmentChargeHistoryRawData =
    RetrieveSelfAssessmentChargeHistoryRawData(nino = nino, transactionId = transactionId)

  private val parsedRequest: RetrieveSelfAssessmentChargeHistoryRequest =
    RetrieveSelfAssessmentChargeHistoryRequest(nino = Nino(nino), transactionId = transactionId)

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

  val response: RetrieveSelfAssessmentChargeHistoryResponse = validChargeHistoryResponseObject
  val mtdResponse: JsValue                                  = mtdMultipleResponse

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveSelfAssessmentChargeHistoryController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRetrieveSelfAssessmentChargeHistoryRequestParser,
      service = mockRetrieveSelfAssessmentChargeHistoryService,
      hateoasFactory = mockHateoasFactory,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  "retrieveChargeHistory" should {
    "return OK" when {
      "happy path" in new Test {

        MockRetrieveSelfAssessmentChargeHistoryRequestParser
          .parse(rawRequest)
          .returns(Right(parsedRequest))

        MockRetrieveSelfAssessmentChargeHistoryService
          .retrieveChargeHistory(parsedRequest)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        MockHateoasFactory
          .wrap(response, RetrieveSelfAssessmentChargeHistoryHateoasData(nino, transactionId))
          .returns(
            HateoasWrapper(
              response,
              Seq(
                chargeHistoryLink,
                transactionDetailsLink
              )))

        val result: Future[Result] = controller.retrieveSelfAssessmentChargeHistory(nino, transactionId)(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdMultipleResponseWithHateoas(nino, transactionId)
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "service errors occur" must {
      def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
        s"a $mtdError error is returned from the service" in new Test {

          MockRetrieveSelfAssessmentChargeHistoryRequestParser
            .parse(rawRequest)
            .returns(Right(parsedRequest))

          MockRetrieveSelfAssessmentChargeHistoryService
            .retrieveChargeHistory(parsedRequest)
            .returns(Future.successful(Left(ErrorWrapper(correlationId, mtdError))))

          val result: Future[Result] = controller.retrieveSelfAssessmentChargeHistory(nino, transactionId)(fakeGetRequest)

          status(result) shouldBe expectedStatus
          contentAsJson(result) shouldBe Json.toJson(mtdError)
          header("X-CorrelationId", result) shouldBe Some(correlationId)

        }
      }

      val input = Seq(
        (NinoFormatError, BAD_REQUEST),
        (TransactionIdFormatError, BAD_REQUEST),
        (NotFoundError, NOT_FOUND),
        (DownstreamError, INTERNAL_SERVER_ERROR)
      )

      input.foreach(args => (serviceErrors _).tupled(args))
    }

  }

}
