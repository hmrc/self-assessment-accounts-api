/*
 * Copyright 2026 HM Revenue & Customs
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

package v4.retrieveItsaPenalties

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import shared.config.SharedAppConfig
import shared.controllers.*
import shared.routing.Version
import shared.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RetrieveItsaPenaltiesController @Inject() (val authService: EnrolmentsAuthService,
                                                 val lookupService: MtdIdLookupService,
                                                 validatorFactory: RetrieveItsaPenaltiesValidatorFactory,
                                                 service: RetrieveItsaPenaltiesService,
                                                 auditService: AuditService,
                                                 cc: ControllerComponents,
                                                 idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: SharedAppConfig)
    extends AuthorisedController(cc) {

  val endpointName: String = "retrieve-itsa-penalties"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrieveItsaPenaltiesController", endpointName = "retrieveItsaPenalties")

  def retrieveItsaPenalties(nino: String): Action[AnyContent] = {
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)
      val validator                    = validatorFactory.validator(nino)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.retrieveItsaPenalties)
          .withPlainJsonResult()
          .withAuditing(AuditHandler(
            auditService,
            auditType = "RetrieveItsaPenalties",
            transactionName = "retrieve-itsa-penalties",
            apiVersion = Version(request),
            params = Map("nino" -> nino),
            includeResponse = true
          ))
      requestHandler.handleRequest()
    }
  }

}
