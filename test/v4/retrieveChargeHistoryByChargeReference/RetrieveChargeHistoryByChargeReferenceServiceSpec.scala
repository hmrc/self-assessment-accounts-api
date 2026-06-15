/*
 * Copyright 2026 HM Revenue & Customs
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
import api.models.domain.Nino
import api.models.errors.*
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
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
          "014" -> NotFoundError,
          "002" -> InternalError,
          "003" -> InternalError,
          "005" -> NotFoundError,
          "015" -> InternalError
        )

      errors.foreach(args => serviceError.tupled(args))
    }
  }

  trait Test extends MockRetrieveChargeHistoryByChargeReferenceConnector {

    val service = new RetrieveChargeHistoryByChargeReferenceService(
      connector = mockRetrieveChargeHistoryByChargeReferenceConnector
    )

  }

}
