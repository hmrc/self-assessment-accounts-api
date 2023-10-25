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

package v2.controllers.validators

import api.controllers.validators.Validator
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import play.api.libs.json.JsValue
import v2.models.request.createOrAmendCodingOut.CreateOrAmendCodingOutRequestData

trait MockCreateOrAmendCodingOutValidatorFactory extends MockFactory {

  val mockCreateOrAmendCodingOutValidatorFactory: CreateOrAmendCodingOutValidatorFactory = mock[CreateOrAmendCodingOutValidatorFactory]

  object MockedCreateOrAmendCodingOutValidatorFactory {

    def validator(): CallHandler[Validator[CreateOrAmendCodingOutRequestData]] =
      (mockCreateOrAmendCodingOutValidatorFactory.validator(_: String, _: String, _: JsValue, _: Boolean)).expects(*, *, *, *)

  }

  def willUseValidator(use: Validator[CreateOrAmendCodingOutRequestData]): CallHandler[Validator[CreateOrAmendCodingOutRequestData]] = {
    MockedCreateOrAmendCodingOutValidatorFactory
      .validator()
      .anyNumberOfTimes()
      .returns(use)
  }

  def returningSuccess(result: CreateOrAmendCodingOutRequestData): Validator[CreateOrAmendCodingOutRequestData] =
    new Validator[CreateOrAmendCodingOutRequestData] {
      def validate: Validated[Seq[MtdError], CreateOrAmendCodingOutRequestData] = Valid(result)
    }

  def returning(result: MtdError*): Validator[CreateOrAmendCodingOutRequestData] = returningErrors(result)

  def returningErrors(result: Seq[MtdError]): Validator[CreateOrAmendCodingOutRequestData] = new Validator[CreateOrAmendCodingOutRequestData] {
    def validate: Validated[Seq[MtdError], CreateOrAmendCodingOutRequestData] = Invalid(result)
  }

}
