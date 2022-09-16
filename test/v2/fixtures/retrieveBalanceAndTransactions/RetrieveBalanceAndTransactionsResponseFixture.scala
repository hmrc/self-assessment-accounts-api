/*
 * Copyright 2021 HM Revenue & Customs
 *
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
