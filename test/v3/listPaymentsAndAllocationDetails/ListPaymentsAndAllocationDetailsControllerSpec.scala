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

package v3.listPaymentsAndAllocationDetails

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.domain.{DateRange, Nino}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import config.MockAppConfig
import play.api.Configuration
import play.api.mvc.Result
import v3.listPaymentsAndAllocationDetails.def1.MockListPaymentsAndAllocationDetailsValidatorFactory
import v3.listPaymentsAndAllocationDetails.def1.model.request.Def1_ListPaymentsAndAllocationDetailsRequestData
import v3.listPaymentsAndAllocationDetails.def1.model.response.ResponseFixtures.{mtdResponseJson, responseObject}

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListPaymentsAndAllocationDetailsControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockAppConfig
    with MockListPaymentsAndAllocationDetailsValidatorFactory
    with MockListPaymentsAndAllocationDetailsService {

  private val requestData =
    Def1_ListPaymentsAndAllocationDetailsRequestData(
      Nino(nino),
      Some(DateRange(LocalDate.parse("2022-08-15"), LocalDate.parse("2022-09-15"))),
      Some("paymentLot"),
      Some("paymentLotItem"))

  "retrieveList" should {
    "return a payments and allocation details response" when {
      "the request is valid" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockListPaymentsAndAllocationDetailsService
          .listPaymentsAndAllocationDetails(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseObject))))

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

        MockListPaymentsAndAllocationDetailsService
          .listPaymentsAndAllocationDetails(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, PaymentLotFormatError))))

        runErrorTest(PaymentLotFormatError)
      }
    }
  }

  private trait Test extends ControllerTest {

    override protected val controller = new ListPaymentsAndAllocationDetailsController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      validatorFactory = mockListPaymentsAndAllocationDetailsValidatorFactory,
      service = mockListPaymentsAndAllocationDetailsService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockAppConfig.featureSwitches returns Configuration.empty
    MockAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] =
      controller.listPayments(nino, Some("2022-08-15"), Some("2022-09-15"), Some("paymentLot"), Some("paymentLotItem"))(fakeGetRequest)

  }

}
