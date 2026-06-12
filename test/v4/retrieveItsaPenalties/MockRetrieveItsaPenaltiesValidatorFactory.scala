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

package v4.retrieveItsaPenalties

import org.scalamock.handlers.CallHandler
import api.controllers.validators.{MockValidatorFactory, Validator}
import v4.retrieveItsaPenalties.model.request.RetrieveItsaPenaltiesRequestData

trait MockRetrieveItsaPenaltiesValidatorFactory extends MockValidatorFactory[RetrieveItsaPenaltiesRequestData] {

  val mockRetrieveItsaPenaltiesValidatorFactory: RetrieveItsaPenaltiesValidatorFactory =
    mock[RetrieveItsaPenaltiesValidatorFactory]

  def validator(): CallHandler[Validator[RetrieveItsaPenaltiesRequestData]] =
    (mockRetrieveItsaPenaltiesValidatorFactory.validator(_: String)).expects(*)

}
