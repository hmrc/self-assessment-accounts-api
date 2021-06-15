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

package v1.models.request.createOrAmendCodingOut

import play.api.libs.json.{Json, OFormat}

case class TaxCodeComponents (payeUnderpayment: Option[Seq[TaxCodeComponent]],
                              selfAssessmentUnderpayment: Option[Seq[TaxCodeComponent]],
                              debt: Option[Seq[TaxCodeComponent]],
                              inYearAdjustment: Option[TaxCodeComponent]) {
  def isEmpty: Boolean = payeUnderpayment.isEmpty && selfAssessmentUnderpayment.isEmpty && debt.isEmpty && inYearAdjustment.isEmpty
}

object TaxCodeComponents {
  implicit val format: OFormat[TaxCodeComponents] = Json.format[TaxCodeComponents]
}
