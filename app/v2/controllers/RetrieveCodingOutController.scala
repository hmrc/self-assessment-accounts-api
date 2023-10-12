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
import api.services.{EnrolmentsAuthService, MtdIdLookupService}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.{IdGenerator, Logging}
import v2.controllers.requestParsers.RetrieveCodingOutRequestParser
import v2.models.request.retrieveCodingOut.RetrieveCodingOutRawRequest
import v2.models.response.retrieveCodingOut.RetrieveCodingOutHateoasData
import v2.services.RetrieveCodingOutService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RetrieveCodingOutController @Inject() (val authService: EnrolmentsAuthService,
                                             val lookupService: MtdIdLookupService,
                                             requestParser: RetrieveCodingOutRequestParser,
                                             service: RetrieveCodingOutService,
                                             hateoasFactory: HateoasFactory,
                                             cc: ControllerComponents,
                                             idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveCodingOutController",
      endpointName = "retrieveCodingOut"
    )

  def retrieveCodingOut(nino: String, taxYear: String, source: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData: RetrieveCodingOutRawRequest = RetrieveCodingOutRawRequest(
        nino = nino,
        taxYear = taxYear,
        source = source
      )

      val requestHandler =
        RequestHandlerOld
          .withParser(requestParser)
          .withService(service.retrieveCodingOut)
          .withHateoasResult(hateoasFactory)(RetrieveCodingOutHateoasData(nino, taxYear))

      requestHandler.handleRequest(rawData)

    }

}