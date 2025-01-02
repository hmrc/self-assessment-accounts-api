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

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import shared.config.SharedAppConfig
import shared.controllers.{AuthorisedController, EndpointLogContext, RequestContext, RequestHandler}
import shared.services.{EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ListPaymentsAndAllocationDetailsController @Inject() (val authService: EnrolmentsAuthService,
                                                            val lookupService: MtdIdLookupService,
                                                            validatorFactory: ListPaymentsAndAllocationDetailsValidatorFactory,
                                                            service: ListPaymentsAndAllocationDetailsService,
                                                            cc: ControllerComponents,
                                                            idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: SharedAppConfig)
    extends AuthorisedController(cc) {

  override val endpointName: String = "list-payments-and-allocation-details"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "ListPaymentsAndAllocationDetailsController", endpointName = "listPaymentsAndAllocationDetails")

  def listPayments(nino: String,
                   fromDate: Option[String],
                   toDate: Option[String],
                   paymentLot: Option[String],
                   paymentLotItem: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, fromDate, toDate, paymentLot, paymentLotItem)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.listPaymentsAndAllocationDetails)
          .withPlainJsonResult()

      requestHandler.handleRequest()
    }

}
