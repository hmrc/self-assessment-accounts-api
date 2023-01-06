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

package v1.services

import api.models.domain.Nino
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v1.fixtures.ListChargesFixture._
import v1.mocks.connectors.MockListChargesConnector
import v1.models.request.listCharges.ListChargesParsedRequest
import v1.models.response.listCharges.ListChargesResponse

import scala.concurrent.Future

class ListChargesServiceSpec extends ServiceSpec {

  private val nino = Nino("AA123456A")

  private val from = "2020-01-01"
  private val to   = "2020-01-02"

  private val request  = ListChargesParsedRequest(nino, from, to)
  private val response = ListChargesResponse(Seq(fullChargeModel))

  trait Test extends MockListChargesConnector {

    val service = new ListChargesService(
      listChargesConnector = mockListChargesConnector
    )

  }

  "service" when {
    "connector call is successful" should {
      "return a Right(ResponseWrapper) when the charges list is not empty" in new Test {
        MockListChargesConnector
          .retrieve(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.list(request)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "connector call is unsuccessful" should {
      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"map connector error code [$desErrorCode] to MTD error code [${error.code}]" in new Test {

          MockListChargesConnector
            .retrieve(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

          await(service.list(request)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input: Seq[(String, MtdError)] = Seq(
        "INVALID_IDTYPE"                       -> InternalError,
        "INVALID_IDNUMBER"                     -> NinoFormatError,
        "INVALID_REGIME_TYPE"                  -> InternalError,
        "INVALID_DATE_FROM"                    -> V1_FromDateFormatError,
        "INVALID_DATE_TO"                      -> V1_ToDateFormatError,
        "NO_DATA_FOUND"                        -> NotFoundError,
        "SERVER_ERROR"                         -> InternalError,
        "SERVICE_UNAVAILABLE"                  -> InternalError,
        "INVALID_DOC_NUMBER"                   -> InternalError,
        "INVALID_ONLY_OPEN_ITEMS"              -> InternalError,
        "INVALID_INCLUDE_LOCKS"                -> InternalError,
        "INVALID_CALCULATE_ACCRUED_INTEREST"   -> InternalError,
        "INVALID_CUSTOMER_PAYMENT_INFORMATION" -> InternalError,
        "INVALID_DATE_RANGE"                   -> RuleDateRangeInvalidError,
        "INVALID_REQUEST"                      -> InternalError,
        "INVALID_REMOVE_PAYMENT_ON_ACCOUNT"    -> InternalError,
        "INVALID_INCLUDE_STATISTICAL"          -> InternalError,
        "REQUEST_NOT_PROCESSED"                -> InternalError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }

}
