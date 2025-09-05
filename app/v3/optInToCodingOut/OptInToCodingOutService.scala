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

package v3.optInToCodingOut

import cats.implicits.*
import common.errors.*
import shared.controllers.RequestContext
import shared.models.errors.*
import shared.services.{BaseService, ServiceOutcome}
import v3.optInToCodingOut.model.request.OptInToCodingOutRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class OptInToCodingOutService @Inject() (connector: OptInToCodingOutConnector) extends BaseService {

  private val errorMap: Map[String, MtdError] =
    Map(
      "INVALID_TAXABLE_ENTITY_ID"      -> NinoFormatError,
      "INVALID_TAX_YEAR"               -> InternalError,
      "INVALID_REGIME"                 -> InternalError,
      "INVALID_CORRELATIONID"          -> InternalError,
      "BUSINESS_PARTNER_NOT_EXIST"     -> RuleBusinessPartnerNotExistError,
      "ITSA_CONTRACT_OBJECT_NOT_EXIST" -> RuleItsaContractObjectNotExistError,
      "REQUEST_NOT_PROCESSED"          -> InternalError,
      "DUPLICATE_ACKNOWLEDGEMENT_REF"  -> InternalError,
      "OPT_OUT_IND_ALREADY_SET"        -> RuleAlreadyOptedInError,
      "SERVER_ERROR"                   -> InternalError,
      "BAD_GATEWAY"                    -> InternalError,
      "SERVICE_UNAVAILABLE"            -> InternalError
    )

  def optInToCodingOut(request: OptInToCodingOutRequestData)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[Unit]] = {

    connector.optInToCodingOut(request.nino, request.taxYear).map(_.leftMap(mapDownstreamErrors(errorMap)))
  }

}
