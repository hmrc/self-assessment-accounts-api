/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.models.response.retrieveTransaction

import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class RetrieveTransactionsResponse[I](transactions: Seq[I])


object RetrieveTransactionsResponse {
  implicit def reads[I: Reads]: Reads[RetrieveTransactionsResponse[I]] =
  implicitly(JsPath \ "transactions").read[Seq[I]].map(items => RetrieveTransactionsResponse(items.filterNot(_ == TransactionItem.empty)))

  implicit def writes[I: OWrites]: OWrites[RetrieveTransactionsResponse[I]] =
  Json.writes[RetrieveTransactionsResponse[I]]
}


