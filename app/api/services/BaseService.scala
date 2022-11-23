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

import api.controllers.{RequestContext, RequestContextImplicits}
import api.models.errors.ErrorWrapper
import api.models.outcomes.ResponseWrapper

import scala.concurrent.{ExecutionContext, Future}

trait ServiceComponent[Input, Output] {
  def service: BaseService[Input, Output]
}

trait BaseService[Input, Output] extends RequestContextImplicits {

  def doService(request: Input)(implicit ctx: RequestContext, ec: ExecutionContext): Future[Either[ErrorWrapper, ResponseWrapper[Output]]]
}
