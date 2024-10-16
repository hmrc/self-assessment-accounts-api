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

package v3.retrieveBalanceAndTransactions.def1.model.response

import config.FeatureSwitches
import play.api.libs.json.{Json, OWrites, Reads}
import v3.retrieveBalanceAndTransactions.model.response.RetrieveBalanceAndTransactionsResponse

case class Def1_RetrieveBalanceAndTransactionsResponse(
    balanceDetails: BalanceDetails,
    codingDetails: Option[Seq[CodingDetails]],
    documentDetails: Option[Seq[DocumentDetails]],
    financialDetails: Option[Seq[FinancialDetails]]
) extends RetrieveBalanceAndTransactionsResponse{

  def adjustFields(featureSwitches: FeatureSwitches): Def1_RetrieveBalanceAndTransactionsResponse = {

    def documentDetailAdditionalField(response: Def1_RetrieveBalanceAndTransactionsResponse): Def1_RetrieveBalanceAndTransactionsResponse = {
      if (featureSwitches.isPOARelevantAmountEnabled) response else response.withoutPOARelevantAmountField
    }
    documentDetailAdditionalField(this)
  }

  def withoutPOARelevantAmountField: Def1_RetrieveBalanceAndTransactionsResponse =
    this.copy(documentDetails = documentDetails.map(_.map(_.copy(poaRelevantAmount = None))))

}

object Def1_RetrieveBalanceAndTransactionsResponse {

  implicit def reads(implicit readLocks: FinancialDetailsItem.ReadLocks): Reads[Def1_RetrieveBalanceAndTransactionsResponse] =
    Json.reads[Def1_RetrieveBalanceAndTransactionsResponse]

  implicit val writes: OWrites[Def1_RetrieveBalanceAndTransactionsResponse] = Json.writes[Def1_RetrieveBalanceAndTransactionsResponse]
}
