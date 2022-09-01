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

import cats.data.EitherT
import cats.implicits.catsSyntaxEitherId
import api.controllers.{AuthorisedController, BaseController, EndpointLogContext}
import api.hateoas.HateoasFactory
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import play.mvc.Http.MimeTypes
import uk.gov.hmrc.http.HeaderCarrier
import utils.{IdGenerator, Logging}
import v1.connectors.RetrieveChargeHistoryConnector
import v1.support.DesResponseMappingSupport
import v2.controllers.requestParsers.RetrieveSelfAssessmentChargeHistoryRequestParser
import v2.models.request.retrieveSelfAssessmentChargeHistory.{RetrieveSelfAssessmentChargeHistoryRawData, RetrieveSelfAssessmentChargeHistoryRequest}
import v2.models.response.retrieveSelfAssessmentChargeHistory.RetrieveSelfAssessmentChargeHistoryResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveSelfAssessmentChargeHistoryController @Inject() (val authService: EnrolmentsAuthService,
                                                               val lookupService: MtdIdLookupService,
                                                               auditService: AuditService,
                                                               requestParser: RetrieveSelfAssessmentChargeHistoryRequestParser,
                                                               service: RetrieveSelfAssessmentChargeHistoryService,
                                                               hateoasFactory: HateoasFactory,
                                                               cc: ControllerComponents,
                                                               val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "RetrieveSelfAssessmentChargeHistoryController", endpointName = "retrieveChargeHistory")

  def retrieveChargeHistory(nino: String, transactionId: String): Action[AnyContent] =
    authorisedAction(nino).async { implicit request =>
      {
        implicit val correlationId: String = idGenerator.generateCorrelationId
        logger.info(s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] " + s"with correlationId: $correlationId")

        val rawRequest = RetrieveSelfAssessmentChargeHistoryRawData(nino, transactionId)
        val result = for {
          parsedRequest   <- EitherT.fromEither[Future](requestParser.parseRequest(rawRequest))
          serviceResponse <- EitherT(service.retrieveChargeHistory(parsedRequest))
          vendorResponse  <- EitherT.fromEither[Future](serviceResponse.responseData.asRight[ErrorWrapper])
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
          val resCorrelationId = errorWrapper.correlationId
          val result           = errorResult(errorWrapper).withApiHeaders(resCorrelationId)
          logger.warn(
            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
              s"Error response received with CorrelationId: $resCorrelationId")
          result
        }.merge
      }

    }

  private def errorResult(errorWrapper: ErrorWrapper) = {
    errorWrapper.error match {
      case BadRequestError | NinoFormatError | TransactionIdFormatError => BadRequest(Json.toJson(errorWrapper))
      case NotFoundError                                                => NotFound(Json.toJson(errorWrapper))
      case DownstreamError                                              => InternalServerError(Json.toJson(errorWrapper))
      case _                                                            => unhandledError(errorWrapper)
    }
  }

}

//-----------STUBS-----------------------------

@Singleton
class RetrieveSelfAssessmentChargeHistoryService @Inject() (connector: RetrieveChargeHistoryConnector)
    extends DesResponseMappingSupport
    with Logging {

  def retrieveChargeHistory(request: RetrieveSelfAssessmentChargeHistoryRequest)(implicit
      hc: HeaderCarrier,
      ec: ExecutionContext,
      logContext: EndpointLogContext,
      correlationId: String): Future[Either[ErrorWrapper, ResponseWrapper[RetrieveSelfAssessmentChargeHistoryResponse]]] = {

    ???
  }

}
