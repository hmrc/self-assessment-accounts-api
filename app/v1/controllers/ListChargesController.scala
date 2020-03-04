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
import javax.inject.{Inject, Singleton}
import play.api.http.MimeTypes
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.Logging
import v1.controllers.requestParsers.ListChargesRequestDataParser
import v1.hateoas.HateoasFactory
import v1.models.errors._
import v1.models.request.listCharges.ListChargesRawRequest
import v1.models.response.listCharges.ListChargesHateoasData
import v1.services.{EnrolmentsAuthService, ListChargesService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListChargesController @Inject()(val authService: EnrolmentsAuthService,
                                      val lookupService: MtdIdLookupService,
                                      requestParser: ListChargesRequestDataParser,
                                      service: ListChargesService,
                                      hateoasFactory: HateoasFactory,
                                      cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging{

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "ListChargesController", endpointName = "listCharges")

  def retrieveList(nino: String, from: Option[String], to: Option[String]): Action[AnyContent] = authorisedAction(nino).async {
    implicit request =>
    val rawData = ListChargesRawRequest(nino, from, to)
    val result =
      for {
        parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
        serviceResponse <- EitherT(service.list(parsedRequest))
        vendorResponse <- EitherT.fromEither[Future](
          hateoasFactory
            .wrapList(serviceResponse.responseData, ListChargesHateoasData(nino))
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
      case NoChargesFoundError => NotFound(Json.toJson(NoChargesFoundError))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }
}
