/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.models.response.retrieveCodingOut

import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import play.api.libs.functional.syntax._

case class TaxCodeComponentsObject(selfAssessmentUnderpayment: Option[Seq[TaxCodeComponents]],
                                   payeUnderpayment: Option[Seq[TaxCodeComponents]],
                                   debt: Option[Seq[TaxCodeComponents]],
                                   inYearAdjustment: Option[TaxCodeComponents])

object TaxCodeComponentsObject {
  implicit val reads: Reads[TaxCodeComponentsObject] = (
      (JsPath \ "selfAssessmentUnderpayment").readNullable[Seq[TaxCodeComponents]] and
      (JsPath \ "payeUnderpayment").readNullable[Seq[TaxCodeComponents]] and
      (JsPath \ "debt").readNullable[Seq[TaxCodeComponents]] and
      (JsPath \ "inYearAdjustment").readNullable[TaxCodeComponents]
    )(TaxCodeComponentsObject.apply _)

  implicit val writes: OWrites[TaxCodeComponentsObject] = Json.writes[TaxCodeComponentsObject]
}
