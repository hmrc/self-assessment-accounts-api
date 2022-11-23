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

import uk.gov.hmrc.http.HeaderCarrier
import utils.IdGenerator

case class RequestContext(hc: HeaderCarrier, correlationId: String, endpointLogContext: EndpointLogContext)

object RequestContext {

  def from(idGenerator: IdGenerator, endpointLogContext: EndpointLogContext)(implicit hc: HeaderCarrier): RequestContext =
    RequestContext(hc, idGenerator.generateCorrelationId, endpointLogContext)
}

trait RequestContextImplicits {

  implicit def toHeaderCarrier(implicit ctx: RequestContext): HeaderCarrier = ctx.hc

  implicit def toCorrelationId(implicit ctx: RequestContext): String = ctx.correlationId

  implicit def toendpointLogContext(implicit ctx: RequestContext): EndpointLogContext = ctx.endpointLogContext

}
