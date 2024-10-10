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

package v3.listPaymentsAndAllocationDetails.model.request

import api.models.domain.{DateRange, Nino}
import v3.listPaymentsAndAllocationDetails.ListPaymentsAndAllocationDetailsSchema

trait ListPaymentsAndAllocationDetailsRequestData {
  val nino: Nino
  val fromAndToDates: Option[DateRange]
  val paymentLot: Option[String]
  val paymentLotItem: Option[String]

  val schema: ListPaymentsAndAllocationDetailsSchema

}
