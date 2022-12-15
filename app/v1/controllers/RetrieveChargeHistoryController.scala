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
import v1.controllers.requestParsers.RetrieveChargeHistoryRequestParser
import v1.models.request.retrieveChargeHistory.RetrieveChargeHistoryRawRequest
import v1.models.response.retrieveChargeHistory.RetrieveChargeHistoryHateoasData
import v1.services.RetrieveChargeHistoryService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RetrieveChargeHistoryController @Inject() (val authService: EnrolmentsAuthService,
                                                 val lookupService: MtdIdLookupService,
                                                 auditService: AuditService,
                                                 requestParser: RetrieveChargeHistoryRequestParser,
                                                 service: RetrieveChargeHistoryService,
                                                 hateoasFactory: HateoasFactory,
                                                 cc: ControllerComponents,
                                                 idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveChargeHistoryController",
      endpointName = "retrieveChargeHistory"
    )

  def retrieveChargeHistory(nino: String, transactionId: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawRequest = RetrieveChargeHistoryRawRequest(nino, transactionId)

      val requestHandler =
        RequestHandler
          .withParser(requestParser)
          .withService(service.retrieveChargeHistory)
          .withHateoasResult(hateoasFactory)(RetrieveChargeHistoryHateoasData(nino, transactionId))
          .withAuditing(
            AuditHandler(
              auditService,
              auditType = "retrieveASelfAssessmentChargesHistory",
              transactionName = "retrieve-a-self-assessment-charges-history",
              params = Map("nino" -> nino)))

      requestHandler.handleRequest(rawRequest)
    }

}
