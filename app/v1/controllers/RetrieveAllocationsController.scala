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

package v1.controllers

import api.controllers._
import api.hateoas.HateoasFactory
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.RetrieveAllocationsRequestParser
import v1.models.request.retrieveAllocations.RetrieveAllocationsRawRequest
import v1.models.response.retrieveAllocations.RetrieveAllocationsHateoasData
import v1.services.RetrieveAllocationsService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RetrieveAllocationsController @Inject() (val authService: EnrolmentsAuthService,
                                               val lookupService: MtdIdLookupService,
                                               auditService: AuditService,
                                               requestParser: RetrieveAllocationsRequestParser,
                                               service: RetrieveAllocationsService,
                                               hateoasFactory: HateoasFactory,
                                               cc: ControllerComponents,
                                               idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveAllocationsController",
      endpointName = "retrieveAllocations"
    )

  def retrieveAllocations(nino: String, paymentId: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = RetrieveAllocationsRawRequest(nino, paymentId)

      val requestHandler =
        requestHandlerFactory
          .withParser(requestParser)
          .withService(service.retrieveAllocations(_))
          .withResultCreator(ResultCreator.hateoasListWrapping(hateoasFactory)((_, _) => RetrieveAllocationsHateoasData(nino, paymentId)))
          .withAuditing(AuditHandler(
            auditService,
            auditType = "retrieveASelfAssessmentPaymentsAllocationDetails",
            transactionName = "retrieve-a-self-assessment-payments-allocation-details",
            params = Map("nino" -> nino)))
          .createRequestHandler

      requestHandler.handleRequest(rawData)
    }

}
