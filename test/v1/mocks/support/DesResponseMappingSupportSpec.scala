/*
 * Copyright 2022 HM Revenue & Customs
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

package v1.mocks.support

import api.controllers.EndpointLogContext
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import play.api.http.Status.BAD_REQUEST
import support.UnitSpec
import utils.Logging
import v1.models.response.retrieveTransactionDetails.{RetrieveTransactionDetailsResponse, TransactionItem}
import v1.support.DesResponseMappingSupport

class DesResponseMappingSupportSpec extends UnitSpec {

  implicit val logContext: EndpointLogContext         = EndpointLogContext("ctrl", "ep")
  val mapping: DesResponseMappingSupport with Logging = new DesResponseMappingSupport with Logging {}

  val correlationId: String = "someCorrelationId"

  object Error1 extends MtdError("msg", "code1", BAD_REQUEST)

  object Error2 extends MtdError("msg", "code2", BAD_REQUEST)

  object ErrorBvrMain extends MtdError("msg", "bvrMain", BAD_REQUEST)

  object ErrorBvr extends MtdError("msg", "bvr", BAD_REQUEST)

  val errorCodeMap: PartialFunction[String, MtdError] = {
    case "ERR1" => Error1
    case "ERR2" => Error2
    case "DS"   => DownstreamError
  }

  "validateTransactionDetailsResponse" when {
    "passed a RetrieveTransactionDetailsResponse with an empty transactionItems array" should {
      "return a NoTransactionDetailsFoundError error" in {
        val responseModel: RetrieveTransactionDetailsResponse = RetrieveTransactionDetailsResponse(
          transactionItems = Seq.empty[TransactionItem]
        )

        mapping.validateTransactionDetailsResponse(ResponseWrapper(correlationId, responseModel)) shouldBe
          Left(ErrorWrapper(correlationId, NoTransactionDetailsFoundError))
      }
    }
    "passed anything else " should {
      "pass it through" in {
        mapping.validateTransactionDetailsResponse(ResponseWrapper(correlationId, NotFoundError)) shouldBe
          Right(ResponseWrapper(correlationId, NotFoundError))
      }
    }
  }

  "mapping Des errors" when {
    "single error" when {
      "the error code is in the map provided" must {
        "use the mapping and wrap" in {
          mapping.mapDownstreamErrors(errorCodeMap)(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("ERR1")))) shouldBe
            ErrorWrapper(correlationId, Error1)
        }
      }

      "the error code is not in the map provided" must {
        "default to DownstreamError and wrap" in {
          mapping.mapDownstreamErrors(errorCodeMap)(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode("UNKNOWN")))) shouldBe
            ErrorWrapper(correlationId, DownstreamError)
        }
      }
    }

    "multiple errors" when {
      "the error codes is in the map provided" must {
        "use the mapping and wrap with main error type of BadRequest" in {
          mapping.mapDownstreamErrors(errorCodeMap)(
            ResponseWrapper(correlationId, DownstreamErrors(List(DownstreamErrorCode("ERR1"), DownstreamErrorCode("ERR2"))))) shouldBe
            ErrorWrapper(correlationId, BadRequestError, Some(Seq(Error1, Error2)))
        }
      }

      "the error code is not in the map provided" must {
        "default main error to DownstreamError ignore other errors" in {
          mapping.mapDownstreamErrors(errorCodeMap)(
            ResponseWrapper(correlationId, DownstreamErrors(List(DownstreamErrorCode("ERR1"), DownstreamErrorCode("UNKNOWN"))))) shouldBe
            ErrorWrapper(correlationId, DownstreamError)
        }
      }

      "one of the mapped errors is DownstreamError" must {
        "wrap the errors with main error type of DownstreamError" in {
          mapping.mapDownstreamErrors(errorCodeMap)(
            ResponseWrapper(correlationId, DownstreamErrors(List(DownstreamErrorCode("ERR1"), DownstreamErrorCode("DS"))))) shouldBe
            ErrorWrapper(correlationId, DownstreamError)
        }
      }
    }

    "the error code is an OutboundError" must {
      "return the error as is (in an ErrorWrapper)" in {
        mapping.mapDownstreamErrors(errorCodeMap)(ResponseWrapper(correlationId, OutboundError(ErrorBvrMain))) shouldBe
          ErrorWrapper(correlationId, ErrorBvrMain)
      }
    }

    "the error code is an OutboundError with multiple errors" must {
      "return the error as is (in an ErrorWrapper)" in {
        mapping.mapDownstreamErrors(errorCodeMap)(ResponseWrapper(correlationId, OutboundError(ErrorBvrMain, Some(Seq(ErrorBvr))))) shouldBe
          ErrorWrapper(correlationId, ErrorBvrMain, Some(Seq(ErrorBvr)))
      }
    }
  }

}
