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
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.ListTransactionsRequestParser
import v1.hateoas.HateoasFactory
import v1.models.errors._
import v1.models.request.listTransactions.ListTransactionsRawRequest
import v1.models.response.listTransaction.ListTransactionsHateoasData
import v1.services.{AuditService, EnrolmentsAuthService, ListTransactionsService, MtdIdLookupService}

import scala.concurrent.{ExecutionContext, Future}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import v1.models.audit.{AuditDetail, AuditEvent, AuditResponse}

@Singleton
class ListTransactionsController @Inject()(val authService: EnrolmentsAuthService,
                                           val lookupService: MtdIdLookupService,
                                           requestParser: ListTransactionsRequestParser,
                                           service: ListTransactionsService,
                                           hateoasFactory: HateoasFactory,
                                           auditService: AuditService,
                                           cc: ControllerComponents,
                                           val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends AuthorisedController(cc) with BaseController with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(
      controllerName = "ListTransactionsController",
      endpointName = "listTransactions"
    )

  //noinspection ScalaStyle
  def listTransactions(nino: String, from: Option[String], to: Option[String]): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>

      implicit val correlationId: String = idGenerator.generateCorrelationId
      logger.warn(
        s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " +
          s"with CorrelationId: $correlationId")

      val rawRequest: ListTransactionsRawRequest = ListTransactionsRawRequest(nino, from, to)
      val result =
        for {
          parsedRequest <- EitherT.fromEither[Future](requestParser.parseRequest(rawRequest))
          serviceResponse <- EitherT(service.listTransactions(parsedRequest))
          vendorResponse <- EitherT.fromEither[Future](
            hateoasFactory
              .wrapList(serviceResponse.responseData, ListTransactionsHateoasData(nino, parsedRequest.from, parsedRequest.to))
              .asRight[ErrorWrapper]
          )
        } yield {
          logger.warn(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Success response received with correlationId: ${serviceResponse.correlationId}"
          )
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

  private def auditSubmission(details: AuditDetail)
                             (implicit hc: HeaderCarrier,
                              ec: ExecutionContext): Future[AuditResult] = {

    val event = AuditEvent(
      auditType = "listSelfAssessmentTransactions",
      transactionName = "list-self-assessment-transactions",
      detail = details
    )

    auditService.auditEvent(event)
  }
}
