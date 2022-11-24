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

import api.controllers.{AuthorisedController, BaseController, EndpointLogContext, RequestHandlerFactory}
import api.hateoas.HateoasFactory
import api.models.audit.{AuditEvent, AuditResponse, GenericAuditDetail}
import api.models.errors._
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import cats.data.EitherT
import cats.implicits._
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.RetrieveTransactionDetailsRequestParser
import v1.models.request.retrieveTransactionDetails.RetrieveTransactionDetailsRawRequest
import v1.models.response.retrieveTransactionDetails.RetrieveTransactionDetailsHateoasData
import v1.models.response.retrieveTransactionDetails.RetrieveTransactionDetailsResponse._
import v1.services.RetrieveTransactionDetailsService

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveTransactionDetailsController @Inject() (val authService: EnrolmentsAuthService,
                                                      val lookupService: MtdIdLookupService,
                                                      auditService: AuditService,
                                                      requestParser: RetrieveTransactionDetailsRequestParser,
                                                      service: RetrieveTransactionDetailsService,
                                                      hateoasFactory: HateoasFactory,
                                                      cc: ControllerComponents,
                                                      idGenerator: IdGenerator,
                                                      requestHandlerFactory: RequestHandlerFactory)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

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
        parsedRequest   <- EitherT.fromEither[Future](requestParser.parseRequest(rawRequest))
        serviceResponse <- EitherT(service.retrieveTransactionDetails(parsedRequest))
        vendorResponse <- EitherT.fromEither[Future](
          hateoasFactory
            .wrap(
              serviceResponse.responseData,
              RetrieveTransactionDetailsHateoasData(nino, transactionId, serviceResponse.responseData.transactionItems.head.paymentId))
            .asRight[ErrorWrapper])
      } yield {
        logger.info(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Success response received wth CorrelationId: ${serviceResponse.correlationId}")

        auditSubmission(
          GenericAuditDetail(
            userDetails = request.userDetails,
            params = Map("nino" -> nino),
            requestBody = None,
            `X-CorrelationId` = serviceResponse.correlationId,
            auditResponse = AuditResponse(httpStatus = OK, None, None)
          )
        )

        Ok(Json.toJson(vendorResponse))
          .withApiHeaders(serviceResponse.correlationId)
          .as(MimeTypes.JSON)
      }
      result.leftMap { errorWrapper =>
        val result = errorResult(errorWrapper)

        auditSubmission(
          GenericAuditDetail(
            userDetails = request.userDetails,
            params = Map("nino" -> nino),
            requestBody = None,
            `X-CorrelationId` = errorWrapper.correlationId,
            auditResponse = AuditResponse(httpStatus = result.header.status, response = Left(errorWrapper.auditErrors))
          )
        )

        result
      }.merge
    }

  private def auditSubmission(details: GenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {

    val event = AuditEvent(
      auditType = "retrieveASelfAssessmentTransactionsDetail",
      transactionName = "retrieve-a-self-assessment-transactions-detail",
      detail = details
    )

    auditService.auditEvent(event)
  }

}
