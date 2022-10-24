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

import api.controllers.{AuthorisedController, BaseController, EndpointLogContext}
import api.models.errors._
import api.services.{EnrolmentsAuthService, MtdIdLookupService}
import cats.data.EitherT
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import utils.{IdGenerator, Logging}
import v2.controllers.requestParsers.ListPaymentsAndAllocationDetailsRequestParser
import v2.models.request.listPaymentsAndAllocationDetails.ListPaymentsAndAllocationDetailsRawData
import v2.services.ListPaymentsAndAllocationDetailsService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ListPaymentsAndAllocationDetailsController @Inject() (val authService: EnrolmentsAuthService,
                                                            val lookupService: MtdIdLookupService,
                                                            requestParser: ListPaymentsAndAllocationDetailsRequestParser,
                                                            service: ListPaymentsAndAllocationDetailsService,
                                                            cc: ControllerComponents,
                                                            val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "ListPaymentsAndAllocationDetailsController", endpointName = "listPaymentsAndAllocationDetails")

  def listPayments(nino: String,
                   fromDate: Option[String],
                   toDate: Option[String],
                   paymentLot: Option[String],
                   paymentLotItem: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with CorrelationId: $correlationId")

      val rawData = ListPaymentsAndAllocationDetailsRawData(
        nino = nino,
        fromDate = fromDate,
        toDate = toDate,
        paymentLot = paymentLot,
        paymentLotItem = paymentLotItem
      )

      val result =
        for {
          parsedRequest   <- EitherT.fromEither[Future](requestParser.parseRequest(rawData))
          serviceResponse <- EitherT(service.listPaymentsAndAllocationDetails(parsedRequest))
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with correlationId: ${serviceResponse.correlationId}"
          )

          Ok(Json.toJson(serviceResponse.responseData))
            .withApiHeaders(serviceResponse.correlationId)
        }

      result.leftMap { errorWrapper =>
        val resCorrelationId = errorWrapper.correlationId
        val result           = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    errorWrapper.error match {
      case err
          if errorWrapper.containsAnyOf(
            BadRequestError,
            NinoFormatError,
            V2_FromDateFormatError,
            V2_ToDateFormatError,
            V2_MissingFromDateError,
            V2_MissingToDateError,
            V2_RangeToDateBeforeFromDateError,
            RuleInvalidDateRangeError,
            PaymentLotFormatError,
            PaymentLotItemFormatError,
            MissingPaymentLotError
          ) =>
        BadRequest(Json.toJson(errorWrapper))
      case NotFoundError   => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
      case _               => unhandledError(errorWrapper)
    }
  }

}
