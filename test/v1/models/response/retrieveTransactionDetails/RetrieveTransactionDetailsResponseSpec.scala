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
import v1.fixtures.transactionDetails.RetrieveTransactionDetailsResponseFixture

class RetrieveTransactionDetailsResponseSpec extends UnitSpec with RetrieveTransactionDetailsResponseFixture {

  "RetrieveTransactionDetailsResponse" when {
    "read from valid JSON (charge)" should {
      "produce the expected RetrieveTransactionDetailsResponse object" in {
        desJsonCharge.as[RetrieveTransactionDetailsResponse] shouldBe responseModelCharge
      }
    }

    "read from valid JSON (payment)" should {
      "produce the expected RetrieveTransactionDetailsResponse object" in {
        desJsonPayment.as[RetrieveTransactionDetailsResponse] shouldBe responseModelPayment
      }
    }

    "read from valid JSON with multiple transaction items" should {
      "produce the expected RetrieveTransactionDetailsResponse object" in {
        desJsonMultiple.as[RetrieveTransactionDetailsResponse] shouldBe responseModelMultiple
      }
    }

    "read from valid JSON with an empty transactionItems array" should {
      "produce the expected RetrieveTransactionDetailsResponse object" in {
        desJsonNoTransactions.as[RetrieveTransactionDetailsResponse] shouldBe responseModelNoTransactions
      }
    }

    "read from empty JSON" should {
      "produce a JsError" in {
        desJsonEmpty.validate[RetrieveTransactionDetailsResponse] shouldBe a[JsError]
      }
    }

    "written to JSON (charge)" should {
      "produce the expected JSON" in {
        Json.toJson(responseModelCharge) shouldBe mtdJsonCharge
      }
    }

    "written to JSON (payment)" should {
      "produce the expected JSON" in {
        Json.toJson(responseModelPayment) shouldBe mtdJsonPayment
      }
    }

    "written to JSON (multiple transaction items)" should {
      "produce the expected JSON for a charge" in {
        Json.toJson(responseModelMultiple) shouldBe mtdJsonMultiple
      }
    }

    "written to JSON (empty transaction items array)" should {
      "produce the expected JSON" in {
        Json.toJson(responseModelNoTransactions) shouldBe mtdJsonNoTransactions
      }
    }
  }
}
