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

package v2.controllers

import api.controllers._
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.{IdGenerator, Logging}
import v2.controllers.requestParsers.DeleteCodingOutParser
import v2.models.request.deleteCodingOut.DeleteCodingOutRawRequest
import v2.services.DeleteCodingOutService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class DeleteCodingOutController @Inject() (val authService: EnrolmentsAuthService,
                                           val lookupService: MtdIdLookupService,
                                           parser: DeleteCodingOutParser,
                                           service: DeleteCodingOutService,
                                           auditService: AuditService,
                                           cc: ControllerComponents,
                                           idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "DeleteCodingOutController", endpointName = "deleteCodingOut")

  def handleRequest(nino: String, taxYear: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = DeleteCodingOutRawRequest(nino, taxYear)

      val requestHandler =
        RequestHandlerOld
          .withParser(parser)
          .withService(service.deleteCodingOut)
          .withNoContentResult()
          .withAuditing(
            AuditHandlerOld(
              auditService,
              auditType = "DeleteCodingOutUnderpayments",
              transactionName = "delete-coding-out-underpayments",
              params = Map("nino" -> nino, "taxYear" -> taxYear)))

      requestHandler.handleRequest(rawData)
    }

}