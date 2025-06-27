/*
 * Copyright 2025 HM Revenue & Customs
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

package v4.listPaymentsAndAllocationDetails

import shared.connectors.ConnectorSpec
import shared.models.domain.{DateRange, Nino}
import shared.models.outcomes.ResponseWrapper
import uk.gov.hmrc.http.StringContextOps
import v4.listPaymentsAndAllocationDetails.def1.model.request.Def1_ListPaymentsAndAllocationDetailsRequestData
import v4.listPaymentsAndAllocationDetails.def1.model.response.ResponseFixtures.responseObject
import v4.listPaymentsAndAllocationDetails.model.request.ListPaymentsAndAllocationDetailsRequestData
import v4.listPaymentsAndAllocationDetails.model.response.ListPaymentsAndAllocationDetailsResponse

import java.time.LocalDate
import scala.concurrent.Future

class ListPaymentsAndAllocationDetailsConnectorSpec extends ConnectorSpec {

  private val nino           = "AA123456A"
  private val dateFrom       = "2018-08-13"
  private val dateTo         = "2019-08-13"
  private val paymentLot     = "081203010024"
  private val paymentLotItem = "000001"

  private val validRequest: ListPaymentsAndAllocationDetailsRequestData =
    Def1_ListPaymentsAndAllocationDetailsRequestData(
      Nino(nino),
      Some(DateRange(LocalDate.parse(dateFrom), LocalDate.parse(dateTo))),
      Some(paymentLot),
      Some(paymentLotItem))

  trait Test { _: ConnectorTest =>

    val connector: ListPaymentsAndAllocationDetailsConnector =
      new ListPaymentsAndAllocationDetailsConnector(http = mockHttpClient, appConfig = mockSharedAppConfig)

    def connectorRequest(request: ListPaymentsAndAllocationDetailsRequestData,
                         response: ListPaymentsAndAllocationDetailsResponse,
                         queryParams: Seq[(String, String)]): Unit = {

      val outcome = Right(ResponseWrapper(correlationId, response))

      willGet(
        url = url"$baseUrl/cross-regime/payment-allocation/NINO/$nino/ITSA",
        parameters = queryParams
      ).returns(Future.successful(outcome))

      val result = await(connector.listPaymentsAndAllocationDetails(request))
      result shouldBe outcome
    }

  }

  "ListPaymentsAndAllocationDetailsConnector" should {
    "return a valid response" when {
      "a valid request is supplied" in new DesTest with Test {
        val queryParams: Seq[(String, String)] =
          List(
            "dateFrom"       -> s"$dateFrom",
            "dateTo"         -> s"$dateTo",
            "paymentLot"     -> s"$paymentLot",
            "paymentLotItem" -> s"$paymentLotItem"
          )

        connectorRequest(validRequest, responseObject, queryParams)
      }
    }
  }

}
