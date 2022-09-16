/*
 * Copyright 2021 HM Revenue & Customs
 *
 */

package v2.models.response.retrieveBalanceAndTransactions

import play.api.libs.json.{Json, OFormat}

case class RetrieveBalanceAndTransactionsResponse(
    balanceDetails: BalanceDetails,
//codingDetails: Option[Seq[CodingDetails]],
    documentDetails: Option[Seq[DocumentDetails]],
    financialDetails: Option[Seq[FinanceDetails]]
)

object RetrieveBalanceAndTransactionsResponse {
  implicit val format: OFormat[RetrieveBalanceAndTransactionsResponse] = Json.format
}
