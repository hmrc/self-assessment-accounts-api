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
import api.services.{EnrolmentsAuthService, MtdIdLookupService}
import config.AppConfig
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.IdGenerator
import v3.controllers.validators.RetrieveChargeHistoryByTransactionIdValidatorFactory
import v3.models.response.retrieveChargeHistory.RetrieveChargeHistoryResponse.RetrieveChargeHistoryHateoasData
import v3.services.RetrieveChargeHistoryByTransactionIdService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RetrieveChargeHistoryByTransactionIdController @Inject() (val authService: EnrolmentsAuthService,
                                                                val lookupService: MtdIdLookupService,
                                                                validatorFactory: RetrieveChargeHistoryByTransactionIdValidatorFactory,
                                                                service: RetrieveChargeHistoryByTransactionIdService,
                                                                hateoasFactory: HateoasFactory,
                                                                cc: ControllerComponents,
                                                                idGenerator: IdGenerator)(implicit ec: ExecutionContext, appConfig: AppConfig)
    extends AuthorisedController(cc) {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrieveChargeHistoryByTransactionIdController", endpointName = "retrieveChargeHistoryByTransactionId")

  def retrieveChargeHistoryByTransactionId(nino: String, transactionId: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val validator = validatorFactory.validator(nino, transactionId)

      val requestHandler =
        RequestHandler
          .withValidator(validator)
          .withService(service.retrieveChargeHistoryByTransactionId)
          .withHateoasResult(hateoasFactory)(RetrieveChargeHistoryHateoasData(nino, transactionId))

      requestHandler.handleRequest()
    }

}
