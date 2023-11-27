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

package v2.models.response.listCharges

import api.config.MockAppConfig
import api.hateoas.Link
import api.hateoas.Method.GET
import play.api.libs.json.{JsError, Json}
import support.UnitSpec
import v2.fixtures.ListChargesFixture._

class ListChargesResponseSpec extends UnitSpec with MockAppConfig {

  "listChargesResponse" should {
    "return a successful Json model" when {
      "the json contains all fields with a single charge" in {
        listChargesDesJson.as[ListChargesResponse[Charge]] shouldBe fullListSingleCharge
      }

      "the json contains all fields with a multiple charges" in {
        fullDesListChargesMultipleResponse.as[ListChargesResponse[Charge]] shouldBe fullListMultipleCharges
      }
    }

    "successfully write the model to Json" when {
      "using a standard Json Owrites" in {
        Json.toJson(fullListSingleCharge) shouldBe listChargesMtdResponse
      }
    }

    "throw an error" when {
      "there are no mandatory fields" in {
        invalidDesListChargesResponse.validate[ListChargesResponse[Charge]] shouldBe a[JsError]
      }
    }
  }

  "LinksFactory.itemLinks" should {
    "return the expected links" in {
      MockedAppConfig.apiGatewayContext.returns("context").anyNumberOfTimes()

      val result = ListChargesResponse.LinksFactory.itemLinks(
        mockAppConfig,
        ListChargesHateoasData("nino", "from", "to"),
        Charge("2023-24", "transaction-id", "01-01-2024", None, 1.23, 0)
      )

      result shouldBe List(
        Link("/context/nino/transactions/transaction-id", GET, "retrieve-transaction-details")
      )
    }
  }

  "LinksFactory.links" should {
    "return the expected links" in {
      MockedAppConfig.apiGatewayContext.returns("context").anyNumberOfTimes()

      val result = ListChargesResponse.LinksFactory.links(
        mockAppConfig,
        ListChargesHateoasData("nino", "from", "to")
      )

      result shouldBe List(
        Link("/context/nino/charges?from=from&to=to", GET, "self"),
        Link("/context/nino/transactions?from=from&to=to", GET, "list-transactions")
      )
    }
  }

}