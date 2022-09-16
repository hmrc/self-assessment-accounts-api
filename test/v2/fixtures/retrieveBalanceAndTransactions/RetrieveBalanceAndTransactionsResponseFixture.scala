/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v2.fixtures.retrieveBalanceAndTransactions

import play.api.libs.json.{JsValue, Json}
import v2.models.response.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsResponse

trait RetrieveBalanceAndTransactionsResponseFixture extends BalanceDetailsFixture with DocumentDetailsFixture with FinanceDetailsFixture {

  val responseModel: RetrieveBalanceAndTransactionsResponse = RetrieveBalanceAndTransactionsResponse(
    balanceDetailsObject,
    Some(Seq(documentDetails)),
    Some(Seq(financeDetailsFullObject))
  )

  val responseMtdJson: JsValue = Json.parse(s"""
      |{
      |  "balanceDetails": $mtdResponseJson,
      |  "documentDetails": [$mtdDocumentDetailsJson],
      |  "financialDetails": [$mtdFinanceDetailFullJson]
      |}""".stripMargin)

  val responseDownstreamJson: JsValue = Json.parse(s"""
       |{
       |  "balanceDetails": $downstreamResponseJson,
       |  "documentDetails": [$downstreamDocumentDetailsJson],
       |  "financialDetails": [$downstreamFinanceDetailFullJson]
       |}""".stripMargin)

}
