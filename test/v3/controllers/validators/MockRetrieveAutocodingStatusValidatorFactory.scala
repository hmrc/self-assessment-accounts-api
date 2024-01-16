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

package v3.controllers.validators

import api.controllers.validators.Validator
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import v3.models.request.retrieveAutocodingStatus.RetrieveAutocodingStatusRequestData

trait MockRetrieveAutocodingStatusValidatorFactory extends MockFactory {

  val mockRetrieveAutocodingStatusValidatorFactory: RetrieveAutocodingStatusValidatorFactory =
    mock[RetrieveAutocodingStatusValidatorFactory]

  object MockRetrieveAutocodingStatusValidatorFactory {

    def validator(): CallHandler[Validator[RetrieveAutocodingStatusRequestData]] =
      (mockRetrieveAutocodingStatusValidatorFactory.validator(_: String, _: String)).expects(*, *)

  }

  def willUseValidator(use: Validator[RetrieveAutocodingStatusRequestData]): CallHandler[Validator[RetrieveAutocodingStatusRequestData]] = {
    MockRetrieveAutocodingStatusValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: RetrieveAutocodingStatusRequestData): Validator[RetrieveAutocodingStatusRequestData] =
    new Validator[RetrieveAutocodingStatusRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveAutocodingStatusRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[RetrieveAutocodingStatusRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[RetrieveAutocodingStatusRequestData] =
    new Validator[RetrieveAutocodingStatusRequestData] {
      def validate: Validated[Seq[MtdError], RetrieveAutocodingStatusRequestData] = Invalid(result)
    }

}
