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

package v4.createOrAmendCodingOut

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.*
import api.controllers.validators.resolvers.ResolveTaxYear.ResolveDetailedTaxYear
import api.models.domain.TaxYear
import api.models.errors.MtdError
import cats.data.Validated
import cats.data.Validated.Valid
import cats.implicits.*
import common.resolvers.ResolveParsedNumericId
import config.SaAccountsConfig
import play.api.libs.json.JsValue
import v4.createOrAmendCodingOut.model.request.{CreateOrAmendCodingOutRequestBody, CreateOrAmendCodingOutRequestData, TaxCodeComponent}

import javax.inject.Singleton

@Singleton
class CreateOrAmendCodingOutValidator(nino: String, taxYear: String, body: JsValue, temporalValidationEnabled: Boolean, appConfig: SaAccountsConfig)
    extends Validator[CreateOrAmendCodingOutRequestData] {

  private val resolveJson = new ResolveNonEmptyJsonObject[CreateOrAmendCodingOutRequestBody]()

  private val validatePayeUnderpayments = ResolveParsedNumber()

  private val resolveTaxYear =
    ResolveDetailedTaxYear(
      minimumTaxYear = TaxYear.ending(appConfig.minimumPermittedTaxYear),
      allowIncompleteTaxYear = !temporalValidationEnabled
    )

  def validate: Validated[Seq[MtdError], CreateOrAmendCodingOutRequestData] =
    (
      ResolveNino(nino),
      resolveTaxYear(taxYear),
      resolveJson(body)
    ).mapN(CreateOrAmendCodingOutRequestData.apply) andThen validatedParsedBody

  private def validatedParsedBody(parsed: CreateOrAmendCodingOutRequestData): Validated[Seq[MtdError], CreateOrAmendCodingOutRequestData] = {

    def validateTaxCodeComponents(maybeComponents: Option[Seq[TaxCodeComponent]], subPath: String): Validated[Seq[MtdError], Unit] =
      maybeComponents match {
        case Some(components) =>
          components.zipWithIndex.traverse_ { case (component, i) =>
            combine(
              validatePayeUnderpayments(component.amount, path = s"/taxCodeComponents/$subPath/$i/amount"),
              ResolveParsedNumericId(component.id, path = s"/taxCodeComponents/$subPath/$i/id")
            )
          }

        case None =>
          Valid(())
      }

    def validateTaxCodeComponent(maybeComponent: Option[TaxCodeComponent], subPath: String): Validated[Seq[MtdError], Unit] =
      maybeComponent match {
        case Some(component) =>
          combine(
            validatePayeUnderpayments(component.amount, path = s"/taxCodeComponents/$subPath/amount"),
            ResolveParsedNumericId(component.id, path = s"/taxCodeComponents/$subPath/id")
          )

        case None =>
          Valid(())
      }

    import parsed.body.taxCodeComponents.*

    combine(
      validateTaxCodeComponents(payeUnderpayment, "payeUnderpayment"),
      validateTaxCodeComponents(selfAssessmentUnderpayment, "selfAssessmentUnderpayment"),
      validateTaxCodeComponents(debt, "debt"),
      validateTaxCodeComponent(inYearAdjustment, "inYearAdjustment")
    ).map(_ => parsed)
  }

}
