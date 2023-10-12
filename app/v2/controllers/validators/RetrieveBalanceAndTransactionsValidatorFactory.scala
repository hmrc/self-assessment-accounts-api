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
import api.controllers.validators.resolvers.{ResolveBoolean, ResolveIsoDate, ResolveNino}
import api.models.errors._
import cats.data.Validated
import cats.data.Validated.{Invalid, Valid}
import cats.implicits._
import v2.models.request.retrieveBalanceAndTransactions.RetrieveBalanceAndTransactionsRequestData

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Singleton

@Singleton
class RetrieveBalanceAndTransactionsValidatorFactory {

  private val minYear                       = 1900
  private val maxYear                       = 2100
  private val dateFormat: DateTimeFormatter = DateTimeFormatter ofPattern "yyyy-MM-dd"

  def validator(nino: String,
                docNumber: Option[String],
                fromDate: Option[String],
                toDate: Option[String],
                onlyOpenItems: Option[String],
                includeLocks: Option[String],
                calculateAccruedInterest: Option[String],
                removePOA: Option[String],
                customerPaymentInformation: Option[String],
                includeEstimatedCharges: Option[String]): Validator[RetrieveBalanceAndTransactionsRequestData] =
    new Validator[RetrieveBalanceAndTransactionsRequestData] {

      def validate: Validated[Seq[MtdError], RetrieveBalanceAndTransactionsRequestData] =
        (
          ResolveNino(nino),
          resolveDocNumber(docNumber),
          validateFromDate(fromDate),
          validateToDate(toDate),
          ResolveBoolean(onlyOpenItems, defaultValue = false, OnlyOpenItemsFormatError),
          ResolveBoolean(includeLocks, defaultValue = false, IncludeLocksFormatError),
          ResolveBoolean(calculateAccruedInterest, defaultValue = false, CalculateAccruedInterestFormatError),
          ResolveBoolean(removePOA, defaultValue = false, RemovePaymentOnAccountFormatError),
          ResolveBoolean(customerPaymentInformation, defaultValue = false, CustomerPaymentInformationFormatError),
          ResolveBoolean(includeEstimatedCharges, defaultValue = false, IncludeEstimatedChargesFormatError)
        ).mapN(RetrieveBalanceAndTransactionsRequestData) andThen validateParameterRules

      private def resolveDocNumber(docNumber: Option[String]): Validated[Seq[MtdError], Option[String]] = {
        val MAX_LENGTH = 12
        docNumber
          .map {
            case docNumber if docNumber.nonEmpty && docNumber.length < MAX_LENGTH => Valid(Some(docNumber))
            case _                                                                => Invalid(List(DocNumberFormatError))
          }
          .getOrElse(Valid(None))
      }

      private def validateFromDate(fromDate: Option[String]): Validated[Seq[MtdError], Option[String]] = {
        def checkMinYear(maybeParsedFromDate: Option[LocalDate]): Validated[Seq[MtdError], Option[String]] =
          maybeParsedFromDate
            .map {
              case parsedFromDate if parsedFromDate.getYear <= minYear => Invalid(List(FromDateFormatError))
              case _                                                   => Valid(fromDate)
            }
            .getOrElse(Valid(None))

        ResolveIsoDate(fromDate, FromDateFormatError) andThen checkMinYear
      }

      private def validateToDate(toDate: Option[String]): Validated[Seq[MtdError], Option[String]] = {
        def checkMaxYear(maybeParsedToDate: Option[LocalDate]): Validated[Seq[MtdError], Option[String]] =
          maybeParsedToDate
            .map {
              case parsedToDate if parsedToDate.getYear >= maxYear => Invalid(List(ToDateFormatError))
              case _                                               => Valid(toDate)
            }
            .getOrElse(Valid(None))

        ResolveIsoDate(toDate, ToDateFormatError) andThen checkMaxYear
      }

    }

  private def validateParameterRules(
      parsed: RetrieveBalanceAndTransactionsRequestData): Validated[Seq[MtdError], RetrieveBalanceAndTransactionsRequestData] = {
    import parsed._

    List(
      validateDateRange(fromDate, toDate),
      validateOnlyOpenItems(onlyOpenItems, docNumber, toDate, fromDate)
    ).traverse(identity).map(_ => parsed)

  }

  private def validateDateRange(fromDate: Option[String], toDate: Option[String]): Validated[Seq[MtdError], Unit] = {
    (fromDate, toDate) match {
      case (Some(f), Some(t)) => checkIfToDateIsBeforeFromDate(f, t)
      case (Some(_), None)    => Invalid(List(RuleMissingToDateError))
      case (None, Some(_))    => Invalid(List(MissingFromDateError))
      case (None, None)       => Valid(())
    }
  }

  private def checkIfToDateIsBeforeFromDate(fromDate: String, toDate: String): Validated[Seq[MtdError], Unit] = {
    val fmtFrom = LocalDate.parse(fromDate, dateFormat)
    val fmtTo   = LocalDate.parse(toDate, dateFormat)
    if (fmtTo isBefore fmtFrom) Invalid(List(RangeToDateBeforeFromDateError)) else Valid(())
  }

  private def validateOnlyOpenItems(onlyOpenItems: Boolean,
                                    docNumber: Option[String],
                                    toDate: Option[String],
                                    fromDate: Option[String]): Validated[Seq[MtdError], Unit] = {
    val otherQueryParamsDefined = (docNumber.isDefined || toDate.isDefined) || fromDate.isDefined

    if (onlyOpenItems && otherQueryParamsDefined)
      Invalid(List(RuleInconsistentQueryParamsError))
    else Valid(())
  }

}
