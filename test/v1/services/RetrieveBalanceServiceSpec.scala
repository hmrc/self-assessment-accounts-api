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

package v1.services

import api.models.domain.Nino
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v1.mocks.connectors.MockRetrieveBalanceConnector
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

      MockRetrieveBalanceConnector
        .retrieveBalance(requestData)
        .returns(Future.successful(Right(ResponseWrapper(correlationId, connectorResponse))))

      await(service.retrieveBalance(requestData)) shouldBe Right(ResponseWrapper(correlationId, connectorResponse))
    }
  }

  "unsuccessful" must {
    "map errors according to spec" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the service" in new Test {

          MockRetrieveBalanceConnector
            .retrieveBalance(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(desErrorCode))))))

          await(service.retrieveBalance(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input: Seq[(String, MtdError)] = Seq(
        ("INVALID_IDTYPE", InternalError),
        ("INVALID_IDNUMBER", NinoFormatError),
        ("INVALID_REGIME_TYPE", InternalError),
        ("INVALID_DOC_NUMBER", InternalError),
        ("INVALID_ONLY_OPEN_ITEMS", InternalError),
        ("INVALID_INCLUDE_LOCKS", InternalError),
        ("INVALID_CALCULATE_ACCRUED_INTEREST", InternalError),
        ("INVALID_CUSTOMER_PAYMENT_INFORMATION", InternalError),
        ("INVALID_DATE_FROM", InternalError),
        ("INVALID_DATE_TO", InternalError),
        ("INVALID_DATE_RANGE", InternalError),
        ("INVALID_REQUEST", InternalError),
        ("INVALID_INCLUDE_STATISTICAL", InternalError),
        ("INVALID_REMOVE_PAYMENT_ON_ACCOUNT", InternalError),
        ("REQUEST_NOT_PROCESSED", InternalError),
        ("NO_DATA_FOUND", NotFoundError),
        ("SERVER_ERROR", InternalError),
        ("SERVICE_UNAVAILABLE", InternalError)
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }

}
