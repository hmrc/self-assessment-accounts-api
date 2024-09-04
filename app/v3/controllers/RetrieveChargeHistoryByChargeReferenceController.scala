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
import api.hateoas.HateoasFactory
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import config.AppConfig
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import routing.Version
import utils.IdGenerator
import v3.controllers.validators.RetrieveChargeHistoryByChargeReferenceValidatorFactory
import v3.models.response.retrieveChargeHistory.RetrieveChargeHistoryResponse.RetrieveChargeHistoryHateoasData
import v3.services.RetrieveChargeHistoryByChargeReferenceService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RetrieveChargeHistoryByChargeReferenceController @Inject() (val authService: EnrolmentsAuthService,
                                                                  val lookupService: MtdIdLookupService,
                                                                  validatorFactory: RetrieveChargeHistoryByChargeReferenceValidatorFactory,
                                                                  service: RetrieveChargeHistoryByChargeReferenceService,
                                                                  hateoasFactory: HateoasFactory,
                                                                  auditService: AuditService,
                                                                  cc: ControllerComponents,
                                                                  idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends AuthorisedController(cc) {

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
          .withHateoasResult(hateoasFactory)(RetrieveChargeHistoryHateoasData(nino, chargeReference))
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
