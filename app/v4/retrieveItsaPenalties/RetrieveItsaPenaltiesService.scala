/*
 * Copyright 2026 HM Revenue & Customs
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

package v4.retrieveItsaPenalties

import cats.implicits.*
import api.controllers.RequestContext
import api.models.errors.*
import api.services.{BaseService, ServiceOutcome}
import v4.retrieveItsaPenalties.model.request.RetrieveItsaPenaltiesRequestData
import v4.retrieveItsaPenalties.model.response.RetrieveItsaPenaltiesResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveItsaPenaltiesService @Inject() (connector: RetrieveItsaPenaltiesConnector) extends BaseService {

  def retrieveItsaPenalties(request: RetrieveItsaPenaltiesRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[RetrieveItsaPenaltiesResponse]] =
    connector.retrieveItsaPenalties(request).map(_.leftMap(mapDownstreamErrors(errorMap)))

  private val errorMap: Map[String, MtdError] =
    Map(
      "016" -> NinoFormatError,
      "002" -> InternalError,
      "015" -> InternalError,
      "003" -> InternalError,
      "135" -> InternalError
    )

}
