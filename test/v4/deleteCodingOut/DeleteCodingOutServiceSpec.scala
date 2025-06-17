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

package v4.deleteCodingOut

import common.errors.{CodingOutNotFoundError, RuleOutsideAmendmentWindowError}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v4.deleteCodingOut.def1.model.request.Def1_DeleteCodingOutRequestData
import v4.deleteCodingOut.model.request.DeleteCodingOutRequestData

import scala.concurrent.Future

class DeleteCodingOutServiceSpec extends ServiceSpec {

  private val nino = Nino("AA123456A")

  private val taxYear = TaxYear.fromMtd("2019-20")

  private val requestData: DeleteCodingOutRequestData = Def1_DeleteCodingOutRequestData(nino, taxYear)

  "DeleteCodingOutService" should {
    "return mapped result" when {
      "connector call is successful" in new Test {
        MockDeleteCodingOutConnector
          .deleteCodingOut(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ()))))

        val result = await(service.deleteCodingOut(requestData))
        result shouldBe Right(ResponseWrapper(correlationId, ()))
      }
    }

    "map errors according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockDeleteCodingOutConnector.deleteCodingOut(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          private val result = await(service.deleteCodingOut(requestData))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = Map(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_TAX_YEAR"          -> TaxYearFormatError,
        "OUTSIDE_AMENDMENT_WINDOW"  -> RuleOutsideAmendmentWindowError,
        "INVALID_CORRELATIONID"     -> InternalError,
        "NO_DATA_FOUND"             -> CodingOutNotFoundError,
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

  trait Test extends MockDeleteCodingOutConnector {
    val service = new DeleteCodingOutService(mockDeleteCodingOutConnector)
  }

}
