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

package v4.retrieveChargeHistoryByChargeReference

import common.models.ChargeReference
import shared.models.domain.Nino
import shared.models.errors._
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v4.retrieveChargeHistoryByChargeReference.def1.model.request.Def1_RetrieveChargeHistoryByChargeReferenceRequestData
import v4.retrieveChargeHistoryByChargeReference.def1.model.response.RetrieveChargeHistoryFixture.validChargeHistoryResponseObject
import v4.retrieveChargeHistoryByChargeReference.model.request.RetrieveChargeHistoryByChargeReferenceRequestData

import scala.concurrent.Future

class RetrieveChargeHistoryByChargeReferenceServiceSpec extends ServiceSpec {

  private val nino            = Nino("AA123456A")
  private val chargeReference = ChargeReference("anId")

  private val requestData: RetrieveChargeHistoryByChargeReferenceRequestData =
    Def1_RetrieveChargeHistoryByChargeReferenceRequestData(
      nino = nino,
      chargeReference = chargeReference
    )

  "RetrieveChargeHistoryService" should {
    "service call successful" when {
      "return mapped result" in new Test {
        MockRetrieveChargeHistoryByChargeReferenceConnector
          .retrieveChargeHistoryByChargeReference(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, validChargeHistoryResponseObject))))

        private val result = await(service.retrieveChargeHistoryByChargeReference(requestData))
        result shouldBe Right(ResponseWrapper(correlationId, validChargeHistoryResponseObject))
      }
    }
  }

  "unsuccessful" must {
    "map errors according to spec" when {

      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockRetrieveChargeHistoryByChargeReferenceConnector
            .retrieveChargeHistoryByChargeReference(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.retrieveChargeHistoryByChargeReference(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors: Seq[(String, MtdError)] =
        List(
          "INVALID_CORRELATIONID" -> InternalError,
          "INVALID_ID_TYPE"       -> InternalError,
          "INVALID_IDVALUE"       -> NinoFormatError,
          "INVALID_REGIME_TYPE"   -> InternalError,
          "INVALID_DOC_NUMBER"    -> TransactionIdFormatError,
          "INVALID_DATE_FROM"     -> InternalError,
          "INVALID_DATE_TO"       -> InternalError,
          "INVALID_DATE_RANGE"    -> InternalError,
          "INVALID_REQUEST"       -> InternalError,
          "REQUEST_NOT_PROCESSED" -> InternalError,
          "NO_DATA_FOUND"         -> NotFoundError,
          "SERVER_ERROR"          -> InternalError,
          "SERVICE_UNAVAILABLE"   -> InternalError
        )

      errors.foreach(args => (serviceError _).tupled(args))
    }
  }

  trait Test extends MockRetrieveChargeHistoryByChargeReferenceConnector {

    val service = new RetrieveChargeHistoryByChargeReferenceService(
      connector = mockRetrieveChargeHistoryByChargeReferenceConnector
    )

  }

}
