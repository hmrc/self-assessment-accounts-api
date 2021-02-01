/*
 * Copyright 2021 HM Revenue & Customs
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
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.RetrieveTransactionDetailsRequestParser
import v1.hateoas.HateoasFactory
import v1.models.audit.{AuditDetail, AuditEvent, AuditResponse}
import v1.models.errors._
import v1.models.request.retrieveTransactionDetails.RetrieveTransactionDetailsRawRequest
import v1.models.response.retrieveTransactionDetails.RetrieveTransactionDetailsHateoasData
import v1.models.response.retrieveTransactionDetails.RetrieveTransactionDetailsResponse._
import v1.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService, RetrieveTransactionDetailsService}

import scala.concurrent.{ExecutionContext, Future}
@Singleton
class RetrieveTransactionDetailsController @Inject()(val authService: EnrolmentsAuthService,
                                                     val lookupService: MtdIdLookupService,
                                                     auditService: AuditService,
                                                     requestParser: RetrieveTransactionDetailsRequestParser,
                                                     service: RetrieveTransactionDetailsService,
                                                     hateoasFactory: HateoasFactory,
                                                     cc: ControllerComponents,
                                                     val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "RetrieveTransactionDetailsController",
      endpointName = "retrieveTransactionDetails"
    )

  def retrieveTransactionDetails(nino: String, transactionId: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>

      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.info(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with CorrelationId: $correlationId")

      val rawRequest = RetrieveTransactionDetailsRawRequest(nino, transactionId)
      val result = for {
          parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawRequest))
          serviceResponse <- EitherT(service.retrieveTransactionDetails(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory
              .wrap(serviceResponse.responseData,
                RetrieveTransactionDetailsHateoasData(nino,transactionId,serviceResponse.responseData.transactionItems.head.paymentId))
              .asRight[ErrorWrapper])
        } yield {
          logger.info(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received wth CorrelationId: ${serviceResponse.correlationId}")

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
        val resCorrelationId = errorWrapper.correlationId
        val result = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
        logger.warn(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: $resCorrelationId")

        auditSubmission(
          AuditDetail(
            userDetails = request.userDetails,
            nino = nino,
            `X-CorrelationId` = resCorrelationId,
            response = AuditResponse(httpStatus = result.header.status, response = Left(errorWrapper.auditErrors))
          )
        )

        result
      }.merge
    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    (errorWrapper.error: @unchecked) match {
      case BadRequestError | NinoFormatError | TransactionIdFormatError => BadRequest(Json.toJson(errorWrapper))
      case NotFoundError => NotFound(Json.toJson(errorWrapper))
      case NoTransactionDetailsFoundError => NotFound(Json.toJson(NoTransactionDetailsFoundError))
      case DownstreamError => InternalServerError(Json.toJson(errorWrapper))
    }
  }

  private def auditSubmission(details: AuditDetail)
                             (implicit hc: HeaderCarrier,
                              ec: ExecutionContext): Future[AuditResult] = {

    val event = AuditEvent(
      auditType = "retrieveASelfAssessmentTransactionsDetail",
      transactionName = "retrieve-a-self-assessment-transactions-detail",
      detail = details
    )

    auditService.auditEvent(event)
  }
}
