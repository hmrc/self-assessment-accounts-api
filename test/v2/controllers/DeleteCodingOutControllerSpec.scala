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
import api.mocks.MockIdGenerator
import api.mocks.hateoas.MockHateoasFactory
import api.mocks.services.{MockAuditService, MockEnrolmentsAuthService, MockMtdIdLookupService}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.requestParsers.MockDeleteCodingOutParser
import v1.mocks.services.MockDeleteCodingOutService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DeleteCodingOutControllerSpec extends ControllerBaseSpec
  with MockEnrolmentsAuthService
  with MockMtdIdLookupService
  with MockDeleteCodingOutService
  with MockDeleteCodingOutParser
  with MockHateoasFactory
  with MockIdGenerator
  with MockAuditService {


  private val nino = "AA123456A"
  private val taxYear = "2019-20"

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new DeleteCodingOutController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      parser = mockRequestDataParser,
      service = mockDeleteCodingOutService,
      auditService = mockAuditService,
      cc = cc,
      idGenerator = mockIdGenerator
    )
    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }


  "handleRequest" should {
    "return internal server error" in new Test {
      val result: Future[Result] = controller.handleRequest(nino, taxYear)(fakeGetRequest)
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }

}
