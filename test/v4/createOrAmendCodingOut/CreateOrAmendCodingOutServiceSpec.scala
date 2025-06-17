/*
 * Copyright 2025 HM Revenue & Customs
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

package v4.createOrAmendCodingOut

import common.errors.RuleOutsideAmendmentWindowError
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.{ServiceOutcome, ServiceSpec}
import v4.createOrAmendCodingOut.def1.model.request.Def1_CreateOrAmendCodingOutRequestData
import v4.createOrAmendCodingOut.def1.models.request.CodingOutFixtures.validRequestBody
import v4.createOrAmendCodingOut.model.request.CreateOrAmendCodingOutRequestData

import scala.concurrent.Future

class CreateOrAmendCodingOutServiceSpec extends ServiceSpec {

  private val nino = Nino("AA123456A")

  private val taxYear = TaxYear.fromMtd("2019-20")

  private val request: CreateOrAmendCodingOutRequestData =
    Def1_CreateOrAmendCodingOutRequestData(nino, taxYear, validRequestBody)

  "CreateOrAmendCodingOutService" should {
    "return mapped result" when {
      "connector call is successful" in new Test {
        MockCreateOrAmendCodingOutConnector
          .amendCodingOut(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result: ServiceOutcome[Unit] = await(service.amend(request))
        result shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "map errors according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockCreateOrAmendCodingOutConnector
            .amendCodingOut(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          private val result = await(service.amend(request))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = Map(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_TAX_YEAR"          -> TaxYearFormatError,
        "INVALID_CORRELATIONID"     -> InternalError,
        "INVALID_PAYLOAD"           -> InternalError,
        "INVALID_REQUEST_TAX_YEAR"  -> RuleTaxYearNotEndedError,
        "DUPLICATE_ID_NOT_ALLOWED"  -> RuleDuplicateIdError,
        "OUTSIDE_AMENDMENT_WINDOW"  -> RuleOutsideAmendmentWindowError,
        "SERVER_ERROR"              -> InternalError,
        "SERVICE_UNAVAILABLE"       -> InternalError
      )

      val extraTysErrors = Map(
        "INVALID_CORRELATION_ID" -> InternalError,
        "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError
      )

      (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
    }

  }

  trait Test extends MockCreateOrAmendCodingOutConnector {
    val service = new CreateOrAmendCodingOutService(mockCreateOrAmendCodingOutConnector)
  }

}
