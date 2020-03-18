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
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.Logging
import v1.controllers.requestParsers.RetrieveAllocationsRequestParser
import v1.hateoas.HateoasFactory
import v1.models.audit.{AuditDetail, AuditEvent, AuditResponse}
import v1.models.errors._
import v1.models.request.retrieveAllocations.RetrieveAllocationsRawRequest
import v1.models.response.retrieveAllocations.RetrieveAllocationsHateoasData
import v1.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService, RetrieveAllocationsService}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveAllocationsController @Inject()(val authService: EnrolmentsAuthService,
                                              val lookupService: MtdIdLookupService,
                                              auditService: AuditService,
                                              requestParser: RetrieveAllocationsRequestParser,
                                              service: RetrieveAllocationsService,
                                              hateoasFactory: HateoasFactory,
                                              cc: ControllerComponents)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveAllocationsController",
      endpointName = "retrieveAllocations"
    )

  def retrieveAllocations(nino: String, paymentId: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>

      val rawRequest = RetrieveAllocationsRawRequest(nino, paymentId)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawRequest))
          serviceResponse <- EitherT(service.retrieveAllocations(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory.wrap(serviceResponse.responseData, RetrieveAllocationsHateoasData(nino, paymentId)).asRight[ErrorWrapper])
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

          auditSubmission(
            AuditDetail(
              userDetails = request.userDetails,
              nino = nino,
              `X-CorrelationId` = serviceResponse.correlationId,
              response = AuditResponse(httpStatus = OK, None, None))
          )

          Ok(Json.toJson(vendorResponse))
            .withApiHeaders(serviceResponse.correlationId)
            .as(MimeTypes.JSON)
        }

      result.leftMap { errorWrapper =>
        val correlationId = getCorrelationId(errorWrapper)
        val result = errorResult(errorWrapper).withApiHeaders(correlationId)

        auditSubmission(
          AuditDetail(
            userDetails = request.userDetails,
            nino = nino,
            `X-CorrelationId` = correlationId,
            response = AuditResponse(httpStatus = result.header.status, response = Left(errorWrapper.auditErrors))
          )
        )

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case BadRequestError | NinoFormatError | PaymentIdFormatError => BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }

  private def auditSubmission(details: AuditDetail)
                             (implicit hc: HeaderCarrier,
                              ec: ExecutionContext): Future[AuditResult] = {

    val event = AuditEvent(
      auditType = "retrieveASelfAssessmentPaymentsAllocationDetails",
      transactionName = "retrieve-a-self-assessment-payments-allocation-details",
      detail = details
    )

    auditService.auditEvent(event)
  }
}
