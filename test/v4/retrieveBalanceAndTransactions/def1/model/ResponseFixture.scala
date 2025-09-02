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

package v4.retrieveBalanceAndTransactions.def1.model

import play.api.libs.json.{JsValue, Json}
import v4.retrieveBalanceAndTransactions.def1.model.BalanceDetailsFixture._
import v4.retrieveBalanceAndTransactions.def1.model.CodingDetailsFixture._
import v4.retrieveBalanceAndTransactions.def1.model.DocumentDetailsFixture._
import v4.retrieveBalanceAndTransactions.def1.model.FinancialDetailsFixture._
import v4.retrieveBalanceAndTransactions.def1.model.response.FinancialDetails
import v4.retrieveBalanceAndTransactions.model.response.RetrieveBalanceAndTransactionsResponse

object ResponseFixture {

  val response: RetrieveBalanceAndTransactionsResponse                = responseWith(financialDetailsFull)
  val responseHip: RetrieveBalanceAndTransactionsResponse             = responseWith(financialDetailsFullHip)
  val responseWithoutLocks: RetrieveBalanceAndTransactionsResponse    = responseWith(financialDetailsWithoutLocks)
  val responseWithoutLocksHip: RetrieveBalanceAndTransactionsResponse = responseWith(financialDetailsWithoutLocksHip)

  val minimalResponse: RetrieveBalanceAndTransactionsResponse =
    RetrieveBalanceAndTransactionsResponse(minimalBalanceDetails, None, None, None)

  val mtdResponseJson: JsValue                         = mtdResponseJsonWith(mtdFinancialDetailsFullJson)
  val mtdResponseJsonHip: JsValue                      = mtdResponseJsonWith(mtdFinancialDetailsFullJsonHip)
  val mtdResponseWithoutPOARelevantAmountJson: JsValue = mtdResponseJsonWithoutPOAAmount(mtdFinancialDetailsFullJson)
  val mtdResponseWithoutLocksJson: JsValue             = mtdResponseJsonWith(mtdFinancialDetailsWithoutLocksJson)
  val mtdResponseWithoutLocksJsonHip: JsValue          = mtdResponseJsonWith(mtdFinancialDetailsWithoutLocksJsonHip)

  val downstreamResponseJson: JsValue = Json.parse(s"""
        |  {
        |    "balanceDetails": $balanceDetailsDownstreamResponseJson,
        |    "codingDetails": [
        |        $codingDetailsDownstreamResponseJson
        |    ],
        |    "documentDetails": [
        |        $documentDetailsDownstreamResponseJson,
        |        $documentDetailsWithoutDocDueDateDownstreamResponseJson
        |    ],
        |    "financialDetails": [
        |        $downstreamFinancialDetailsFullJson
        |    ]
        |  }
        |""".stripMargin)

  val downstreamResponseHipJson: JsValue = Json.parse(s"""
        |{
        |  "success":  {
        |    "balanceDetails": $balanceDetailsDownstreamResponseHipJson,
        |    "codingDetails": [
        |        $codingDetailsDownstreamResponseJson
        |    ],
        |    "documentDetails": [
        |        $documentDetailsDownstreamResponseHipJson,
        |        $documentDetailsWithoutDocDueDateDownstreamResponseHipJson
        |    ],
        |    "financialDetailsItem": [
        |        $downstreamFinancialDetailsFullHipJson
        |    ]
        |  }
        |}
        |""".stripMargin)

  val downstreamResponseWithoutPOAAmountJson: JsValue = Json.parse(s"""
        |  {
        |    "balanceDetails": $balanceDetailsDownstreamResponseJson,
        |    "codingDetails": [
        |        $codingDetailsDownstreamResponseJson
        |    ],
        |    "documentDetails": [
        |        $documentDetailsWithoutPOAAmountDownstreamResponseJson,
        |        $documentDetailsWithoutPOAAmntAndDocDueDateDownstreamResponseJson
        |    ],
        |    "financialDetails": [
        |        $downstreamFinancialDetailsFullJson
        |    ]
        |  }
        |""".stripMargin)

  val downstreamResponseWithoutPOAAmountHipJson: JsValue = Json.parse(s"""
        |{
        |  "success": {
        |    "balanceDetails": $balanceDetailsDownstreamResponseHipJson,
        |    "codingDetails": [
        |        $codingDetailsDownstreamResponseJson
        |    ],
        |    "documentDetails": [
        |        $documentDetailsWithoutPOAAmountDownstreamResponseHipJson,
        |        $documentDetailsWithoutPOAAmntAndDocDueDateDownstreamResponseHipJson
        |    ],
        |    "financialDetailsItem": [
        |        $downstreamFinancialDetailsFullHipJson
        |    ]
        |  }
        |}
        |""".stripMargin)

  val minimalDownstreamResponseJson: JsValue = Json.parse(s"""
        |  {
        |    "balanceDetails": $minimalBalanceDetailsDownstreamResponseJson
        |  }
        |""".stripMargin)

  val minimalDownstreamResponseHipJson: JsValue = Json.parse(s"""
       |{
       |  "success": {
       |    "balanceDetails": $minimalBalanceDetailsDownstreamResponseHipJson
       |  }
       |}
       |""".stripMargin)

  private def responseWith(financialDetails: FinancialDetails) =
    RetrieveBalanceAndTransactionsResponse(
      balanceDetails,
      Some(List(codingDetails)),
      Some(List(documentDetails, documentDetailsWithoutDocDueDate)),
      Some(List(financialDetails))
    )

  private def mtdResponseJsonWith(financialDefails: JsValue) = Json.parse(s"""
       |  {
       |    "balanceDetails": $balanceDetailsMtdResponseJson,
       |    "codingDetails": [
       |        $codingDetailsMtdResponseJson
       |    ],
       |    "documentDetails": [
       |        $documentDetailsMtdResponseJson,
       |        $documentDetailsWithoutDocDueDateMtdResponseJson
       |    ],
       |    "financialDetails": [
       |        $financialDefails
       |    ]
       |  }
       |""".stripMargin)

  private def mtdResponseJsonWithoutPOAAmount(financialDefails: JsValue) = Json.parse(s"""
       |  {
       |    "balanceDetails": $balanceDetailsMtdResponseJson,
       |    "codingDetails": [
       |        $codingDetailsMtdResponseJson
       |    ],
       |    "documentDetails": [
       |        $documentDetailsMtdResponseWithoutPOARelevantAmountJson,
       |        $documentDetailWithoutPoaRelevantAmountAndDocDueDateMtdResponseJson
       |    ],
       |    "financialDetails": [
       |        $financialDefails
       |    ]
       |  }
       |""".stripMargin)

}
