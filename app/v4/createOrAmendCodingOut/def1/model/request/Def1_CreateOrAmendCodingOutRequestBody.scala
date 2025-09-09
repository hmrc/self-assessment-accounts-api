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

package v4.createOrAmendCodingOut.def1.model.request

import play.api.libs.json.{Json, OFormat}
import shared.utils.EmptinessChecker
import v4.createOrAmendCodingOut.model.request.CreateOrAmendCodingOutRequestBody

case class Def1_CreateOrAmendCodingOutRequestBody(taxCodeComponents: TaxCodeComponents) extends CreateOrAmendCodingOutRequestBody {

  def getEmptyFieldName(seqO: Option[Seq[TaxCodeComponent]], fieldName: String): Seq[String] =
    seqO.map(seq => if (seq.isEmpty) Seq(s"/taxCodeComponents/$fieldName") else Seq.empty).getOrElse(Seq.empty)

  def emptyFields: Seq[String] =
    if (taxCodeComponents.isEmpty) {
      List("/taxCodeComponents")
    } else {
      getEmptyFieldName(seqO = taxCodeComponents.payeUnderpayment, fieldName = "payeUnderpayment") ++
        getEmptyFieldName(seqO = taxCodeComponents.selfAssessmentUnderpayment, fieldName = "selfAssessmentUnderpayment") ++
        getEmptyFieldName(seqO = taxCodeComponents.debt, fieldName = "debt")
    }

}

object Def1_CreateOrAmendCodingOutRequestBody {
  implicit val format: OFormat[Def1_CreateOrAmendCodingOutRequestBody] = Json.format[Def1_CreateOrAmendCodingOutRequestBody]
  given EmptinessChecker[Def1_CreateOrAmendCodingOutRequestBody]       = EmptinessChecker.derived

}
