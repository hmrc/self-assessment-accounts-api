/*
 * Copyright 2021 HM Revenue & Customs
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

import v1.models.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockRetrieveBalanceConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveBalance.RetrieveBalanceParsedRequest
import v1.models.response.retrieveBalance.RetrieveBalanceResponse

import scala.concurrent.Future

class RetrieveBalanceServiceSpec extends ServiceSpec {

  private val nino = "AA123456A"

  private val requestData: RetrieveBalanceParsedRequest =
    RetrieveBalanceParsedRequest(
      nino = Nino(nino)
    )

  trait Test extends MockRetrieveBalanceConnector {

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("controller", "retrieveBalance")

    val service = new RetrieveBalanceService(
      connector = mockRetrieveBalanceConnector
    )
  }

  "service" when {
    "service call successful" must {}
    "return mapped result" in new Test {

      val connectorResponse: RetrieveBalanceResponse =
        RetrieveBalanceResponse(
          overdueAmount = 100.00,
          payableAmount = 100.00,
          payableDueDate = Some("2020-03-01"),
          pendingChargeDueAmount = 100.00,
          pendingChargeDueDate = Some("2020-06-01"),
          totalBalance = 100.00
        )

      MockRetrieveBalanceConnector.retrieveBalance(requestData)
        .returns(Future.successful(Right(ResponseWrapper(correlationId, connectorResponse))))

      await(service.retrieveBalance(requestData)) shouldBe Right(ResponseWrapper(correlationId,connectorResponse))
    }
  }

  "unsuccessful" must {
    "map errors according to spec" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the service" in new Test {

          MockRetrieveBalanceConnector.retrieveBalance(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.retrieveBalance(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
      }

      val input: Seq[(String, MtdError)] = Seq(
        ("INVALID_IDTYPE", DownstreamError),
        ("INVALID_IDNUMBER", NinoFormatError),
        ("INVALID_REGIME_TYPE", DownstreamError),
        ("INVALID_DOC_NUMBER", DownstreamError),
        ("INVALID_ONLY_OPEN_ITEMS", DownstreamError),
        ("INVALID_INCLUDE_LOCKS", DownstreamError),
        ("INVALID_CALCULATE_ACCRUED_INTEREST", DownstreamError),
        ("INVALID_CUSTOMER_PAYMENT_INFORMATION", DownstreamError),
        ("INVALID_DATE_FROM", DownstreamError),
        ("INVALID_DATE_TO", DownstreamError),
        ("INVALID_DATE_RANGE", DownstreamError),
        ("INVALID_REQUEST", DownstreamError),
        ("INVALID_INCLUDE_STATISTICAL", DownstreamError),
        ("INVALID_REMOVE_PAYMENT_ON_ACCOUNT", DownstreamError),
        ("REQUEST_NOT_PROCESSED", DownstreamError),
        ("NO_DATA_FOUND", NotFoundError),
        ("SERVER_ERROR", DownstreamError),
        ("SERVICE_UNAVAILABLE", DownstreamError)
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}
