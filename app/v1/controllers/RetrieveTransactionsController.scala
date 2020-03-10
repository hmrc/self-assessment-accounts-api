/*
 * Copyright 2020 HM Revenue & Customs
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

import cats.implicits._
import cats.data.EitherT
import javax.inject.{Inject, Singleton}
import play.api.http.MimeTypes
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.Logging
import v1.controllers.requestParsers.RetrieveTransactionsRequestParser
import v1.hateoas.HateoasFactory
import v1.models.errors._
import v1.models.request.retrieveTransactions.RetrieveTransactionsRawRequest
import v1.models.response.retrieveTransaction.RetrieveTransactionsHateoasData
import v1.services.{EnrolmentsAuthService, MtdIdLookupService, RetrieveTransactionsService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveTransactionsController @Inject()(val authService: EnrolmentsAuthService,
                                               val lookupService: MtdIdLookupService,
                                               requestParser: RetrieveTransactionsRequestParser,
                                               service: RetrieveTransactionsService,
                                               hateoasFactory: HateoasFactory,
                                               cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveTransactionsController",
      endpointName = "retrieveTransactions"
    )

  def retrieveTransactions(nino: String, from: Option[String], to: Option[String]): Action[AnyContent] = authorisedAction(nino).async{
    implicit request =>
      val rawRequest: RetrieveTransactionsRawRequest = RetrieveTransactionsRawRequest (nino, from, to)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawRequest))
          serviceResponse <- EitherT(service.retrieveTransactions(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory
              .wrapList(serviceResponse.responseData, RetrieveTransactionsHateoasData(nino))
              .asRight[ErrorWrapper]
          )
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with correlationId: ${serviceResponse.correlationId}"
          )

          Ok(Json.toJson(vendorResponse))
            .withApiHeaders(serviceResponse.correlationId)
            .as(MimeTypes.JSON)
        }
      result.leftMap { errorWrapper =>
        val correlationId = getCorrelationId(errorWrapper)
        val result = errorResult(errorWrapper).withApiHeaders(correlationId)

        result
      }.merge
  }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case BadRequestError | NinoFormatError |
           FromDateFormatError | MissingFromDateError |
           ToDateFormatError | MissingToDateError |
           RangeToDateBeforeFromDateError | RuleFromDateNotSupportedError |
           RuleDateRangeInvalidError => BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case NoTransactionsFoundError => NotFound(Json.toJson(NoTransactionsFoundError))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }

}