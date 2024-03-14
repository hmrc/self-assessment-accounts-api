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

package v3.controllers

import api.controllers._
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import routing.{Version, Version3}
import utils.IdGenerator
import v3.controllers.validators.RetrieveCodingOutStatusValidatorFactory
import v3.services.RetrieveCodingOutStatusService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RetrieveCodingOutStatusController @Inject() (val authService: EnrolmentsAuthService,
                                                   val lookupService: MtdIdLookupService,
                                                   validatorFactory: RetrieveCodingOutStatusValidatorFactory,
                                                   service: RetrieveCodingOutStatusService,
                                                   auditService: AuditService,
                                                   cc: ControllerComponents,
                                                   idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrieveCodingOutStatusController", endpointName = "retrieveCodingOutStatus")

  def retrieveCodingOutStatus(nino: String, taxYear: String): Action[AnyContent] = {
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, taxYear)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.retrieveCodingOutStatus)
          .withPlainJsonResult()
          .withAuditing(
            AuditHandler(
              auditService,
              auditType = "RetrieveCodingOutStatus",
              transactionName = "retrieve-coding-out-status",
              apiVersion = Version.from(request, orElse = Version3),
              params = Map("nino" -> nino, "taxYear" -> taxYear),
              includeResponse = true
            ))
      requestHandler.handleRequest()
    }
  }
}
