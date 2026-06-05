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

package v4.retrieveItsaPenalties

import shared.models.domain.Nino
import shared.models.errors.*
import shared.models.outcomes.ResponseWrapper
import shared.services.ServiceSpec
import v4.retrieveItsaPenalties.model.request.RetrieveItsaPenaltiesRequestData
import v4.retrieveItsaPenalties.model.response.RetrieveItsaPenaltiesFixture.responseModel

import scala.concurrent.Future

class RetrieveItsaPenaltiesServiceSpec extends ServiceSpec {

  private val nino: Nino = Nino("AA123456A")

  private val requestData: RetrieveItsaPenaltiesRequestData =
    RetrieveItsaPenaltiesRequestData(nino = nino)

  "RetrieveItsaPenaltiesService" should {
    "return a mapped response when the connector call is successful" in new Test {
      MockRetrieveItsaPenaltiesConnector
        .retrieveItsaPenalties(requestData)
        .returns(Future.successful(Right(ResponseWrapper(correlationId, responseModel))))

      await(service.retrieveItsaPenalties(requestData)) shouldBe Right(ResponseWrapper(correlationId, responseModel))
    }

    "map errors according to spec" when {

      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the service" in new Test {

          MockRetrieveItsaPenaltiesConnector
            .retrieveItsaPenalties(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))))

          await(service.retrieveItsaPenalties(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors: Seq[(String, MtdError)] =
        List("016" -> NinoFormatError, "002" -> InternalError, "015" -> InternalError, "003" -> InternalError, "135" -> InternalError)

      errors.foreach(args => serviceError.tupled(args))
    }

  }

  trait Test extends MockRetrieveItsaPenaltiesConnector {

    val service = new RetrieveItsaPenaltiesService(
      connector = mockRetrieveItsaPenaltiesConnector
    )

  }

}
