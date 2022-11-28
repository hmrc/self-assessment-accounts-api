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

import scala.concurrent.{ExecutionContext, Future}

trait RequestHandler[InputRaw <: RawData, Input, Output] extends RequestContextImplicits {
  self: Logging
    with ServiceComponent[Input, Output]
    with ResultCreatorComponent[InputRaw, Input, Output]
    with ErrorHandlingComponent
    with AuditHandlerComponent =>

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

  def handleRequest(rawData: InputRaw)(implicit ctx: RequestContext, request: UserRequest[_]): Future[Result] = {

    def auditIfRequired(httpStatus: Int, response: Either[ErrorWrapper, Option[JsValue]]): Unit =
      auditHandler.foreach { creator =>
        creator.performAudit(request.userDetails, httpStatus, response)
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
          .createResult(rawData, parsedRequest, serviceResponse.responseData)

        val result = resultWrapper.asResult.withApiHeaders(serviceResponse.correlationId)

        auditIfRequired(result.header.status, Right(resultWrapper.body))

        result
      }

    result.leftMap { errorWrapper =>
      val result = errorResult(errorWrapper)

      auditIfRequired(result.header.status, Left(errorWrapper))

      result
    }.merge
  }

  private def errorResult(errorWrapper: ErrorWrapper)(implicit endpointLogContext: EndpointLogContext): Result = {
    val resCorrelationId = errorWrapper.correlationId

    logger.warn(
      s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
        s"Error response received with CorrelationId: $resCorrelationId")

    val errorResult = errorHandling.errorResultPF
      .applyOrElse(errorWrapper, unhandledError)

    errorResult.withApiHeaders(resCorrelationId)
  }

  private def unhandledError(errorWrapper: ErrorWrapper)(implicit endpointLogContext: EndpointLogContext): Result = {
    logger.error(
      s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
        s"Unhandled error: $errorWrapper")
    InternalServerError(Json.toJson(DownstreamError))
  }

}

object RequestHandler {

  private[controllers] class Impl[InputRaw <: RawData, Input, Output](val parser: RequestParser[InputRaw, Input],
                                                                      val service: Input => Future[Either[ErrorWrapper, ResponseWrapper[Output]]],
                                                                      val errorHandling: ErrorHandling,
                                                                      val resultCreator: ResultCreator[InputRaw, Input, Output],
                                                                      val auditHandler: Option[AuditHandler])(implicit
      val ec: ExecutionContext)
      extends RequestHandler[InputRaw, Input, Output]
      with ResultCreatorComponent[InputRaw, Input, Output]
      with ServiceComponent[Input, Output]
      with ErrorHandlingComponent
      with AuditHandlerComponent
      with Logging

}
