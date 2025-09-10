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

package v4.retrieveCodingOut

import common.errors.*
import common.models.MtdSource.hmrcHeld
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.services.{ServiceOutcome, ServiceSpec}
import v4.retrieveCodingOut.def1.model.reponse.RetrieveCodingOutFixture.retrieveCodingOutResponse
import v4.retrieveCodingOut.def1.model.request.Def1_RetrieveCodingOutRequestData
import v4.retrieveCodingOut.def1.model.response.Def1_RetrieveCodingOutResponse
import v4.retrieveCodingOut.model.response.RetrieveCodingOutResponse

import scala.concurrent.Future

class RetrieveCodingOutServiceSpec extends ServiceSpec {

  private val nino    = Nino("AA123456A")
  private val taxYear = TaxYear.fromMtd("2019-20")
  private val source  = hmrcHeld

  private val requestData = Def1_RetrieveCodingOutRequestData(nino, taxYear, Some(source))

  val response: Def1_RetrieveCodingOutResponse = retrieveCodingOutResponse

  "RetrieveCodingOutService" should {
    "return the mapped result" when {
      "the service call is successfult" in new Test {
        MockRetrieveCodingOutConnector
          .retrieveCodingOut(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        val result: ServiceOutcome[RetrieveCodingOutResponse] = await(service.retrieveCodingOut(requestData))
        result shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "map errors according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {
          MockRetrieveCodingOutConnector
            .retrieveCodingOut(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          val result: ServiceOutcome[RetrieveCodingOutResponse] = await(service.retrieveCodingOut(requestData))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = Map(
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_TAX_YEAR"          -> TaxYearFormatError,
        "INVALID_VIEW"              -> SourceFormatError,
        "INVALID_CORRELATIONID"     -> InternalError,
        "NO_DATA_FOUND"             -> CodingOutNotFoundError,
        "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
        "SERVER_ERROR"              -> InternalError,
        "SERVICE_UNAVAILABLE"       -> InternalError
      )

      val extraTysErrors = Map(
        "INVALID_CORRELATION_ID" -> InternalError,
        "NOT_FOUND"              -> CodingOutNotFoundError
      )

      (errors ++ extraTysErrors).foreach(serviceError.tupled)
    }
  }

  trait Test extends MockRetrieveCodingOutConnector {
    val service = new RetrieveCodingOutService(mockRetrieveCodingOutConnector)
  }

}
