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

package v1.models.response.listCharges

import play.api.libs.json.JsError
import support.UnitSpec
import v1.fixtures.ListChargesFixture._

class ChargeSpec extends UnitSpec {

  "Charge model" should {
    "return a successful Json model" when {
     "the json contains all fields" in {
       fullDesChargeResponse.as[Charge] shouldBe fullChargeModel
     }

      "the json contains only mandatory fields" in {
        minimalDesChargeResponse.as[Charge] shouldBe minimalChargeModel
      }
    }

    "throw an error" when {
      "the json contains a incorrect type" in {
        invalidDesChargeResponse.validate[Charge] shouldBe a[JsError]
      }
    }
  }
}
