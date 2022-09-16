/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v2.fixtures.retrieveBalanceAndTransactions

import play.api.libs.json.{JsValue, Json}
import v2.models.response.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsResponse

trait RetrieveBalanceAndTransactionsResponseFixture extends BalanceDetailsFixture with DocumentDetailsFixture with FinancialDetailsFixture {

  val responseModel: RetrieveBalanceAndTransactionsResponse = RetrieveBalanceAndTransactionsResponse(
    balanceDetailsObject,
    Some(Seq(documentDetails)),
    Some(Seq(financialDetailsFullObject))
  )

  val responseMtdJson: JsValue = Json.parse(s"""
      |{
      |  "balanceDetails": $mtdResponseJson,
      |  "documentDetails": [$mtdDocumentDetailsJson],
      |  "financialDetails": [$mtdFinancialDetailsFullJson]
      |}""".stripMargin)

  val responseDownstreamJson: JsValue = Json.parse(s"""
       |{
       |  "balanceDetails": $downstreamResponseJson,
       |  "documentDetails": [$downstreamDocumentDetailsJson],
       |  "financialDetails": [$downstreamFinancialDetailsFullJson]
       |}""".stripMargin)

}
