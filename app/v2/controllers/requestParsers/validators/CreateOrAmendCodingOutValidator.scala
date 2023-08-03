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

package v2.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.Validator
import api.controllers.requestParsers.validators.validations._
import api.models.errors.{MtdError, RuleIncorrectOrEmptyBodyError}
import config.AppConfig
import utils.CurrentDate
import v2.models.request.createOrAmendCodingOut.{CreateOrAmendCodingOutRawRequest, CreateOrAmendCodingOutRequestBody, TaxCodeComponent}

import javax.inject.Inject

class CreateOrAmendCodingOutValidator @Inject()(implicit currentDate: CurrentDate, appConfig: AppConfig)
    extends Validator[CreateOrAmendCodingOutRawRequest] {

  private val validationSet = List(parameterValidation, parameterRuleValidation, bodyFormatValidation, bodyFieldsEmptyValidation, bodyFieldValidation)

  private def parameterValidation: CreateOrAmendCodingOutRawRequest => List[List[MtdError]] = (data: CreateOrAmendCodingOutRawRequest) => {
    List(
      NinoValidation.validate(data.nino),
      TaxYearValidation.validate(data.taxYear)
    )
  }

  private def parameterRuleValidation: CreateOrAmendCodingOutRawRequest => List[List[MtdError]] = (data: CreateOrAmendCodingOutRawRequest) => {
    List(
      TaxYearNotSupportedValidation.validate(data.taxYear),
      if (data.temporalValidationEnabled) TaxYearNotEndedValidation.validate(data.taxYear) else Nil
    )
  }

  private def bodyFormatValidation: CreateOrAmendCodingOutRawRequest => List[List[MtdError]] = { data =>
    List(
      JsonFormatValidation.validate[CreateOrAmendCodingOutRequestBody](data.body)
    )
  }

  private def bodyFieldsEmptyValidation: CreateOrAmendCodingOutRawRequest => List[List[MtdError]] = (data: CreateOrAmendCodingOutRawRequest) => {
    val body = data.body.as[CreateOrAmendCodingOutRequestBody]

    if (body.emptyFields.nonEmpty) {
      List(List(RuleIncorrectOrEmptyBodyError.copy(paths = Some(body.emptyFields))))
    } else {
      NoValidationErrors
    }
  }

  private def bodyFieldValidation: CreateOrAmendCodingOutRawRequest => List[List[MtdError]] = (data: CreateOrAmendCodingOutRawRequest) => {
    val body = data.body.as[CreateOrAmendCodingOutRequestBody]

    List(flattenErrors(bodyValidations(body)))
  }

  private def bodyValidations(body: CreateOrAmendCodingOutRequestBody): List[List[MtdError]] = {
    val payeUnderpaymentErrors: List[List[MtdError]] = body.taxCodeComponents.payeUnderpayment
      .map(_.zipWithIndex.map { case (component, i) =>
        getPayeUnderpaymentErrors(component, i)
      })
      .getOrElse(NoValidationErrors)
      .toList
    val selfAssessmentUnderpaymentErrors: List[List[MtdError]] = body.taxCodeComponents.selfAssessmentUnderpayment
      .map(_.zipWithIndex.map { case (component, i) =>
        getSelfAssessmentUnderpaymentErrors(component, i)
      })
      .getOrElse(NoValidationErrors)
      .toList
    val debtErrors: List[List[MtdError]] = body.taxCodeComponents.debt
      .map(_.zipWithIndex.map { case (component, i) =>
        getDebtErrors(component, i)
      })
      .getOrElse(NoValidationErrors)
      .toList
    val inYearAdjustmentErrors: List[List[MtdError]] = List(
      body.taxCodeComponents.inYearAdjustment.map(getInYearAdjustmentErrors).getOrElse(NoValidationErrors))

    payeUnderpaymentErrors ++ selfAssessmentUnderpaymentErrors ++ debtErrors ++ inYearAdjustmentErrors
  }

  private def getPayeUnderpaymentErrors(taxCodeComponent: TaxCodeComponent, i: Int): List[MtdError] = {
    List(
      NumberValidation.validateOptional(
        field = Some(taxCodeComponent.amount),
        path = s"/taxCodeComponents/payeUnderpayment/$i/amount"
      ),
      IdValidation.validate(
        field = taxCodeComponent.id,
        path = s"/taxCodeComponents/payeUnderpayment/$i/id"
      )
    ).flatten
  }

  private def getSelfAssessmentUnderpaymentErrors(taxCodeComponent: TaxCodeComponent, i: Int): List[MtdError] = {
    List(
      NumberValidation.validateOptional(
        field = Some(taxCodeComponent.amount),
        path = s"/taxCodeComponents/selfAssessmentUnderpayment/$i/amount"
      ),
      IdValidation.validate(
        field = taxCodeComponent.id,
        path = s"/taxCodeComponents/selfAssessmentUnderpayment/$i/id"
      )
    ).flatten
  }

  private def getDebtErrors(taxCodeComponent: TaxCodeComponent, i: Int): List[MtdError] = {
    List(
      NumberValidation.validateOptional(
        field = Some(taxCodeComponent.amount),
        path = s"/taxCodeComponents/debt/$i/amount"
      ),
      IdValidation.validate(
        field = taxCodeComponent.id,
        path = s"/taxCodeComponents/debt/$i/id"
      )
    ).flatten
  }

  private def getInYearAdjustmentErrors(taxCodeComponent: TaxCodeComponent): List[MtdError] = {
    List(
      NumberValidation.validateOptional(
        field = Some(taxCodeComponent.amount),
        path = "/taxCodeComponents/inYearAdjustment/amount"
      ),
      IdValidation.validate(
        field = taxCodeComponent.id,
        path = "/taxCodeComponents/inYearAdjustment/id"
      )
    ).flatten
  }

  override def validate(data: CreateOrAmendCodingOutRawRequest): List[MtdError] = {
    run(validationSet, data).distinct
  }

}
