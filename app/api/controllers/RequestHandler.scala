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

package api.controllers

import api.controllers.requestParsers.RequestParser
import api.models.audit.{AuditHandler, AuditHandlerComponent}
import api.models.errors.{DownstreamError, ErrorWrapper}
import api.models.outcomes.ResponseWrapper
import api.models.request.RawData
import api.services.ServiceComponent
import cats.data.EitherT
import cats.implicits._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.mvc.Results.InternalServerError
import utils.Logging

import scala.annotation.nowarn
import scala.concurrent.{ExecutionContext, Future}

trait RequestHandler[InputRaw <: RawData, Input, Output] extends RequestContextImplicits {
  self: Logging
    with ServiceComponent[Input, Output]
    with ResultCreatorComponent[InputRaw, Output]
    with ErrorHandlingComponent
    with AuditHandlerComponent[InputRaw] =>

  val parser: RequestParser[InputRaw, Input]

  implicit val ec: ExecutionContext

  implicit class Response(result: Result) {

    def withApiHeaders(correlationId: String, responseHeaders: (String, String)*): Result = {

      val newHeaders: Seq[(String, String)] = responseHeaders ++ Seq(
        "X-CorrelationId"        -> correlationId,
        "X-Content-Type-Options" -> "nosniff",
        "Content-Type"           -> "application/json"
      )

      result.copy(header = result.header.copy(headers = result.header.headers ++ newHeaders))
    }

  }

  protected def unhandledError(errorWrapper: ErrorWrapper)(implicit endpointLogContext: EndpointLogContext): Result = {
    logger.error(
      s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
        s"Unhandled error: $errorWrapper")
    InternalServerError(Json.toJson(DownstreamError))
  }

  def handleRequest(rawData: InputRaw)(implicit ctx: RequestContext, request: UserRequest[_]): Future[Result] = {

    def auditIfRequired(httpStatus: Int, response: Either[ErrorWrapper, Option[JsValue]]): Unit =
      auditEventCreator.foreach { creator =>
        creator.performAudit(rawData, request.userDetails, httpStatus, response)
      }

    logger.info(
      message = s"[${ctx.endpointLogContext.controllerName}][${ctx.endpointLogContext.endpointName}] " +
        s"with correlationId : ${ctx.correlationId}")

    val result =
      for {
        parsedRequest   <- EitherT.fromEither[Future](parser.parseRequest(rawData))
        serviceResponse <- EitherT(service(parsedRequest))
      } yield {
        logger.info(
          s"[${ctx.endpointLogContext.controllerName}][${ctx.endpointLogContext.endpointName}] - " +
            s"Success response received with CorrelationId: ${serviceResponse.correlationId}")

        val resultWrapper = resultCreator
          .createResult(rawData, serviceResponse.responseData)

        auditIfRequired(resultWrapper.httpStatus, Right(resultWrapper.body))

        resultWrapper.toResult.withApiHeaders(serviceResponse.correlationId)
      }

    // FIXME move to separate method to do the mapping (and error logging) moved over from BaseController
    result.leftMap { errorWrapper =>
      val resCorrelationId = errorWrapper.correlationId
      val result           = errorResult(errorWrapper).withApiHeaders(resCorrelationId)

      logger.warn(
        s"[${ctx.endpointLogContext.controllerName}][${ctx.endpointLogContext.endpointName}] - " +
          s"Error response received with CorrelationId: $resCorrelationId")

      auditIfRequired(result.header.status, Left(errorWrapper))

      result
    }.merge
  }

  private def errorResult(errorWrapper: ErrorWrapper)(implicit endpointLogContext: EndpointLogContext): Result =
    errorResultPF
      .orElse(errorHandling.errorResultPF)
      .applyOrElse(errorWrapper, unhandledError)

  protected def errorResultPF(implicit @nowarn endpointLogContext: EndpointLogContext): PartialFunction[ErrorWrapper, Result] =
    PartialFunction.empty

}

object RequestHandler {

  def apply[InputRaw <: RawData, Input, Output](_parser: RequestParser[InputRaw, Input],
                                                _service: Input => Future[Either[ErrorWrapper, ResponseWrapper[Output]]],
                                                _errorHandling: PartialFunction[ErrorWrapper, Result],
                                                _resultsCreator: ResultCreator[InputRaw, Output],
                                                _auditEventCreator: Option[AuditHandler[InputRaw]],
                                                _commonErrorHandling: ErrorHandling)(implicit
      ec0: ExecutionContext): RequestHandler[InputRaw, Input, Output] =
    new RequestHandler[InputRaw, Input, Output]
      with ResultCreatorComponent[InputRaw, Output]
      with ServiceComponent[Input, Output]
      with ErrorHandlingComponent
      with AuditHandlerComponent[InputRaw]
      with Logging {

      override def auditEventCreator: Option[AuditHandler[InputRaw]] = _auditEventCreator

      override def resultCreator: ResultCreator[InputRaw, Output] = _resultsCreator

      override protected def errorResultPF(implicit endpointLogContext: EndpointLogContext): PartialFunction[ErrorWrapper, Result] =
        _errorHandling

      override def errorHandling: ErrorHandling = _commonErrorHandling

      override val parser: RequestParser[InputRaw, Input]                                  = _parser
      override val service: Input => Future[Either[ErrorWrapper, ResponseWrapper[Output]]] = _service

      override implicit val ec: ExecutionContext = ec0
    }

}
