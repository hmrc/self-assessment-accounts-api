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

package v1.models.response.listCharges

import play.api.libs.json.JsError
import support.UnitSpec
import v1.fixtures.ListChargesFixture._

class ListChargesResponseSpec extends UnitSpec {

  "listChargesResponse" should {
    "return a successful Json model" when {
      "the json contains all fields with a single charge" in {
        listChargesDesJson.as[ListChargesResponse[Charge]] shouldBe fullListSingleChargeModel
      }

      "the json contains all fields with a multiple charges" in {
        fullDesListChargesMultipleResponse.as[ListChargesResponse[Charge]] shouldBe fullListMultipleChargeModel
      }

      "the json contains minimal fields with no charges" in {
        minimalDesListChargesResponse.as[ListChargesResponse[Charge]] shouldBe minimalListChargeModel
      }
    }

    "throw an error" when {
      "there are no mandatory fields" in {
        invalidDesListChargesResponse.validate[ListChargesResponse[Charge]] shouldBe a[JsError]
      }
    }
  }
}
