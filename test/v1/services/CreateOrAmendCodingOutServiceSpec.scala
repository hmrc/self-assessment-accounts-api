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
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockCreateOrAmendCodingOutConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.createOrAmendCodingOut._

import scala.concurrent.Future

class CreateOrAmendCodingOutServiceSpec extends ServiceSpec {

  private val nino = "AA112233A"
  private val taxYear = "2021-22"

  val createOrAmendCodingOutRequestBody: CreateOrAmendCodingOutRequestBody = CreateOrAmendCodingOutRequestBody(taxCodeComponents = TaxCodeComponents(
    payeUnderpayment = Some(Seq(TaxCodeComponent(id = 12345, amount = 123.45))),
    selfAssessmentUnderpayment = Some(Seq(TaxCodeComponent(id = 12345, amount = 123.45))),
    debt = Some(Seq(TaxCodeComponent(id = 12345, amount = 123.45))),
    inYearAdjustment = Some(TaxCodeComponent(id = 12345, amount = 123.45))
  ))

  val request: CreateOrAmendCodingOutParsedRequest = CreateOrAmendCodingOutParsedRequest(
    nino = Nino(nino),
    taxYear = taxYear,
    body = createOrAmendCodingOutRequestBody
  )

  trait Test extends MockCreateOrAmendCodingOutConnector{
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: CreateOrAmendCodingOutService = new CreateOrAmendCodingOutService(
      connector = mockCreateOrAmendCodingOutConnector
    )
  }

  "CreateOrAmendCodingOutService" when {
    ".amend" should {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockCreateOrAmendCodingOutConnector.amendCodingOut(request)
          .returns(Future.successful(outcome))

        await(service.amend(request)) shouldBe outcome
      }

      "map errors according to spec" when {

        def serviceError(desErrorCode: String, error: MtdError): Unit =
          s"a $desErrorCode error is returned from the service" in new Test {

            MockCreateOrAmendCodingOutConnector.amendCodingOut(request)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

            await(service.amend(request)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val input = Seq(
          ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
          ("INVALID_TAX_YEAR", TaxYearFormatError),
          ("INVALID_CORRELATIONID", DownstreamError),
          ("INVALID_PAYLOAD", DownstreamError),
          ("INVALID_REQUEST_TAX_YEAR", RuleTaxYearNotEndedError),
          ("SERVER_ERROR", DownstreamError),
          ("SERVICE_UNAVAILABLE", DownstreamError)
        )

        input.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}