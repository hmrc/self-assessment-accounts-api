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

import play.api.libs.json.{JsError, Json}
import support.UnitSpec
import v2.fixtures.ListChargesFixture._

class ChargeSpec extends UnitSpec {

  "Charge" should {
    "return the expected Json object" when {
      "the json contains all fields" in {
        fullDesChargeResponse.as[Charge] shouldBe fullCharge
      }

      "successfully write the model to Json" when {
        "using a standard Json Owrites" in {
          Json.toJson(fullCharge) shouldBe fullChargeMtdResponse
        }
      }

      "throw an error" when {
        "the json contains a incorrect type" in {
          invalidDesChargeResponse.validate[Charge] shouldBe a[JsError]
        }
      }
    }
  }

}
