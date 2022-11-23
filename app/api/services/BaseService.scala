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

package api.services

import api.controllers.{EndpointLogContext, RequestContext}
import api.models.errors.ErrorWrapper
import api.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.Future

trait ServiceComponent[Input, Output] {
  def service: Input => Future[Either[ErrorWrapper, ResponseWrapper[Output]]]
}

trait BaseService {

  implicit def toHeaderCarrier(implicit ctx: RequestContext): HeaderCarrier = ctx.hc

  implicit def toCorrelationId(implicit ctx: RequestContext): String = ctx.correlationId

  implicit def toEndpointLogContext(implicit ctx: RequestContext): EndpointLogContext = ctx.endpointLogContext
}