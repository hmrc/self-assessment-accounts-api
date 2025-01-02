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

package v3.optInToCodingOut

import api.controllers.validators.MockValidatorFactory
import org.scalamock.handlers.CallHandler
import v3.optInToCodingOut.model.request.OptInToCodingOutRequestData

trait MockOptInToCodingOutValidatorFactory extends MockValidatorFactory[OptInToCodingOutRequestData] {

  val mockOptInToCodingOutValidatorFactory: OptInToCodingOutValidatorFactory = mock[OptInToCodingOutValidatorFactory]

  def validator(): CallHandler[Validator[OptInToCodingOutRequestData]] =
    (mockOptInToCodingOutValidatorFactory.validator(_: String, _: String)).expects(*, *)

}
