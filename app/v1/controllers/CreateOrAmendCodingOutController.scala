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

import api.controllers._
import api.hateoas.HateoasFactory
import api.models.audit.AuditHandler
import api.services.{AuditService, EnrolmentsAuthService, MtdIdLookupService}
import play.api.libs.json.JsValue
import play.api.mvc.{Action, ControllerComponents}
import utils.{IdGenerator, Logging}
import v1.controllers.requestParsers.CreateOrAmendCodingOutParser
import v1.models.request.createOrAmendCodingOut.CreateOrAmendCodingOutRawRequest
import v1.models.response.createOrAmendCodingOut.CreateOrAmendCodingOutHateoasData
import v1.models.response.createOrAmendCodingOut.CreateOrAmendCodingOutResponse.LinksFactory
import v1.services.CreateOrAmendCodingOutService

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class CreateOrAmendCodingOutController @Inject() (val authService: EnrolmentsAuthService,
                                                  val lookupService: MtdIdLookupService,
                                                  parser: CreateOrAmendCodingOutParser,
                                                  service: CreateOrAmendCodingOutService,
                                                  hateoasFactory: HateoasFactory,
                                                  auditService: AuditService,
                                                  cc: ControllerComponents,
                                                  idGenerator: IdGenerator,
                                                  requestHandlerFactory: RequestHandlerFactory)(implicit ec: ExecutionContext)
    extends AuthorisedController(cc)
    with BaseController
    with Logging {

  implicit val endpointLogContext: EndpointLogContext =
    EndpointLogContext(controllerName = "CreateOrAmendCodingOutController", endpointName = "CreateOrAmendCodingOut")

  def createOrAmendCodingOut(nino: String, taxYear: String): Action[JsValue] =
    authorisedAction(nino).async(parse.json) { implicit request =>
      implicit val ctx: RequestContext = RequestContext.from(idGenerator, endpointLogContext)

      val rawData = CreateOrAmendCodingOutRawRequest(nino, taxYear, request.body)

      val requestHandler =
        requestHandlerFactory
          .withParser(parser)
          .withService(service.amend(_))
          .withHateoasResult(hateoasFactory)(_ => CreateOrAmendCodingOutHateoasData(nino, taxYear))
          .withAuditing(AuditHandler(auditService,auditType = "CreateAmendCodingOutUnderpayment", transactionName = "create-amend-coding-out-underpayment", Some(request.body)) { _ =>
            Map("nino" -> nino, "taxYear" -> taxYear)
          })
          .createRequestHandler

      requestHandler.handleRequest(rawData)

//      val result =
//        for {
//          parsedRequest   <- EitherT.fromEither[Future](parser.parseRequest(rawData))
//          serviceResponse <- EitherT(service.amend(parsedRequest))
//          vendorResponse <- EitherT.fromEither[Future](
//            hateoasFactory.wrap(serviceResponse.responseData, CreateOrAmendCodingOutHateoasData(nino, taxYear)).asRight[ErrorWrapper]
//          )
//        } yield {
//          logger.info(
//            s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
//              s"Success response received with CorrelationId: ${serviceResponse.correlationId}")
//
//          auditSubmission(
//            GenericAuditDetail(
//              userDetails = request.userDetails,
//              params = Map("nino" -> nino, "taxYear" -> taxYear),
//              requestBody = Some(request.body),
//              `X-CorrelationId` = serviceResponse.correlationId,
//              auditResponse = AuditResponse(httpStatus = OK, response = Right(Some(Json.toJson(vendorResponse))))
//            )
//          )
//
//          Ok(Json.toJson(vendorResponse))
//            .withApiHeaders(serviceResponse.correlationId)
//        }
//
//      result.leftMap { errorWrapper =>
//        val result = errorResult(errorWrapper)
//
//        auditSubmission(
//          GenericAuditDetail(
//            userDetails = request.userDetails,
//            params = Map("nino" -> nino, "taxYear" -> taxYear),
//            requestBody = Some(request.body),
//            `X-CorrelationId` = correlationId,
//            auditResponse = AuditResponse(httpStatus = result.header.status, response = Left(errorWrapper.auditErrors))
//          )
//        )
//
//        result
//      }.merge
    }

//  private def auditSubmission(details: GenericAuditDetail)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {
//    val event = AuditEvent(auditType = "CreateAmendCodingOutUnderpayment", transactionName = "create-amend-coding-out-underpayment", detail = details)
//    auditService.auditEvent(event)
//  }

}
