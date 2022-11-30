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

package v2.controllers

import api.controllers._
import api.services.{EnrolmentsAuthService, MtdIdLookupService}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.{IdGenerator, Logging}
import v2.controllers.requestParsers.RetrieveBalanceAndTransactionsRequestParser
import v2.models.request.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsRawData
import v2.services.RetrieveBalanceAndTransactionsService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class RetrieveBalanceAndTransactionsController @Inject() (val authService: EnrolmentsAuthService,
                                                          val lookupService: MtdIdLookupService,
                                                          requestParser: RetrieveBalanceAndTransactionsRequestParser,
                                                          service: RetrieveBalanceAndTransactionsService,
                                                          cc: ControllerComponents,
                                                          idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrieveBalanceAndTransactionsController", endpointName = "retrieveBalanceAndTransactions")

  def retrieveBalanceAndTransactions(nino: String,
                                     docNumber: Option[String],
                                     fromDate: Option[String],
                                     toDate: Option[String],
                                     onlyOpenItems: Option[String],
                                     includeLocks: Option[String],
                                     calculateAccruedInterest: Option[String],
                                     removePOA: Option[String],
                                     customerPaymentInformation: Option[String],
                                     includeEstimatedCharges: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawRequest = RetrieveBalanceAndTransactionsRawData(
        nino,
        docNumber: Option[String],
        fromDate: Option[String],
        toDate: Option[String],
        onlyOpenItems: Option[String],
        includeLocks: Option[String],
        calculateAccruedInterest: Option[String],
        removePOA: Option[String],
        customerPaymentInformation: Option[String],
        includeEstimatedCharges: Option[String]
      )

      val requestHandler =
        RequestHandler
          .withParser(requestParser)
          .withService(service.retrieveBalanceAndTransactions(_))
          .withPlainJsonResult()

      requestHandler.handleRequest(rawRequest)
    }

}
