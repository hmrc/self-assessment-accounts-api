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

package v3.listPaymentsAndAllocationDetails

import api.controllers.validators.Validator
import v3.listPaymentsAndAllocationDetails.def1.Def1_ListPaymentsAndAllocationDetailsValidator
import v3.listPaymentsAndAllocationDetails.model.request.ListPaymentsAndAllocationDetailsRequestData

import javax.inject.{Inject, Singleton}

@Singleton
class ListPaymentsAndAllocationDetailsValidatorFactory @Inject() {

  def validator(nino: String, fromDate: Option[String], toDate: Option[String], paymentLot: Option[String], paymentLotItem: Option[String]):
  Validator[ListPaymentsAndAllocationDetailsRequestData] = {

    val schema = ListPaymentsAndAllocationDetailsSchema.schemaFor(fromDate, toDate)

    schema match {
      case ListPaymentsAndAllocationDetailsSchema.Def1 => new Def1_ListPaymentsAndAllocationDetailsValidator(nino, fromDate, toDate, paymentLot, paymentLotItem)
    }
  }

}
