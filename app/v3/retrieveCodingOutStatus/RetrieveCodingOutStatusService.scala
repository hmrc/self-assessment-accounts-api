/*
 * Copyright 2023 HM Revenue & Customs
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

package v3.retrieveCodingOutStatus

import cats.implicits._
import shared.controllers.RequestContext
import shared.models.errors._
import shared.services.{BaseService, ServiceOutcome}
import v3.common.errors._
import v3.retrieveCodingOutStatus.model.request.RetrieveCodingOutStatusRequestData
import v3.retrieveCodingOutStatus.model.response.RetrieveCodingOutStatusResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveCodingOutStatusService @Inject() (connector: RetrieveCodingOutStatusConnector) extends BaseService {

  private val errorMap: Map[String, MtdError] =
    Map(
      "INVALID_TAXABLE_ENTITY_ID"      -> NinoFormatError,
      "INVALID_TAX_YEAR"               -> InternalError,
      "INVALID_REGIME"                 -> InternalError,
      "INVALID_CORRELATIONID"          -> InternalError,
      "DUPLICATE_SUBMISSION"           -> InternalError,
      "BUSINESS_PARTNER_NOT_EXIST"     -> RuleBusinessPartnerNotExistError,
      "ITSA_CONTRACT_OBJECT_NOT_EXIST" -> RuleItsaContractObjectNotExistError,
      "REQUEST_NOT_PROCESSED"          -> InternalError,
      "SERVER_ERROR"                   -> InternalError,
      "BAD_GATEWAY"                    -> InternalError,
      "SERVICE_UNAVAILABLE"            -> InternalError
    )

  def retrieveCodingOutStatus(request: RetrieveCodingOutStatusRequestData)(implicit
      ctx: RequestContext,
      ec: ExecutionContext): Future[ServiceOutcome[RetrieveCodingOutStatusResponse]] = {

    connector.retrieveCodingOutStatus(request).map(_.leftMap(mapDownstreamErrors(errorMap)))
  }

}
