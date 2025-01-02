/*
 * Copyright 2024 HM Revenue & Customs
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

import play.api.libs.json.Reads
import shared.schema.DownstreamReadable
import v3.listPaymentsAndAllocationDetails.def1.model.response.Def1_ListPaymentsAndAllocationDetailsResponse
import v3.listPaymentsAndAllocationDetails.model.response.ListPaymentsAndAllocationDetailsResponse

sealed trait ListPaymentsAndAllocationDetailsSchema extends DownstreamReadable[ListPaymentsAndAllocationDetailsResponse]

object ListPaymentsAndAllocationDetailsSchema {

  case object Def1 extends ListPaymentsAndAllocationDetailsSchema {
    type DownstreamResp = Def1_ListPaymentsAndAllocationDetailsResponse
    val connectorReads: Reads[DownstreamResp] = Def1_ListPaymentsAndAllocationDetailsResponse.reads
  }

  private val defaultSchema = Def1

  def schemaFor(fromDate: Option[String], toDate: Option[String]): ListPaymentsAndAllocationDetailsSchema = {
    defaultSchema
  }

}
