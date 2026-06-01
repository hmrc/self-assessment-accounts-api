/*
 * Copyright 2026 HM Revenue & Customs
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

package v4.retrieveItsaPenalties.def1.model.response

import play.api.libs.functional.syntax.*
import play.api.libs.json.*

case class LateSubmissions(lateSubmissionId: String,
                           incomeSource: Option[String],
                           taxPeriod: Option[String],
                           taxReturnStatus: Option[String],
                           taxPeriodStartDate: Option[String],
                           taxPeriodEndDate: Option[String],
                           taxPeriodDueDate: Option[String],
                           returnReceiptDate: Option[String])

object LateSubmissions {

  implicit val reads: Reads[LateSubmissions] = (
    (JsPath \ "lateSubmissionID").read[String] and
      (JsPath \ "incomeSource").readNullable[String] and
      (JsPath \ "taxPeriod").readNullable[String] and
      (JsPath \ "taxReturnStatus").readNullable[String].map(_.map(_.toLowerCase)) and
      (JsPath \ "taxPeriodStartDate").readNullable[String] and
      (JsPath \ "taxPeriodEndDate").readNullable[String] and
      (JsPath \ "taxPeriodDueDate").readNullable[String] and
      (JsPath \ "returnReceiptDate").readNullable[String]
  )(LateSubmissions.apply)

  implicit val writes: OWrites[LateSubmissions] = Json.writes[LateSubmissions]
}
