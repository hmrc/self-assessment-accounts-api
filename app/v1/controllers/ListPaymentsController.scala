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
import v1.controllers.requestParsers.ListPaymentsRequestParser
import v1.models.request.listPayments.ListPaymentsRawRequest
import v1.models.response.listPayments.ListPaymentsHateoasData
import v1.services.ListPaymentsService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class ListPaymentsController @Inject() (val authService: EnrolmentsAuthService,
                                        val lookupService: MtdIdLookupService,
                                        requestParser: ListPaymentsRequestParser,
                                        service: ListPaymentsService,
                                        hateoasFactory: HateoasFactory,
                                        auditService: AuditService,
                                        cc: ControllerComponents,
                                        idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "ListPaymentsController", endpointName = "listPayments")

  def listPayments(nino: String, from: Option[String], to: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = ListPaymentsRawRequest(nino, from, to)

      val requestHandler =
        RequestHandlerFactory
          .withParser(requestParser)
          .withService(service.list(_))
          .withResultCreator(ResultCreator.hateoasListWrapping(hateoasFactory)((parsedRequest, _) =>
            ListPaymentsHateoasData(nino, parsedRequest.from, parsedRequest.to)))
          .withAuditing(
            AuditHandler(
              auditService,
              auditType = "listSelfAssessmentPayments",
              transactionName = "list-self-assessment-payments",
              params = Map("nino" -> nino)))
          .createRequestHandler

      requestHandler.handleRequest(rawData)
    }

}
