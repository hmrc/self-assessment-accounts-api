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
import v1.controllers.requestParsers.ListChargesRequestParser
import v1.models.request.listCharges.ListChargesRawRequest
import v1.models.response.listCharges.ListChargesHateoasData
import v1.services.ListChargesService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ListChargesController @Inject() (val authService: EnrolmentsAuthService,
                                       val lookupService: MtdIdLookupService,
                                       requestParser: ListChargesRequestParser,
                                       service: ListChargesService,
                                       hateoasFactory: HateoasFactory,
                                       auditService: AuditService,
                                       cc: ControllerComponents,
                                       idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "ListChargesController", endpointName = "listCharges")

  def listCharges(nino: String, from: Option[String], to: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = ListChargesRawRequest(nino, from, to)

      val requestHandler =
        RequestHandler
          .withParser(requestParser)
          .withService(service.list)
          .withResultCreator(ResultCreator.hateoasListWrapping(hateoasFactory)((parsedRequest, _) =>
            ListChargesHateoasData(nino, parsedRequest.from, parsedRequest.to)))
          .withAuditing(
            AuditHandler(
              auditService,
              auditType = "listSelfAssessmentCharges",
              transactionName = "list-self-assessment-charges",
              params = Map("nino" -> nino)))

      requestHandler.handleRequest(rawData)
    }

}
