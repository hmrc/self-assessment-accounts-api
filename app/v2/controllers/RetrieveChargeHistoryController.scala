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
import api.hateoas.HateoasFactory
import api.services.{EnrolmentsAuthService, MtdIdLookupService}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.{IdGenerator, Logging}
import v2.controllers.requestParsers.RetrieveChargeHistoryRequestParser
import v2.models.request.retrieveChargeHistory.RetrieveChargeHistoryRawData
import v2.models.response.retrieveChargeHistory.RetrieveChargeHistoryResponse.RetrieveChargeHistoryHateoasData
import v2.services.RetrieveChargeHistoryService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RetrieveChargeHistoryController @Inject() (val authService: EnrolmentsAuthService,
                                                 val lookupService: MtdIdLookupService,
                                                 requestParser: RetrieveChargeHistoryRequestParser,
                                                 service: RetrieveChargeHistoryService,
                                                 hateoasFactory: HateoasFactory,
                                                 cc: ControllerComponents,
                                                 idGenerator: IdGenerator,
                                                 requestHandlerFactory: RequestHandlerFactory)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrieveChargeHistoryController", endpointName = "retrieveChargeHistory")

  def retrieveChargeHistory(nino: String, transactionId: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawRequest = RetrieveChargeHistoryRawData(nino, transactionId)

      val requestHandler =
        requestHandlerFactory
          .withParser(requestParser)
          .withService(service.retrieveChargeHistory(_))
          .withHateoasResult(hateoasFactory)((_,_) => RetrieveChargeHistoryHateoasData(nino, transactionId))
          .createRequestHandler

      requestHandler.handleRequest(rawRequest)
    }

}
