/*
 * Copyright 2022 HM Revenue & Customs
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

package v2.models.response.retrieveBalanceAndTransactions

import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import support.UnitSpec
import v2.fixtures.retrieveBalanceAndTransactions.FinancialDetailsItemFixture

class FinancialDetailsItemSpec extends UnitSpec with FinancialDetailsItemFixture {

  "FinancialDetailsItem" when {
    "written to MTD JSON" must {
      "work" in {
        Json.toJson(financialDetailsItemModel) shouldBe financialDetailsItemMtdJson
      }
    }

    "read from downstream" when {
      "when all properties are present" must {
        "work" in {
          financialDetailsItemDownstreamJson.as[FinancialDetailsItem] shouldBe financialDetailsItemModel
        }
      }

      "when no mandatory properties are present" must {
        "work" in {
          JsObject.empty.as[FinancialDetailsItem] shouldBe financialDetailsItemModelEmpty
        }
      }

      "converting clearingReason" must {
        def json(value: String): JsValue = Json.parse(s"""{ "clearingReason": "$value" }""")

        def model(value: Option[String]): FinancialDetailsItem = financialDetailsItemModelEmpty.copy(clearingReason = value)

        "convert if present and a mapping is defined" when {
          def doTest(downstreamValue: String, mtdValue: String): Unit =
            s"downstream value is $downstreamValue" in {
              json(downstreamValue).as[FinancialDetailsItem] shouldBe model(Some(mtdValue))
            }

          Seq("01" -> "Incoming Payment", "02" -> "Outgoing Payment", "05" -> "Reversal", "06" -> "Manual Clearing", "08" -> "Automatic Clearing")
            .foreach((doTest _).tupled)
        }

        "leave absent if present and no mapping is defined" in {
          json("UNKNOWN").as[FinancialDetailsItem] shouldBe model(None)
        }
      }

      "converting outgoingPaymentMethod" must {
        def json(value: String): JsValue                       = Json.parse(s"""{ "outgoingPaymentMethod": "$value" }""")
        def model(value: Option[String]): FinancialDetailsItem = financialDetailsItemModelEmpty.copy(outgoingPaymentMethod = value)

        "convert if present and a mapping is defined" when {
          def doTest(downstreamValue: String, mtdValue: String): Unit =
            s"downstream value is $downstreamValue" in {
              json(downstreamValue).as[FinancialDetailsItem] shouldBe model(Some(mtdValue))
            }

          Seq("A" -> "Repayment to Card", "P" -> "Payable Order Repayment", "R" -> "BACS Payment out").foreach((doTest _).tupled)
        }

        "leave absent if present and no mapping is defined" in {
          json("UNKNOWN").as[FinancialDetailsItem] shouldBe model(None)
        }
      }

      "converting paymentLock to isChargeOnHold" must {
        def json(value: String): JsValue                = Json.parse(s"""{ "paymentLock": "$value" }""")
        def model(value: Boolean): FinancialDetailsItem = financialDetailsItemModelEmpty.copy(isChargeOnHold = value)

        "convert non-empty string to true" in {
          json("ANYTHING").as[FinancialDetailsItem] shouldBe model(true)
        }

        "convert empty string to false" in {
          json("").as[FinancialDetailsItem] shouldBe model(false)
        }

        "convert absent field to false" in {
          JsObject.empty.as[FinancialDetailsItem] shouldBe model(false)
        }
      }

      "converting clearingLock to isEstimatedChargeOnHold" must {
        def json(value: String): JsValue                = Json.parse(s"""{ "clearingLock": "$value" }""")
        def model(value: Boolean): FinancialDetailsItem = financialDetailsItemModelEmpty.copy(isEstimatedChargeOnHold = value)

        "convert non-empty string to true" in {
          json("ANYTHING").as[FinancialDetailsItem] shouldBe model(true)
        }

        "convert empty string to false" in {
          json("").as[FinancialDetailsItem] shouldBe model(false)
        }

        "convert absent field to false" in {
          JsObject.empty.as[FinancialDetailsItem] shouldBe model(false)
        }
      }

      "converting interestLock to isInterestAccrualOnHold" must {
        def json(value: String): JsValue                = Json.parse(s"""{ "interestLock": "$value" }""")
        def model(value: Boolean): FinancialDetailsItem = financialDetailsItemModelEmpty.copy(isInterestAccrualOnHold = value)

        "convert non-empty string to true" in {
          json("ANYTHING").as[FinancialDetailsItem] shouldBe model(true)
        }

        "convert empty string to false" in {
          json("").as[FinancialDetailsItem] shouldBe model(false)
        }

        "convert absent field to false" in {
          JsObject.empty.as[FinancialDetailsItem] shouldBe model(false)
        }
      }

      "converting dunningLock to isInterestChargeOnHold" must {
        def json(value: String): JsValue = Json.parse(s"""{ "dunningLock": "$value" }""")

        def model(value: Boolean): FinancialDetailsItem = financialDetailsItemModelEmpty.copy(isInterestChargeOnHold = value)

        "convert non-empty string to true" in {
          json("ANYTHING").as[FinancialDetailsItem] shouldBe model(true)
        }

        "convert empty string to false" in {
          json("").as[FinancialDetailsItem] shouldBe model(false)
        }

        "convert absent field to false" in {
          JsObject.empty.as[FinancialDetailsItem] shouldBe model(false)
        }
      }

      "converting statisticalDocument to isChargeEstimate" must {
        def json(value: String): JsValue                = Json.parse(s"""{ "statisticalDocument": "$value" }""")
        def model(value: Option[Boolean]): FinancialDetailsItem = financialDetailsItemModelEmpty.copy(isChargeEstimate = value)

        "convert Y string to true" in {
          json("Y").as[FinancialDetailsItem] shouldBe model(Some(true))
        }

        "convert N string to false" in {
          json("N").as[FinancialDetailsItem] shouldBe model(Some(false))
        }

        "convert any other string to an error" in {
          json("X").validate[FinancialDetailsItem] shouldBe a[JsError]
        }

        "convert absent field to absent field" in {
          JsObject.empty.as[FinancialDetailsItem] shouldBe model(None)
        }
      }
    }
  }

}
