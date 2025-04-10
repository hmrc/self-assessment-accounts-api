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

package v4.retrieveBalanceAndTransactions.def1.model.response

import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{JsPath, Json, Reads, Writes}

case class ChargeDetail(documentId: String,
                        documentType: Option[String],
                        documentTypeDescription: Option[String],
                        chargeType: Option[String],
                        chargeTypeDescription: Option[String])

object ChargeDetail {
  implicit val writes: Writes[ChargeDetail] = Json.writes[ChargeDetail]

  implicit val reads: Reads[ChargeDetail] =
    ((JsPath \ "documentId").read[String].orElse((JsPath \ "documentID").read[String]) and
    (JsPath \ "mainTransaction").readNullable[String] and
    (JsPath \ "mainType").readNullable[String] and
    (JsPath \ "subTransaction").readNullable[String] and
    (JsPath \ "chargeType").readNullable[String])(ChargeDetail.apply _)

}
