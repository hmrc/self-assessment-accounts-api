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

package v1.controllers.requestParsers.validators

import v1.controllers.requestParsers.validators.validations._
import v1.controllers.requestParsers.validators.validations.TaxYearNotEndedValidation
import v1.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError}
import v1.models.request.createOrAmendCodingOut.{CreateOrAmendCodingOutRawRequest, CreateOrAmendCodingOutRequestBody}
import config.AppConfig
import javax.inject.Inject

class CreateOrAmendCodingOutValidator @Inject()(implicit appConfig: AppConfig) extends Validator[CreateOrAmendCodingOutRawRequest]{

  private val validationSet = List(parameterValidation, parameterRuleValidation, bodyFormatValidation, bodyFieldValidation)

  private def parameterValidation: CreateOrAmendCodingOutRawRequest => List[List[MtdError]] = (data: CreateOrAmendCodingOutRawRequest) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: CreateOrAmendCodingOutRawRequest => List[List[MtdError]] = (data: CreateOrAmendCodingOutRawRequest) => {
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear),
      TaxYearNotEndedValidation.validate(data.taxYear)
    )
  }

  private def bodyFormatValidation: CreateOrAmendCodingOutRawRequest => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[CreateOrAmendCodingOutRequestBody](data.body, RuleIncorrectOrEmptyBodyError)
    )
  }

  private def bodyFieldValidation: CreateOrAmendCodingOutRawRequest => List[List[MtdError]] = (data: CreateOrAmendCodingOutRawRequest) => {
    val body = data.body.as[CreateOrAmendCodingOutRequestBody]

    List(flattenErrors(bodyValidations(body)))
  }

  private def bodyValidations(body: CreateOrAmendCodingOutRequestBody): List[List[MtdError]] = {
    List(
      NumberValidation.validateOptional(
        field = body.payeUnderpayments,
        path = s"/payeUnderpayments"
      ),
      NumberValidation.validateOptional(
        field = body.selfAssessmentUnderPayments,
        path = s"/selfAssessmentUnderPayments"
      ),
      NumberValidation.validateOptional(
        field = body.debts,
        path = s"/debts"
      ),
      NumberValidation.validateOptional(
        field = body.inYearAdjustments,
        path = s"/inYearAdjustments"
      )
    )
  }

  override def validate(data: CreateOrAmendCodingOutRawRequest): List[MtdError] = {
    run(validationSet, data).distinct
  }
}
