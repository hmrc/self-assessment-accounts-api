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

package v3.services

import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v3.connectors.MockRetrieveCodingOutStatusConnector
import v3.models.errors.{BusinessPartnerNotExistError, ITSAContractObjectNotExistError}
import v3.models.request.retrieveCodingOutStatus.RetrieveCodingOutStatusRequestData
import v3.models.response.retrieveCodingOutStatus.RetrieveCodingOutStatusResponse

import scala.concurrent.Future

class RetrieveCodingOutStatusServiceSpec extends ServiceSpec {

  private val nino    = "AA123456A"
  private val taxYear = "2014"

  private val requestData: RetrieveCodingOutStatusRequestData =
    RetrieveCodingOutStatusRequestData(
      Nino(nino),
      TaxYear(taxYear)
    )

  val retrieveCodingOutStatusResponse: RetrieveCodingOutStatusResponse =
    RetrieveCodingOutStatusResponse(processingDate = "2023-12-17T09:30:47Z", nino = nino, taxYear = TaxYear(taxYear), optOutIndicator = true)

  "RetrieveCodingOutStatusService" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockRetrieveCodingOutStatusConnector
          .retrieveCodingOutStatus(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, retrieveCodingOutStatusResponse))))

        await(service.retrieveCodingOutStatus(requestData)) shouldBe Right(ResponseWrapper(correlationId, retrieveCodingOutStatusResponse))
      }
    }

    "map errors according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockRetrieveCodingOutStatusConnector
            .retrieveCodingOutStatus(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          private val result = await(service.retrieveCodingOutStatus(requestData))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors: Seq[(String, MtdError)] =
        List(
          "INVALID_TAXABLE_ENTITY_ID"      -> NinoFormatError,
          "INVALID_TAX_YEAR"               -> TaxYearFormatError,
          "INVALID_REGIME"                 -> InternalError,
          "INVALID_CORRELATIONID"          -> InternalError,
          "DUPLICATE_SUBMISSION"           -> InternalError,
          "BUSINESS_PARTNER_NOT_EXIST"     -> BusinessPartnerNotExistError,
          "ITSA_CONTRACT_OBJECT_NOT_EXIST" -> ITSAContractObjectNotExistError,
          "REQUEST_NOT_PROCESSED"          -> InternalError,
          "SERVER_ERROR"                   -> InternalError,
          "BAD_GATEWAY"                    -> InternalError,
          "SERVICE_UNAVAILABLE"            -> InternalError
        )

      errors.foreach(args => (serviceError _).tupled(args))
    }

  }

  trait Test extends MockRetrieveCodingOutStatusConnector {

    val service = new RetrieveCodingOutStatusService(
      connector = mockRetrieveCodingOutStatusConnector
    )

  }

}
