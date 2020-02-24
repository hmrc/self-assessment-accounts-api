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
import uk.gov.hmrc.http.HeaderCarrier
import v1.fixtures.RetrieveBalanceFixture
import v1.hateoas.HateoasLinks
import v1.mocks.hateoas.MockHateoasFactory
import v1.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService}
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveBalance.{RetrieveBalanceParsedRequest, RetrieveBalanceRawRequest}

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
      nino = nino
    )

  private val retrieveBalanceResponse = RetrieveBalanceFixture.fullDesResponse
  private val mtdResponse = RetrieveBalanceFixture.mtdJsonWithHateoas(nino = nino)

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
          .wrap(retrieveBalanceResponse, )
      }
    }
  }
}





