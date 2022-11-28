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
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.CreateOrAmendCodingOutParser
import v1.models.request.createOrAmendCodingOut.CreateOrAmendCodingOutRawRequest
import v1.models.response.createOrAmendCodingOut.CreateOrAmendCodingOutHateoasData
import v1.models.response.createOrAmendCodingOut.CreateOrAmendCodingOutResponse.LinksFactory
import v1.services.CreateOrAmendCodingOutService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateOrAmendCodingOutController @Inject() (val authService: EnrolmentsAuthService,
                                                  val lookupService: MtdIdLookupService,
                                                  parser: CreateOrAmendCodingOutParser,
                                                  service: CreateOrAmendCodingOutService,
                                                  hateoasFactory: HateoasFactory,
                                                  auditService: AuditService,
                                                  cc: ControllerComponents,
                                                  idGenerator: IdGenerator,
                                                  requestHandlerFactory: RequestHandlerFactory)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "CreateOrAmendCodingOutController", endpointName = "CreateOrAmendCodingOut")

  def createOrAmendCodingOut(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = CreateOrAmendCodingOutRawRequest(nino, taxYear, request.body)

      val requestHandler =
        requestHandlerFactory
          .withParser(parser)
          .withService(service.amend(_))
          .withHateoasResult(hateoasFactory)((_, _) => CreateOrAmendCodingOutHateoasData(nino, taxYear))
          .withAuditing(AuditHandler(
            auditService,
            auditType = "CreateAmendCodingOutUnderpayment",
            transactionName = "create-amend-coding-out-underpayment",
            params = Map("nino" -> nino, "taxYear" -> taxYear),
            Some(request.body)
          ))
          .createRequestHandler

      requestHandler.handleRequest(rawData)
    }

}
