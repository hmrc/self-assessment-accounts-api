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

package v4.retrieveChargeHistoryByChargeReference

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import shared.config.SharedAppConfig
import shared.controllers._
import shared.routing.Version
import shared.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import shared.utils.IdGenerator

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RetrieveChargeHistoryByChargeReferenceController @Inject() (val authService: EnrolmentsAuthService,
                                                                  val lookupService: MtdIdLookupService,
                                                                  validatorFactory: RetrieveChargeHistoryByChargeReferenceValidatorFactory,
                                                                  service: RetrieveChargeHistoryByChargeReferenceService,
                                                                  auditService: AuditService,
                                                                  cc: ControllerComponents,
                                                                  idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: SharedAppConfig)
    extends AuthorisedController(cc) {

  val endpointName: String = "retrieve-charge-history-by-charge-reference"

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrieveChargeHistoryByChargeReferenceController", endpointName = "retrieveChargeHistoryByChargeReference")

  def retrieveChargeHistoryByChargeReference(nino: String, chargeReference: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, chargeReference)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.retrieveChargeHistoryByChargeReference)
          .withPlainJsonResult()
          .withAuditing(AuditHandler(
            auditService,
            auditType = "RetrieveAChargeHistoryByChargeReference",
            transactionName = "retrieve-a-charge-history-by-charge-reference",
            apiVersion = Version(request),
            params = Map("nino" -> nino, "chargeReference" -> chargeReference),
            includeResponse = true
          ))

      requestHandler.handleRequest()
    }

}
