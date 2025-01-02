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

package v3.retrieveBalanceAndTransactions.def1

import cats.data.Validated
import cats.data.Validated.Valid
import cats.implicits._
import common.errors._
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers._
import shared.models.errors._
import v3.retrieveBalanceAndTransactions.model.request.RetrieveBalanceAndTransactionsRequestData
import shared.models.domain.DateRange


import javax.inject.Singleton

@Singleton
class Def1_RetrieveBalanceAndTransactionsValidator(nino: String,
                                                   docNumber: Option[String],
                                                   fromDate: Option[String],
                                                   toDate: Option[String],
                                                   onlyOpenItems: Option[String],
                                                   includeLocks: Option[String],
                                                   calculateAccruedInterest: Option[String],
                                                   removePOA: Option[String],
                                                   customerPaymentInformation: Option[String],
                                                   includeEstimatedCharges: Option[String])
    extends Validator[RetrieveBalanceAndTransactionsRequestData] {

  private val minYear = 1900
  private val maxYear = 2100

  private val resolveDateRange = ResolveDateRange()
    .withYearsLimitedTo(minYear, maxYear)

  def validate: Validated[Seq[MtdError], RetrieveBalanceAndTransactionsRequestData] =
    (
      ResolveNino(nino),
      resolveDocNumber(docNumber),
      validateDateRange(fromDate, toDate) andThen { maybeFromAndTo =>
        maybeFromAndTo
          .map { case (from, to) =>
            resolveDateRange(from -> to)
              .map(Some(_))
          }
          .getOrElse(Valid(None))
      },
      ResolveBoolean.apply(onlyOpenItems, OnlyOpenItemsFormatError),
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
        case _                                                                => invalid(DocNumberFormatError)
      }
      .getOrElse(Valid(None))
  }

  private def validateParameterRules(
      parsed: RetrieveBalanceAndTransactionsRequestData): Validated[Seq[MtdError], RetrieveBalanceAndTransactionsRequestData] = {

    List(
      validateOnlyOpenItems(parsed.onlyOpenItems, parsed.docNumber, parsed.fromAndToDates)
    ).traverse(identity).map(_ => parsed)

  }

  private def validateDateRange(fromDate: Option[String], toDate: Option[String]): Validated[Seq[MtdError], Option[(String, String)]] = {
    (fromDate, toDate) match {
      case (None, None)           => Valid(None)
      case (Some(from), Some(to)) => Valid(Some((from, to)))
      case (Some(_), None)        => invalid(RuleMissingToDateError)
      case (None, Some(_))        => invalid(MissingFromDateError)
    }
  }

  private def validateOnlyOpenItems(onlyOpenItems: Boolean,
                                    docNumber: Option[String],
                                    fromAndToDates: Option[DateRange]): Validated[Seq[MtdError], Unit] = {
    val otherQueryParamsDefined = docNumber.isDefined || fromAndToDates.isDefined

    if (onlyOpenItems && otherQueryParamsDefined)
      invalid(RuleInconsistentQueryParamsError)
    else
      Valid(())
  }

}
