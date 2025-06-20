/*
 * Copyright 2024 HM Revenue & Customs
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

package v3.optOutOfCodingOut

import common.errors.{RuleAlreadyOptedOutError, RuleBusinessPartnerNotExistError, RuleItsaContractObjectNotExistError}
import shared.models.domain.{Nino, TaxYear}
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v3.optOutOfCodingOut.def1.model.request.Def1_OptOutOfCodingOutRequestData
import v3.optOutOfCodingOut.def1.model.response.Def1_OptOutOfCodingOutResponse

import scala.concurrent.Future

class OptOutOfCodingOutServiceSpec extends ServiceSpec {

  private val nino               = Nino("AA123456A")
  private val taxYear            = TaxYear("2014")
  private val requestData        = Def1_OptOutOfCodingOutRequestData(nino, taxYear)
  private val downstreamResponse = Def1_OptOutOfCodingOutResponse(processingDate = "2020-12-17T09:30:47Z")

  "OptOutOfCodingOutService" when {
    "service call successful" must {
      "return success" in new Test {
        MockOptOutOfCodingOutConnector.amendCodingOutOptOut(requestData) returns
          Future.successful(Right(ResponseWrapper(correlationId, downstreamResponse)))

        await(service.optOutOfCodingOut(requestData)) shouldBe Right(ResponseWrapper(correlationId, downstreamResponse))
      }
    }

    "service call unsuccessful" must {
      "map errors according to spec" when {
        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockOptOutOfCodingOutConnector.amendCodingOutOptOut(requestData) returns
              Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode)))))

            await(service.optOutOfCodingOut(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors: Seq[(String, MtdError)] =
          List(
            "INVALID_TAXABLE_ENTITY_ID"      -> NinoFormatError,
            "INVALID_TAX_YEAR"               -> InternalError,
            "INVALID_REGIME"                 -> InternalError,
            "INVALID_CORRELATIONID"          -> InternalError,
            "BUSINESS_PARTNER_NOT_EXIST"     -> RuleBusinessPartnerNotExistError,
            "ITSA_CONTRACT_OBJECT_NOT_EXIST" -> RuleItsaContractObjectNotExistError,
            "REQUEST_NOT_PROCESSED"          -> InternalError,
            "DUPLICATE_ACKNOWLEDGEMENT_REF"  -> InternalError,
            "OPT_OUT_IND_ALREADY_SET"        -> RuleAlreadyOptedOutError,
            "BAD_GATEWAY"                    -> InternalError,
            "SERVER_ERROR"                   -> InternalError,
            "SERVICE_UNAVAILABLE"            -> InternalError
          )

        errors.foreach(args => (serviceError _).tupled(args))
      }
    }
  }

  trait Test extends MockOptOutOfCodingOutConnector {

    val service = new OptOutOfCodingOutService(
      connector = mockOptOutOfCodingOutConnector
    )

  }

}
