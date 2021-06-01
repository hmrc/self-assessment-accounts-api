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
import v1.mocks.connectors.MockRetrieveCodingOutConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.retrieveCodingOut.RetrieveCodingOutParsedRequest
import v1.models.response.retrieveCodingOut.{RetrieveCodingOutResponse, TaxCodeComponent}

import scala.concurrent.Future

class RetrieveCodingOutServiceSpec extends ServiceSpec {

  private val nino = Nino("AA123456A")

  private val taxCodeComponent: TaxCodeComponent =
    TaxCodeComponent(
      amount = 1000,
      relatedTaxYear = "2019-20",
      submittedOn = "2020-07-06T09:37:17Z"
    )

  private val retrieveCodingOutResponse: RetrieveCodingOutResponse =
    RetrieveCodingOutResponse(
      source = "hmrcHeld",
      selfAssessmentUnderPayments = Some(Seq(taxCodeComponent)),
      payeUnderpayments = Some(Seq(taxCodeComponent)),
      debts = Some(Seq(taxCodeComponent)),
      inYearAdjustments = Some(taxCodeComponent)
    )

  private val requestData: RetrieveCodingOutParsedRequest =
    RetrieveCodingOutParsedRequest(
      nino = nino,
      taxYear = "2019-20",
      source = Some("hmrcHeld")
    )

  trait Test extends MockRetrieveCodingOutConnector {

    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("RetrieveCodingOutParsedRequest", "retrieveCodingOut")

    val service = new RetrieveCodingOutService(
      connector = mockRetrieveCodingOutConnector
    )
  }

  "RetrieveCodingOutService" when {
    "service call successful" must {
      "return mapped result" in new Test {

        val connectorResponse: RetrieveCodingOutResponse = retrieveCodingOutResponse

        MockRetrieveCodingOutConnector.retrieveCodingOut(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, connectorResponse))))

        await(service.retrieveCodingOut(requestData)) shouldBe Right(ResponseWrapper(correlationId, connectorResponse))
      }
    }

    "unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockRetrieveCodingOutConnector.retrieveCodingOut(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.retrieveCodingOut(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input: Seq[(String, MtdError)] = Seq(
          "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
          "INVALID_TAX_YEAR" -> TaxYearFormatError,
          "INVALID_VIEW" -> SourceFormatError,
          "INVALID_CORRELATIONID" -> DownstreamError,
          "NO_DATA_FOUND" -> CodingOutNotFoundError,
          "TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
          "SERVER_ERROR" -> DownstreamError,
          "SERVICE_UNAVAILABLE" -> DownstreamError
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}