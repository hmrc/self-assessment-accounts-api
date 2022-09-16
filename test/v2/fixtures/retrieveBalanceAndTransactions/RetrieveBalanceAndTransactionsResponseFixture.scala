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

package v2.fixtures.retrieveBalanceAndTransactions

import play.api.libs.json.{JsValue, Json}
import v2.models.response.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsResponse

trait RetrieveBalanceAndTransactionsResponseFixture extends BalanceDetailsFixture with DocumentDetailsFixture with FinancialDetailsFixture {

  val responseModel: RetrieveBalanceAndTransactionsResponse = RetrieveBalanceAndTransactionsResponse(
    balanceDetails,
    Some(Seq(documentDetails)),
    Some(Seq(financialDetails))
  )

  val responseMtdJson: JsValue = Json.parse(s"""
      |{
      |  "balanceDetails": $balanceDetailsMtdJson,
      |  "documentDetails": [$documentDetailsMtdJson],
      |  "financialDetails": [$financialDetailsMtdJson]
      |}""".stripMargin)

  val responseDownstreamJson: JsValue = Json.parse(s"""
       |{
       |  "balanceDetails": $balanceDetailsDownstreamJson,
       |  "documentDetails": [$documentDetailsDownstreamJson],
       |  "financialDetails": [$financialDetailsDownstreamJson]
       |}""".stripMargin)

}
