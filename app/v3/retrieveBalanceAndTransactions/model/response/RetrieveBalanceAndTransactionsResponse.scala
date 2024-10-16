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

package v3.retrieveBalanceAndTransactions.model.response

import play.api.libs.json.{JsObject, Json, OWrites, Reads}
import utils.JsonWritesUtil.writesFrom
import v3.retrieveBalanceAndTransactions.def1.model.response._

trait RetrieveBalanceAndTransactionsResponse

object RetrieveBalanceAndTransactionsResponse {

  implicit def reads: Reads[RetrieveBalanceAndTransactionsResponse] =
    Json.reads[RetrieveBalanceAndTransactionsResponse]

  implicit val writes: OWrites[RetrieveBalanceAndTransactionsResponse] = writesFrom {
    case def1: Def1_RetrieveBalanceAndTransactionsResponse => Json.toJson(def1).as[JsObject]

  }
}
