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

package v1.services

import api.mocks.MockCurrentDate
import api.models.domain.{Nino, TaxYear}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import org.scalamock.handlers.CallHandler
import utils.CurrentDate
import v1.mocks.connectors.MockRetrieveCodingOutConnector
import v1.models.request.retrieveCodingOut.RetrieveCodingOutParsedRequest
import v1.models.response.retrieveCodingOut._

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

class RetrieveCodingOutServiceSpec extends ServiceSpec {

  private val nino = Nino("AA123456A")

  def unmatchedCustomerSubmissions(objectId: Option[BigInt]): UnmatchedCustomerSubmissions =
    UnmatchedCustomerSubmissions(
      0,
      "2021-08-24T14:15:22Z",
      objectId
    )

  def taxCodeComponents(objectId: Option[BigInt]): TaxCodeComponents =
    TaxCodeComponents(
      0,
      Some("2021-22"),
      "2021-08-24T14:15:22Z",
      "hmrcHeld",
      objectId
    )

  def taxCodeComponentObject(objectId: Option[BigInt]): TaxCodeComponentsObject =
    TaxCodeComponentsObject(
      Some(Seq(taxCodeComponents(objectId))),
      Some(Seq(taxCodeComponents(objectId))),
      Some(Seq(taxCodeComponents(objectId))),
      Some(taxCodeComponents(objectId))
    )

  def unmatchedCustomerSubmissionsObject(objectId: Option[BigInt]): UnmatchedCustomerSubmissionsObject =
    UnmatchedCustomerSubmissionsObject(
      Some(Seq(unmatchedCustomerSubmissions(objectId))),
      Some(Seq(unmatchedCustomerSubmissions(objectId))),
      Some(Seq(unmatchedCustomerSubmissions(objectId))),
      Some(unmatchedCustomerSubmissions(objectId))
    )

  def retrieveCodingOutResponse(objectId: Option[BigInt]): RetrieveCodingOutResponse =
    RetrieveCodingOutResponse(
      Some(taxCodeComponentObject(objectId)),
      Some(unmatchedCustomerSubmissionsObject(objectId))
    )

  val requestData: RetrieveCodingOutParsedRequest =
    RetrieveCodingOutParsedRequest(
      nino = nino,
      taxYear = TaxYear.fromMtd("2021-22"),
      source = Some("hmrcHeld")
    )

  trait Test extends MockRetrieveCodingOutConnector with MockCurrentDate {

    implicit val dateProvider: CurrentDate   = mockCurrentDate
    val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    def setupDateProvider(date: String): CallHandler[LocalDate] =
      MockCurrentDate.getCurrentDate
        .returns(LocalDate.parse(date, dateTimeFormatter))
        .anyNumberOfTimes()

    val service = new RetrieveCodingOutService(
      connector = mockRetrieveCodingOutConnector
    )

  }

  "RetrieveCodingOutService" when {
    "service call successful" must {
      "return mapped result" in new Test {
        setupDateProvider("2022-04-06")

        val connectorResponse: RetrieveCodingOutResponse = retrieveCodingOutResponse(Some(BigInt(12345678910L)))

        MockRetrieveCodingOutConnector
          .retrieveCodingOut(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, connectorResponse))))

        await(service.retrieveCodingOut(requestData)) shouldBe Right(ResponseWrapper(correlationId, connectorResponse))
      }

    }

    "validateCodingOutResponse" must {
      "return a success if the tax year hasn't ended and there are ids present" in new Test {
        setupDateProvider("2021-04-06")

        val connectorResponse: RetrieveCodingOutResponse = retrieveCodingOutResponse(Some(BigInt(12345678910L)))

        MockRetrieveCodingOutConnector
          .retrieveCodingOut(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, connectorResponse))))

        await(service.retrieveCodingOut(requestData)) shouldBe Right(ResponseWrapper(correlationId, connectorResponse))

      }
      "return a success if the tax year hasn't ended and there aren't ids present" in new Test {
        setupDateProvider("2021-04-06")

        val connectorResponse: RetrieveCodingOutResponse = retrieveCodingOutResponse(None)

        MockRetrieveCodingOutConnector
          .retrieveCodingOut(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, connectorResponse))))

        await(service.retrieveCodingOut(requestData)) shouldBe Right(ResponseWrapper(correlationId, connectorResponse))

      }
      "return a failure if the tax year has ended and there no ideas present in the body" in new Test {
        setupDateProvider("2022-04-06")

        val connectorResponse: RetrieveCodingOutResponse = retrieveCodingOutResponse(None)

        MockRetrieveCodingOutConnector
          .retrieveCodingOut(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, connectorResponse))))

        await(service.retrieveCodingOut(requestData)) shouldBe Left(ErrorWrapper(correlationId, InternalError))

      }
    }

    "unsuccessful" must {
      "map errors according to spec" when {

        def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
          s"a $downstreamErrorCode error is returned from the service" in new Test {

            MockRetrieveCodingOutConnector
              .retrieveCodingOut(requestData)
              .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

            await(service.retrieveCodingOut(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
          }

        val errors: Seq[(String, MtdError)] = Seq(
          "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
          "INVALID_TAX_YEAR"          -> TaxYearFormatError,
          "INVALID_VIEW"              -> SourceFormatError,
          "INVALID_CORRELATIONID"     -> InternalError,
          "NO_DATA_FOUND"             -> CodingOutNotFoundError,
          "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError,
          "SERVER_ERROR"              -> InternalError,
          "SERVICE_UNAVAILABLE"       -> InternalError
        )

        val extraTysErrors: Seq[(String, MtdError)] = Seq(
          "INVALID_CORRELATION_ID" -> InternalError,
          "NOT_FOUND"              -> CodingOutNotFoundError
        )

        (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
      }
    }
  }

}
