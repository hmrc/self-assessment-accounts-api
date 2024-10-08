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

package v2.fixtures.retrieveBalanceAndTransactions

import play.api.libs.json.{JsValue, Json}
import v2.fixtures.retrieveBalanceAndTransactions.BalanceDetailsFixture._
import v2.fixtures.retrieveBalanceAndTransactions.CodingDetailsFixture.{
  codingDetails,
  codingDetailsDownstreamResponseJson,
  codingDetailsMtdResponseJson
}
import v2.fixtures.retrieveBalanceAndTransactions.DocumentDetailsFixture.{
  documentDetails,
  documentDetailsDownstreamResponseJson,
  documentDetailsMtdResponseJson
}
import v2.fixtures.retrieveBalanceAndTransactions.FinancialDetailsFixture._
import v2.models.response.retrieveBalanceAndTransactions.{FinancialDetails, RetrieveBalanceAndTransactionsResponse}

object ResponseFixture {

  val response: RetrieveBalanceAndTransactionsResponse             = responseWith(financialDetailsFull)
  val responseWithoutLocks: RetrieveBalanceAndTransactionsResponse = responseWith(financialDetailsWithoutLocks)

  val minimalResponse: RetrieveBalanceAndTransactionsResponse =
    RetrieveBalanceAndTransactionsResponse(
      minimalBalanceDetails,
      None,
      None,
      None
    )

  val mtdResponseJson: JsValue             = mtdResponseJsonWith(mtdFinancialDetailsFullJson)
  val mtdResponseWithoutLocksJson: JsValue = mtdResponseJsonWith(mtdFinancialDetailsWithoutLocksJson)

  val downstreamResponseJson: JsValue = Json.parse(s"""
        |  {
        |    "balanceDetails": $balanceDetailsDownstreamResponseJson,
        |    "codingDetails": [
        |        $codingDetailsDownstreamResponseJson
        |    ],
        |    "documentDetails": [
        |        $documentDetailsDownstreamResponseJson
        |    ],
        |    "financialDetails": [
        |        $downstreamFinancialDetailsFullJson
        |    ]
        |  }
        |""".stripMargin)

  val minimalDownstreamResponseJson: JsValue = Json.parse(s"""
        |  {
        |    "balanceDetails": $minimalBalanceDetailsDownstreamResponseJson
        |  }
        |""".stripMargin)

  private def responseWith(financialDetails: FinancialDetails) =
    RetrieveBalanceAndTransactionsResponse(
      balanceDetails,
      Some(List(codingDetails)),
      Some(List(documentDetails)),
      Some(List(financialDetails))
    )

  private def mtdResponseJsonWith(financialDefails: JsValue) = Json.parse(s"""
       |  {
       |    "balanceDetails": $balanceDetailsMtdResponseJson,
       |    "codingDetails": [
       |        $codingDetailsMtdResponseJson
       |    ],
       |    "documentDetails": [
       |        $documentDetailsMtdResponseJson
       |    ],
       |    "financialDetails": [
       |        $financialDefails
       |    ]
       |  }
       |""".stripMargin)

}
