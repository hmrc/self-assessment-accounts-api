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

import api.controllers._
import api.services.{EnrolmentsAuthService, MtdIdLookupService}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.{IdGenerator, Logging}
import v2.controllers.requestParsers.ListPaymentsAndAllocationDetailsRequestParser
import v2.models.request.listPaymentsAndAllocationDetails.ListPaymentsAndAllocationDetailsRawData
import v2.services.ListPaymentsAndAllocationDetailsService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ListPaymentsAndAllocationDetailsController @Inject() (val authService: EnrolmentsAuthService,
                                                            val lookupService: MtdIdLookupService,
                                                            requestParser: ListPaymentsAndAllocationDetailsRequestParser,
                                                            service: ListPaymentsAndAllocationDetailsService,
                                                            cc: ControllerComponents,
                                                              idGenerator: IdGenerator,
                                                            requestHandlerFactory: RequestHandlerFactory)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "ListPaymentsAndAllocationDetailsController", endpointName = "listPaymentsAndAllocationDetails")

  def listPayments(nino: String,
                   fromDate: Option[String],
                   toDate: Option[String],
                   paymentLot: Option[String],
                   paymentLotItem: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)


      val rawData = ListPaymentsAndAllocationDetailsRawData(
        nino = nino,
        fromDate = fromDate,
        toDate = toDate,
        paymentLot = paymentLot,
        paymentLotItem = paymentLotItem
      )

      val requestHandler =
        requestHandlerFactory
          .withParser(requestParser)
          .withService(service.listPaymentsAndAllocationDetails(_))
          .withResultCreator(ResultCreator.json())
          .createRequestHandler

      requestHandler.handleRequest(rawData)
    }

}
