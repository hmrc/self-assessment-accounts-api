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

package v4.retrieveBalanceAndTransactions.def1.model.response

import play.api.libs.json.{JsError, JsObject, JsValue, Json}
import shared.utils.UnitSpec
import v4.retrieveBalanceAndTransactions.def1.model.FinancialDetailsItemFixture

class FinancialDetailsItemSpec extends UnitSpec with FinancialDetailsItemFixture {

  "FinancialDetailsItem" when {
    "written to MTD JSON" must {
      "work" in {
        Json.toJson(financialDetailsItem) shouldBe financialDetailsItemMtdJson
      }
    }

    "read from downstream" when {
      "reading locks" when {
        implicit val readLocks: FinancialDetailsItem.ReadLocks = FinancialDetailsItem.ReadLocks(true)

        "all properties are present and the feature switch is disabled (IFS enabled)" must {
          "include locks in the object" in {
            financialDetailsItemDownstreamJson.as[FinancialDetailsItem] shouldBe financialDetailsItem
          }
        }

        "all properties are present and the feature switch is enabled (HIP enabled)" must {
          "include locks in the object" in {
            financialDetailsItemDownstreamHipJson.as[FinancialDetailsItem] shouldBe financialDetailsItemHip
          }
        }

        "no mandatory properties are present" must {
          "return an empty object except for a locks field with false values" in {
            JsObject.empty.as[FinancialDetailsItem] shouldBe financialDetailsItemEmpty.copy(locks = Some(financialDetailsItemLocksFalse))
          }
        }
      }

      "not reading locks" when {
        implicit val readLocks: FinancialDetailsItem.ReadLocks = FinancialDetailsItem.ReadLocks(false)

        "all properties are present and the feature switch is disabled (IFS enabled)" must {
          "not include locks field" in {
            financialDetailsItemDownstreamJson.as[FinancialDetailsItem] shouldBe financialDetailsItem.copy(locks = None)
          }
        }

        "all properties are present and the feature switch is enabled (HIP enabled)" must {
          "not include locks field" in {
            financialDetailsItemDownstreamHipJson.as[FinancialDetailsItem] shouldBe financialDetailsItemHip.copy(locks = None)
          }
        }

        "no mandatory properties are present" must {
          "return an empty object" in {
            JsObject.empty.as[FinancialDetailsItem] shouldBe financialDetailsItemEmpty
          }
        }
      }

      "converting individual fields" when {
        // Exclude locks so we can more easily compare just the field of interest...
        implicit val readLocks: FinancialDetailsItem.ReadLocks = FinancialDetailsItem.ReadLocks(false)

        "converting outgoingPaymentMethod" must {
          def json(value: String): JsValue = Json.parse(s"""{ "outgoingPaymentMethod": "$value" }""")

          def model(value: Option[String]): FinancialDetailsItem = financialDetailsItemEmpty.copy(outgoingPaymentMethod = value)

          "convert if present and a mapping is defined" when {
            def doTest(downstreamValue: String, mtdValue: String): Unit =
              s"downstream value is $downstreamValue" in {
                json(downstreamValue).as[FinancialDetailsItem] shouldBe model(Some(mtdValue))
              }

            List("A" -> "Repayment to Card", "P" -> "Payable Order Repayment", "R" -> "BACS Payment out").foreach(doTest.tupled)
          }

          "leave absent if present and no mapping is defined" in {
            json("UNKNOWN").as[FinancialDetailsItem] shouldBe model(None)
          }
        }

        "converting statisticalDocument to isChargeEstimate" must {
          def json(value: String): JsValue = Json.parse(s"""{ "statisticalDocument": "$value" }""")

          def model(value: Option[Boolean]): FinancialDetailsItem = financialDetailsItemEmpty.copy(isChargeEstimate = value)

          "convert Y string to true" in {
            json("Y").as[FinancialDetailsItem] shouldBe model(Some(true))
          }

          "convert G string to true" in {
            json("G").as[FinancialDetailsItem] shouldBe model(Some(true))
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

}
