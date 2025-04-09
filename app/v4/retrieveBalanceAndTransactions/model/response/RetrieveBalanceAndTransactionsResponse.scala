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

package v4.retrieveBalanceAndTransactions.model.response

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v4.retrieveBalanceAndTransactions.def1.model.response._

case class RetrieveBalanceAndTransactionsResponse(
    balanceDetails: BalanceDetails,
    codingDetails: Option[Seq[CodingDetails]],
    documentDetails: Option[Seq[DocumentDetails]],
    financialDetails: Option[Seq[FinancialDetails]]
)

object RetrieveBalanceAndTransactionsResponse {

  private def innerReads(implicit readLocks: FinancialDetailsItem.ReadLocks): Reads[RetrieveBalanceAndTransactionsResponse] =
    (
      (JsPath \ "balanceDetails").read[BalanceDetails] and
        (JsPath \ "codingDetails").readNullable[Seq[CodingDetails]] and
        (JsPath \ "documentDetails").readNullable[Seq[DocumentDetails]] and
        (JsPath \ "financialDetails").read[Seq[FinancialDetails]].map(fd => Option(fd))
          .orElse((JsPath \ "financialDetailsItem").readNullable[Seq[FinancialDetails]])
    )(RetrieveBalanceAndTransactionsResponse.apply _)

  // accommodating HIP response being wrapped in a 'success' object
  implicit def reads(implicit readLocks: FinancialDetailsItem.ReadLocks): Reads[RetrieveBalanceAndTransactionsResponse] =
    (JsPath \ "success").read[RetrieveBalanceAndTransactionsResponse](innerReads).orElse(innerReads)

  implicit val writes: OWrites[RetrieveBalanceAndTransactionsResponse] = Json.writes[RetrieveBalanceAndTransactionsResponse]

}
