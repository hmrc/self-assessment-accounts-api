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

package v3.listPaymentsAndAllocationDetails

import api.services.ServiceSpec
import v3.listPaymentsAndAllocationDetails.def1.model.request.Def1_ListPaymentsAndAllocationDetailsRequestData
import v3.listPaymentsAndAllocationDetails.def1.model.response.ResponseFixtures.responseObject
import v3.listPaymentsAndAllocationDetails.model.request.ListPaymentsAndAllocationDetailsRequestData
import v3.listPaymentsAndAllocationDetails.model.response.ListPaymentsAndAllocationDetailsResponse

import scala.concurrent.Future

class ListPaymentsAndAllocationDetailsServiceSpec extends ServiceSpec {

  private val nino = "AA123456A"

  private val request: ListPaymentsAndAllocationDetailsRequestData =
    Def1_ListPaymentsAndAllocationDetailsRequestData(Nino(nino), None, None, None)

  "ListPaymentsAndAllocationDetailsService" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockListPaymentsAndAllocationDetailsConnector
          .listPaymentsAndAllocationDetails(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseObject))))

        val result: ServiceOutcome[ListPaymentsAndAllocationDetailsResponse] = await(service.listPaymentsAndAllocationDetails(request))
        result shouldBe Right(ResponseWrapper(correlationId, responseObject))
      }
    }

    "map errors according to spec" when {
      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockListPaymentsAndAllocationDetailsConnector
            .listPaymentsAndAllocationDetails(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          private val result = await(service.listPaymentsAndAllocationDetails(request))
          result shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors: Seq[(String, MtdError)] = List(
        "INVALID_CORRELATIONID"    -> InternalError,
        "INVALID_IDVALUE"          -> NinoFormatError,
        "INVALID_IDTYPE"           -> InternalError,
        "INVALID_REGIME_TYPE"      -> InternalError,
        "INVALID_PAYMENT_LOT"      -> PaymentLotFormatError,
        "INVALID_PAYMENT_LOT_ITEM" -> PaymentLotItemFormatError,
        "INVALID_CLEARING_DOC"     -> InternalError,
        "INVALID_DATE_FROM"        -> FromDateFormatError,
        "INVALID_DATE_TO"          -> ToDateFormatError,
        "INVALID_DATE_RANGE"       -> RuleInvalidDateRangeError,
        "INVALID_REQUEST"          -> RuleInconsistentQueryParamsErrorListSA,
        "REQUEST_NOT_PROCESSED"    -> BadRequestError,
        "NO_DATA_FOUND"            -> NotFoundError,
        "PARTIALLY_MIGRATED"       -> BadRequestError,
        "SERVER_ERROR"             -> InternalError,
        "SERVICE_UNAVAILABLE"      -> InternalError
      )

      errors.foreach(args => (serviceError _).tupled(args))
    }

  }

  trait Test extends MockListPaymentsAndAllocationDetailsConnector {

    val service = new ListPaymentsAndAllocationDetailsService(
      connector = mockListPaymentsAndAllocationDetailsConnector
    )

  }

}
