/*
 * Copyright 2020 HM Revenue & Customs
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

import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.fixtures.RetrieveChargeHistoryFixture
import v1.mocks.connectors.MockRetrieveChargeHistoryConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveChargeHistory.RetrieveChargeHistoryParsedRequest
import v1.models.response.retrieveChargeHistory.RetrieveChargeHistoryResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveChargeHistoryServiceSpec extends UnitSpec {

  private val nino = Nino("AA123456A")
  private val chargeId = "anId"
  private val correlationId = "X-123"

  private val requestData: RetrieveChargeHistoryParsedRequest =
    RetrieveChargeHistoryParsedRequest(
      nino = nino,
      chargeId = chargeId
    )

  trait Test extends MockRetrieveChargeHistoryConnector {

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("RetrieveChargeHistoryController", "retrieveChargeHistory")

    val service = new RetrieveChargeHistoryService(
      connector = mockRetrieveChargeHistoryConnector
    )
  }

  "RetrieveChargeHistoryService" when {
    "service call successful" must {}
    "return mapped result" in new Test {

      val connectorResponse: RetrieveChargeHistoryResponse = RetrieveChargeHistoryFixture.retrieveChargeHistoryResponse

      MockRetrieveChargeHistoryConnector.retrieveChargeHistory(requestData)
        .returns(Future.successful(Right(ResponseWrapper(correlationId, connectorResponse))))

      await(service.retrieveBalance(requestData)) shouldBe Right(ResponseWrapper(correlationId, connectorResponse))
    }
  }

  "unsuccessful" must {
    "map errors according to spec" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the service" in new Test {

          MockRetrieveChargeHistoryConnector.retrieveChargeHistory(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.retrieveBalance(requestData)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
        }

      val input: Seq[(String, MtdError)] = Seq(
        ("INVALID_IDTYPE", DownstreamError),
        ("INVALID_IDVALUE", NinoFormatError),
        ("INVALID_REGIME_TYPE", DownstreamError),
        ("INVALID_DOCUMENT_ID", ChargeIdFormatError),
        ("NO_DATA_FOUND", NotFoundError),
        ("SERVER_ERROR", DownstreamError),
        ("SERVICE_UNAVAILABLE", DownstreamError)
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}