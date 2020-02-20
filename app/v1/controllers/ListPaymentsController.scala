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

import cats.data.EitherT
import cats.implicits._
import javax.inject.Inject
import play.api.http.MimeTypes
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.Logging
import v1.controllers.requestParsers.ListPaymentsRequestDataParser
import v1.hateoas.HateoasFactory
import v1.models.errors._
import v1.models.request.listPayments.ListPaymentsRawRequest
import v1.services.{EnrolmentsAuthService, ListPaymentsService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}

class ListPaymentsController @Inject()(val authService: EnrolmentsAuthService,
                                       val lookupService: MtdIdLookupService,
                                       requestParser: ListPaymentsRequestDataParser,
                                       service: ListPaymentsService,
                                       hateoasFactory: HateoasFactory,
                                       cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging{

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "ListPaymentsController", endpointName = "retrieveList")

  def retrieveList(nino: String, from: Option[String], to: Option[String]): Action[AnyContent] = authorisedAction(nino).async {
    implicit request =>
    val rawData = ListPaymentsRawRequest(nino, from, to)
    val result =
      for {
        parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
        response <- EitherT(service.list(parsedRequest))
      } yield {
        logger.info(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Success response received with correlationId: ${response.correlationId}"
        )

        Ok(Json.toJson(response.responseData))
          .withApiHeaders(response.correlationId)
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
      case NoPaymentsFoundError => NotFound(Json.toJson(NoPaymentsFoundError))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }
}
