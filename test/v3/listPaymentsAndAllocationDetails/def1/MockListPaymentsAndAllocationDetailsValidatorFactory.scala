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

package v3.listPaymentsAndAllocationDetails.def1

import org.scalamock.handlers.CallHandler
import shared.controllers.validators.{MockValidatorFactory, Validator}
import v3.listPaymentsAndAllocationDetails.ListPaymentsAndAllocationDetailsValidatorFactory
import v3.listPaymentsAndAllocationDetails.model.request.ListPaymentsAndAllocationDetailsRequestData

trait MockListPaymentsAndAllocationDetailsValidatorFactory extends MockValidatorFactory[ListPaymentsAndAllocationDetailsRequestData] {

  val mockListPaymentsAndAllocationDetailsValidatorFactory: ListPaymentsAndAllocationDetailsValidatorFactory =
    mock[ListPaymentsAndAllocationDetailsValidatorFactory]

  def validator(): CallHandler[Validator[ListPaymentsAndAllocationDetailsRequestData]] =
    (mockListPaymentsAndAllocationDetailsValidatorFactory
      .validator(_: String, _: Option[String], _: Option[String], _: Option[String], _: Option[String]))
      .expects(*, *, *, *, *)

}
