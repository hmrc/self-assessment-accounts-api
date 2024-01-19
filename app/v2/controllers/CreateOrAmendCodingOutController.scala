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
import api.hateoas.HateoasFactory
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import config.{AppConfig, FeatureSwitches}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import routing.{Version, Version2}
import utils.{IdGenerator, Logging}
import v2.controllers.validators.CreateOrAmendCodingOutValidatorFactory
import v2.models.response.createOrAmendCodingOut.CreateOrAmendCodingOutHateoasData
import v2.models.response.createOrAmendCodingOut.CreateOrAmendCodingOutResponse.LinksFactory
import v2.services.CreateOrAmendCodingOutService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateOrAmendCodingOutController @Inject() (val authService: EnrolmentsAuthService,
                                                  val lookupService: MtdIdLookupService,
                                                  appConfig: AppConfig,
                                                  validatorFactory: CreateOrAmendCodingOutValidatorFactory,
                                                  service: CreateOrAmendCodingOutService,
                                                  hateoasFactory: HateoasFactory,
                                                  auditService: AuditService,
                                                  cc: ControllerComponents,
                                                  idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "CreateOrAmendCodingOutController", endpointName = "CreateOrAmendCodingOut")

  def createOrAmendCodingOut(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val apiVersion: Version = Version.from(request, orElse = Version2)
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator =
        validatorFactory.validator(nino, taxYear, request.body, temporalValidationEnabled = FeatureSwitches(appConfig).isTemporalValidationEnabled)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.amend)
          .withHateoasResult(hateoasFactory)(CreateOrAmendCodingOutHateoasData(nino, taxYear))
          .withAuditing(AuditHandler(
            auditService,
            auditType = "CreateAmendCodingOutUnderpayment",
            transactionName = "create-amend-coding-out-underpayment",
            apiVersion = apiVersion,
            params = Map("nino" -> nino, "taxYear" -> taxYear),
            requestBody = Some(request.body),
            includeResponse = true
          ))

      requestHandler.handleRequest()
    }

}
