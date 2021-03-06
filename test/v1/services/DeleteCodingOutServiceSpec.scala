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
import v1.mocks.connectors.MockDeleteCodingOutConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.request.deleteCodingOut.DeleteCodingOutParsedRequest

import scala.concurrent.Future

class DeleteCodingOutServiceSpec extends ServiceSpec {

  private val nino = "AA123456A"
  private val taxYear = "2021-22"

  val request: DeleteCodingOutParsedRequest = DeleteCodingOutParsedRequest(
    nino = Nino(nino),
    taxYear = taxYear
  )

  trait Test extends MockDeleteCodingOutConnector {
    implicit val logContext: EndpointLogContext = EndpointLogContext("c", "ep")

    val service: DeleteCodingOutService = new DeleteCodingOutService(
      connector = mockDeleteCodingOutConnector
    )
  }

  "DeleteCodingOutService" when {
    ".deleteCodingOut" must {
      "return correct result for a success" in new Test {
        val outcome = Right(ResponseWrapper(correlationId, ()))

        MockDeleteCodingOutConnector.deleteCodingOut(request)
          .returns(Future.successful(outcome))

        await(service.deleteCodingOut(request)) shouldBe outcome
      }
    }

    "map errors according to spec" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the service" in new Test {

          MockDeleteCodingOutConnector.deleteCodingOut(request)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.deleteCodingOut(request)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        ("INVALID_TAXABLE_ENTITY_ID", NinoFormatError),
        ("INVALID_TAX_YEAR", TaxYearFormatError),
        ("INVALID_CORRELATIONID", DownstreamError),
        ("NO_DATA_FOUND", CodingOutNotFoundError),
        ("SERVER_ERROR", DownstreamError),
        ("SERVICE_UNAVAILABLE", DownstreamError)
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}