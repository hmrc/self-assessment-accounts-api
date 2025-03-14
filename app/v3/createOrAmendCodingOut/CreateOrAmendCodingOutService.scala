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

package v3.createOrAmendCodingOut

import cats.implicits._
import shared.controllers.RequestContext
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.BaseService
import v3.createOrAmendCodingOut.model.request.CreateOrAmendCodingOutRequestData

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CreateOrAmendCodingOutService @Inject()(connector: CreateOrAmendCodingOutConnector) extends BaseService {

  def amend(request: CreateOrAmendCodingOutRequestData)(implicit
                                                        ctx: RequestContext,
                                                        ec: ExecutionContext): Future[Either[ErrorWrapper, ResponseWrapper[Unit]]] = {
    connector
      .amendCodingOut(request)
      .map(_.leftMap(mapDownstreamErrors(errorMap)))
  }

  private val errorMap = {
    val errors = Map(
      "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
      "INVALID_TAX_YEAR"          -> TaxYearFormatError,
      "INVALID_CORRELATIONID"     -> InternalError,
      "INVALID_PAYLOAD"           -> InternalError,
      "INVALID_REQUEST_TAX_YEAR"  -> RuleTaxYearNotEndedError,
      "DUPLICATE_ID_NOT_ALLOWED"  -> RuleDuplicateIdError,
      "SERVER_ERROR"              -> InternalError,
      "SERVICE_UNAVAILABLE"       -> InternalError
    )

    val extraTysErrors = Map(
      "INVALID_CORRELATION_ID" -> InternalError,
      "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
    )

    errors ++ extraTysErrors
  }

}
