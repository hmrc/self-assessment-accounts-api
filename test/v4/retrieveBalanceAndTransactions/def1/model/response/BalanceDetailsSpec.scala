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

import play.api.libs.json.Json
import shared.utils.UnitSpec
import v4.retrieveBalanceAndTransactions.def1.model.BalanceDetailsFixture._

class BalanceDetailsSpec extends UnitSpec {

  "reads" when {
    "the feature switch is disabled (IFS enabled)" should {
      "return a BalanceDetails object" when {
        "passed a valid JSON document" in {
          balanceDetailsDownstreamResponseJson
            .as[BalanceDetails] shouldBe balanceDetails
        }
      }

      "filter out bcdBalancePerYear entries that have no amount or no taxYear" in {
        val balanceDetails = Json
          .parse(s"""
               |{
               |    "balanceDueWithin30Days": 123,
               |    "balanceNotDueIn30Days": 123,
               |    "totalBalance": 123,
               |    "overDueAmount": 123,
               |    "bcdBalancePerYear": [
               |      {
               |       "amount": 1,
               |       "taxYear": "2021"
               |      },
               |      {
               |       "taxYear": "2022"
               |      },
               |      {
               |       "amount": 3
               |      },
               |      {
               |       "amount": 4,
               |       "taxYear": "2024"
               |      }
               |    ]
               |  }
               |""".stripMargin)
          .as[BalanceDetails]

        balanceDetails.bcdBalancePerYear shouldBe List(
          BalancePerYear(bcdAmount = 1, taxYear = "2020-21"),
          BalancePerYear(bcdAmount = 4, taxYear = "2023-24")
        )
      }

      "convert an absent bcdBalancePerYear to empty sequence" in {
        val balanceDetails = Json
          .parse(s"""
               |{
               |    "balanceDueWithin30Days": 123,
               |    "balanceNotDueIn30Days": 123,
               |    "totalBalance": 123,
               |    "overDueAmount": 123
               |}
               |""".stripMargin)
          .as[BalanceDetails]

        balanceDetails.bcdBalancePerYear shouldBe Nil
      }
    }
    "the feature switch is enabled (HIP enabled)" should {
      "return a BalanceDetails object" when {
        "passed a valid JSON document" in {
          balanceDetailsDownstreamResponseHipJson
            .as[BalanceDetails] shouldBe balanceDetails
        }
      }

      "filter out bcdBalancePerYear entries that have no amount or no taxYear" in {
        val balanceDetails = Json
          .parse(s"""
                    |{
                    |    "balanceDueWithin30days": 123,
                    |    "balanceNotDueIn30Days": 123,
                    |    "totalBalance": 123,
                    |    "overDueAmount": 123,
                    |    "bcdBalancePerYear": [
                    |      {
                    |       "amount": 1,
                    |       "taxYear": "2021"
                    |      },
                    |      {
                    |       "taxYear": "2022"
                    |      },
                    |      {
                    |       "amount": 3
                    |      },
                    |      {
                    |       "amount": 4,
                    |       "taxYear": "2024"
                    |      }
                    |    ]
                    |  }
                    |""".stripMargin)
          .as[BalanceDetails]

        balanceDetails.bcdBalancePerYear shouldBe List(
          BalancePerYear(bcdAmount = 1, taxYear = "2020-21"),
          BalancePerYear(bcdAmount = 4, taxYear = "2023-24")
        )
      }

      "convert an absent bcdBalancePerYear to empty sequence" in {
        val balanceDetails = Json
          .parse(s"""
                    |{
                    |    "balanceDueWithin30days": 123,
                    |    "balanceNotDueIn30Days": 123,
                    |    "totalBalance": 123,
                    |    "overDueAmount": 123
                    |}
                    |""".stripMargin)
          .as[BalanceDetails]

        balanceDetails.bcdBalancePerYear shouldBe Nil
      }
    }
  }

  "writes" when {
    "passed a BalanceDetails object" should {
      "return valid JSON" in {
        Json.toJson(balanceDetails) shouldBe balanceDetailsMtdResponseJson
      }
    }
  }

}
