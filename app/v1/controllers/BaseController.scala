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

import java.util.UUID

import org.json4s.{CustomSerializer, DefaultFormats, Formats, JString, JsonAST}
import org.json4s.JsonAST.{JDecimal, JObject}
import org.json4s.native.Serialization
import play.api.libs.json.Json
import play.api.mvc.Result
import utils.Logging
import v1.models.errors.ErrorWrapper
import v1.models.hateoas.{HateoasWrapper, Method}

trait BaseController {
  self: Logging =>

  private object BigDecimalSerializer extends CustomSerializer[BigDecimal](_ =>
    ({
      case jde: JDecimal => jde.num
    },
      {
        case bd: BigDecimal => JDecimal(bd.setScale(2, BigDecimal.RoundingMode.HALF_UP))
      })
  )

  private object MethodSerializer extends CustomSerializer[String](_ =>
    ({
      case js: JString => js.s
    },
      {
        case method: Method => JString(method.toString)
      })
  )

  private object ThingSerializer extends CustomSerializer[List[(String, JsonAST.JValue)]](_ =>
    ({
      case jo: JObject => jo.obj
    },
      {
        case hw: HateoasWrapper[a] => JObject(HateoasWrapper.writes.writes(hw))
      })
  )

  def toSerializedString[A](a: A): String = {
    implicit val formats: Formats = DefaultFormats ++ Seq(BigDecimalSerializer) ++ Seq(MethodSerializer)
    Serialization.write(a)
  }

  implicit class Response(result: Result) {

    def withApiHeaders(correlationId: String, responseHeaders: (String, String)*): Result = {

      val newHeaders: Seq[(String, String)] = responseHeaders ++ Seq(
        "X-CorrelationId" -> correlationId,
        "X-Content-Type-Options" -> "nosniff",
        "Content-Type" -> "application/json"
      )

      result.copy(header = result.header.copy(headers = result.header.headers ++ newHeaders))
    }
  }

  protected def getCorrelationId(errorWrapper: ErrorWrapper)(implicit endpointLogContext: EndpointLogContext): String = {
    errorWrapper.correlationId match {
      case Some(correlationId) =>
        logger.info(
          s"[${endpointLogContext.controllerName}][getCorrelationId] - " +
            s"Error received from DES ${Json.toJson(errorWrapper)} with CorrelationId: $correlationId")
        correlationId
      case None =>
        val correlationId = UUID.randomUUID().toString
        logger.info(
          s"[${endpointLogContext.controllerName}][getCorrelationId] - " +
            s"Validation error: ${Json.toJson(errorWrapper)} with CorrelationId: $correlationId")
        correlationId
    }
  }
}


