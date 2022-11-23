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

import api.models.errors.ErrorWrapper
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results.Status
import utils.Logging

trait BaseController  { self: Logging =>

  implicit def toEndpointLogContext(implicit ctx: RequestContext): EndpointLogContext = ctx.endpointLogContext

  protected def errorResult(errorWrapper: ErrorWrapper)(implicit endpointLogContext: EndpointLogContext): Result = {
    val resCorrelationId = errorWrapper.correlationId
    logger.warn(
      s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
        s"Error response received with CorrelationId: $resCorrelationId")

    Status(errorWrapper.error.httpStatus)(Json.toJson(errorWrapper))
      .withApiHeaders(resCorrelationId)
  }

  implicit class Response(result: Result) {

    def withApiHeaders(correlationId: String, responseHeaders: (String, String)*): Result = {

      val newHeaders: Seq[(String, String)] = responseHeaders ++ Seq(
        "X-CorrelationId"        -> correlationId,
        "X-Content-Type-Options" -> "nosniff"
      )

      result.copy(header = result.header.copy(headers = result.header.headers ++ newHeaders))
    }

  }

}
