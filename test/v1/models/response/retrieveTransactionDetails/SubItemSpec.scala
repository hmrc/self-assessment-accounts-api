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

package v1.models.response.retrieveTransactionDetails

import play.api.libs.json.{JsError, Json}
import support.UnitSpec
import v1.fixtures.transactionDetails.SubItemFixture

class SubItemSpec extends UnitSpec with SubItemFixture {

  "SubItem" when {
    "read from valid JSON (charge)" should {
      "produce the expected SubItem object" in {
        desJsonChargeItem.as[SubItem] shouldBe subItemModelCharge
      }
    }

    "read from valid JSON (payment)" should {
      "produce the expected SubItem object" in {
        desJsonPaymentItem.as[SubItem] shouldBe subItemModelPayment
      }
    }

    "read from empty JSON" should {
      "produce an empty SubItem object" in {
        desJsonEmpty.as[SubItem] shouldBe SubItem.empty
      }
    }

    "read from invalid JSON" should {
      "produce a JsError" in {
        desJsonInvalid.validate[SubItem] shouldBe a[JsError]
      }
    }

    "read from JSON with a non numerical id" should {
      "produce a JsError" in {
        desJsonInvalidId.validate[SubItem] shouldBe a[JsError]
      }
    }

    "written to JSON (charge)" should {
      "produce the expected JSON object for a charge" in {
        Json.toJson(subItemModelCharge) shouldBe mtdJsonChargeItem
      }
    }

    "written to JSON (payment)" should {
      "produce the expected JSON object" in {
        Json.toJson(subItemModelPayment) shouldBe mtdJsonPaymentItem
      }
    }
  }
}
