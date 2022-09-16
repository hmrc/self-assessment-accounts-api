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

import play.api.libs.json.{JsObject, JsValue, Json}
import support.UnitSpec
import v2.fixtures.retrieveBalanceAndTransactions.FinancialDetailsItemFixture

class FinancialDetailsItemSpec extends UnitSpec with FinancialDetailsItemFixture {

  "FinancialDetailsItem" when {
    "written to MTD JSON" must {
      "work" in {
        Json.toJson(financialDetailsItem) shouldBe financialDetailsItemMtdJson
      }
    }

    "read from downstream" when {
      "when all properties are present" must {
        "work" in {
          financialDetailsItemDownstreamJson.as[FinancialDetailsItem] shouldBe financialDetailsItem
        }
      }

      "when no mandatory properties are present" must {
        "work" in {
          JsObject.empty.as[FinancialDetailsItem] shouldBe financialDetailsItemEmpty
        }
      }

      "converting outgoingPaymentMethod" must {
        def json(value: String): JsValue                       = Json.parse(s"""{ "outgoingPaymentMethod": "$value" }""")
        def model(value: Option[String]): FinancialDetailsItem = financialDetailsItemEmpty.copy(outgoingPaymentMethod = value)

        "convert if present and a mapping is defined" when {
          def doTest(downstreamValue: String, mtdValue: String): Unit =
            s"downstream value is $downstreamValue" in {
              json(downstreamValue).as[FinancialDetailsItem] shouldBe model(Some(mtdValue))
            }

          Seq("A" -> "Repayment to Card", "P" -> "Payable Order Repayment", "R" -> "BACS Payment out").foreach((doTest _).tupled)
        }

        "leave absent if present and no mapping is defined" in { // FIXME is this right?
          json("UNKNOWN").as[FinancialDetailsItem] shouldBe model(None)
        }
      }

      "converting paymentLock" must {
        def json(value: String): JsValue                       = Json.parse(s"""{ "paymentLock": "$value" }""")
        def model(value: Option[String]): FinancialDetailsItem = financialDetailsItemEmpty.copy(paymentLock = value)

        "convert if present and a mapping is defined" when {
          def doTest(downstreamValue: String, mtdValue: String): Unit =
            s"downstream value is $downstreamValue" in {
              json(downstreamValue).as[FinancialDetailsItem] shouldBe model(Some(mtdValue))
            }

          Seq("K" -> "Additional Security Checks").foreach((doTest _).tupled)
        }

        "leave absent if present and no mapping is defined" in { // FIXME is this right?
          json("UNKNOWN").as[FinancialDetailsItem] shouldBe model(None)
        }
      }

      "converting clearingLock" must {
        def json(value: String): JsValue                       = Json.parse(s"""{ "clearingLock": "$value" }""")
        def model(value: Option[String]): FinancialDetailsItem = financialDetailsItemEmpty.copy(clearingLock = value)

        "convert if present and a mapping is defined" when {
          def doTest(downstreamValue: String, mtdValue: String): Unit =
            s"downstream value is $downstreamValue" in {
              json(downstreamValue).as[FinancialDetailsItem] shouldBe model(Some(mtdValue))
            }

          Seq("0" -> "No Reallocation").foreach((doTest _).tupled)
        }

        "leave absent if present and no mapping is defined" in { // FIXME is this right?
          json("UNKNOWN").as[FinancialDetailsItem] shouldBe model(None)
        }
      }

      "converting statisticalDocument to isStatistical" must {
        def json(value: String): JsValue                        = Json.parse(s"""{ "statisticalDocument": "$value" }""")
        def model(value: Option[Boolean]): FinancialDetailsItem = financialDetailsItemEmpty.copy(isStatistical = value)

        "convert G to true" in {
          json("G").as[FinancialDetailsItem] shouldBe model(Some(true))
        }

        "set false if present and no mapping is defined" in { // FIXME is this right?
          json("UNKNOWN").as[FinancialDetailsItem] shouldBe model(Some(false))
        }
      }
    }
  }

}
