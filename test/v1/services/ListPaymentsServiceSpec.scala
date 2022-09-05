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

import v1.models.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockListPaymentsConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.listPayments.ListPaymentsParsedRequest
import v1.models.response.listPayments.{ListPaymentsResponse, Payment}

import scala.concurrent.Future

class ListPaymentsServiceSpec extends ServiceSpec {

  private val nino = Nino("AA123456A")

  private val from = "2020-01-01"
  private val to   = "2020-01-02"

  private val request  = ListPaymentsParsedRequest(nino, from, to)
  private val response = ListPaymentsResponse(Seq(Payment(Some("123-456"), Some(10.25), Some("beans"), Some("10/01/2020"))))

  trait Test extends MockListPaymentsConnector {

    implicit val hc: HeaderCarrier              = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new ListPaymentsService(
      listPaymentsConnector = mockListPaymentsConnector
    )

  }

  "service" when {
    "connector call is successful" should {
      "return a Right(ResponseWrapper) when the payments list is not empty" in new Test {
        MockListPaymentsConnector
          .retrieve(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.list(request)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "connector call is unsuccessful" should {
      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"map connector error code [$desErrorCode] to MTD error code [${error.code}]" in new Test {

          MockListPaymentsConnector
            .retrieve(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.list(request)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input: Seq[(String, MtdError)] = Seq(
        "INVALID_IDTYPE"           -> DownstreamError,
        "INVALID_IDVALUE"          -> NinoFormatError,
        "INVALID_REGIME_TYPE"      -> DownstreamError,
        "INVALID_PAYMENT_LOT"      -> DownstreamError,
        "INVALID_PAYMENT_LOT_ITEM" -> DownstreamError,
        "INVALID_CLEARING_DOC"     -> DownstreamError,
        "INVALID_DATE_FROM"        -> FromDateFormatError,
        "INVALID_DATE_TO"          -> ToDateFormatError,
        "INVALID_DATE_RANGE"       -> DownstreamError,
        "INVALID_CORRELATIONID"    -> DownstreamError,
        "REQUEST_NOT_PROCESSED"    -> DownstreamError,
        "NO_DATA_FOUND"            -> NotFoundError,
        "PARTIALLY_MIGRATED"       -> DownstreamError,
        "SERVER_ERROR"             -> DownstreamError,
        "SERVICE_UNAVAILABLE"      -> DownstreamError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }

}
