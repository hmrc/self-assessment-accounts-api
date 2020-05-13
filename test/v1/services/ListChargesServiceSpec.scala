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
import v1.fixtures.ListChargesFixture._
import v1.mocks.connectors.MockListChargesConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.listCharges.ListChargesParsedRequest
import v1.models.response.listCharges.ListChargesResponse

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ListChargesServiceSpec extends UnitSpec {

  private val nino = Nino("AA123456A")

  private val from = "2020-01-01"
  private val to = "2020-01-02"

  private val correlationId = "X-123"

  private val request = ListChargesParsedRequest(nino, from, to)
  private val response = ListChargesResponse(Seq(fullChargeModel))

  trait Test extends MockListChargesConnector {

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service = new ListChargesService(
      listChargesConnector = mockListChargesConnector
    )
  }

  "service" when {
    "connector call is successful" should {
      "return a Right(ResponseWrapper) when the charges list is not empty" in new Test {
        MockListChargesConnector.retrieve(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.list(request)) shouldBe Right(ResponseWrapper(correlationId, response))
      }

      "return a NoChargesFoundError when the charges list is empty" in new Test {
        MockListChargesConnector.retrieve(request)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, ListChargesResponse(Seq())))))

        await(service.list(request)) shouldBe Left(ErrorWrapper(Some(correlationId), NoChargesFoundError, None))
      }
    }

    "connector call is unsuccessful" should {
      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"map connector error code [$desErrorCode] to MTD error code [${error.code}]" in new Test {

          MockListChargesConnector.retrieve(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.list(request)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
        }

      val input: Seq[(String, MtdError)] = Seq(
        "INVALID_IDTYPE" -> DownstreamError,
        "INVALID_IDNUMBER" -> NinoFormatError,
        "INVALID_REGIME_TYPE" -> DownstreamError,
        "INVALID_DATE_FROM" -> FromDateFormatError,
        "INVALID_DATE_TO" -> ToDateFormatError,
        "NO_DATA_FOUND" -> NotFoundError,
        "SERVER_ERROR" -> DownstreamError,
        "SERVICE_UNAVAILABLE" -> DownstreamError,
        "INVALID_DOC_NUMBER" -> DownstreamError,
        "INVALID_ONLY_OPEN_ITEMS" -> DownstreamError,
        "INVALID_INCLUDE_LOCKS" -> DownstreamError,
        "INVALID_CALCULATE_ACCRUED_INTEREST" -> DownstreamError,
        "INVALID_CUSTOMER_PAYMENT_INFORMATION" -> DownstreamError,
        "INVALID_REMOVE_PAYMENT_ON_ACCOUNT" -> DownstreamError,
        "REQUEST_NOT_PROCESSED" -> DownstreamError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }

}
