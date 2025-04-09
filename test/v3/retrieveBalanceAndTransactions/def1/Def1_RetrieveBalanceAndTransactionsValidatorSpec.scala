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

import common.errors._
import shared.models.errors._
import shared.utils.UnitSpec
import v3.retrieveBalanceAndTransactions.def1.model.RequestFixture._
import v3.retrieveBalanceAndTransactions.model.request.RetrieveBalanceAndTransactionsRequestData

class Def1_RetrieveBalanceAndTransactionsValidatorSpec extends UnitSpec {

  private implicit val correlationId: String = "1234"
  private val validNino: String              = "AA123456A"

  private def validator(nino: String,
                        docNumber: Option[String],
                        fromDate: Option[String],
                        toDate: Option[String],
                        onlyOpenItems: Option[String],
                        includeLocks: Option[String],
                        calculateAccruedInterest: Option[String],
                        removePOA: Option[String],
                        customerPaymentInformation: Option[String],
                        includeEstimatedCharges: Option[String]) = new Def1_RetrieveBalanceAndTransactionsValidator(
    nino,
    docNumber,
    fromDate,
    toDate,
    onlyOpenItems,
    includeLocks,
    calculateAccruedInterest,
    removePOA,
    customerPaymentInformation,
    includeEstimatedCharges)

  "validator" should {
    "return the parsed domain object" when {
      "a valid request is made" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, Some(validDocNumber), None, None, None, None, None, None, None, None).validateAndWrapResult()

        result shouldBe Right(
          requestDocNumber
        )
      }

      "a valid request with date range is supplied" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, None, Some(validFromDate), Some(validToDate), None, None, None, None, None, None).validateAndWrapResult()

        result shouldBe Right(
          requestDateRange
        )
      }

      "a valid request with the same date is supplied" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, None, Some(validFromDate), Some(validFromDate), None, None, None, None, None, None).validateAndWrapResult()

        result shouldBe Right(
          requestSameDateRange
        )
      }
    }

    "return a parameter error" when {
      "an invalid nino is supplied" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator("nino", None, None, None, None, None, None, None, None, None).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, NinoFormatError)
        )
      }

      "an invalid doc number is supplied" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, docNumber = Some("a" * 13), None, None, None, None, None, None, None, None).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, DocNumberFormatError)
        )
      }

      "an invalid only open items is supplied" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, None, None, None, onlyOpenItems = Some("invalid"), None, None, None, None, None).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, OnlyOpenItemsFormatError)
        )
      }

      "an invalid include locks is supplied" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, None, None, None, None, includeLocks = Some("invalid"), None, None, None, None).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, IncludeLocksFormatError)
        )
      }

      "an invalid calculate accrued interest is supplied" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, None, None, None, None, None, calculateAccruedInterest = Some("invalid"), None, None, None).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, CalculateAccruedInterestFormatError)
        )
      }

      "an invalid customer payment information is supplied" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, None, None, None, None, None, None, None, customerPaymentInformation = Some("invalid"), None).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, CustomerPaymentInformationFormatError)
        )
      }

      "an invalid from date is supplied" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, None, fromDate = Some("invalid"), toDate = Some(validToDate), None, None, None, None, None, None).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, FromDateFormatError)
        )
      }

      "a from date before 1900 is supplied" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, None, fromDate = Some("1899-01-21"), Some("1900-01-21"), None, None, None, None, None, None).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, FromDateFormatError)
        )
      }

      "an invalid to date is supplied" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, None, Some(validFromDate), toDate = Some("invalid"), None, None, None, None, None, None).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, ToDateFormatError)
        )
      }

      "a to date after 2100 is supplied" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, None, Some("2099-01-21"), toDate = Some("2100-01-21"), None, None, None, None, None, None).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, ToDateFormatError)
        )
      }

      "dates that exceed the maximum allowable range are supplied" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, None, Some(validFromDate), Some("2025-08-15"), None, None, None, None, None, None).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleInvalidDateRangeError)
        )
      }

      "dates that exceed the maximum allowable range and are outside of the allowable years are supplied" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, None, Some("1899-01-21"), Some("2100-01-21"), None, None, None, None, None, None).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, BadRequestError, Some(List(FromDateFormatError, ToDateFormatError, RuleInvalidDateRangeError)))
        )
      }

      "an invalid remove POA is supplied" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, None, None, None, None, None, None, removePOA = Some("invalid"), None, None).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RemovePaymentOnAccountFormatError)
        )
      }

      "an invalid include charge estimate is supplied" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, None, None, None, None, None, None, None, None, includeEstimatedCharges = Some("invalid")).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, IncludeEstimatedChargesFormatError)
        )
      }

      "to date is supplied but from date is not" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, None, fromDate = None, toDate = Some("2022-11-01"), None, None, None, None, None, None).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, MissingFromDateError)
        )
      }

      "from date is supplied but to date is not" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, None, fromDate = Some("2022-12-01"), toDate = None, None, None, None, None, None, None).validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleMissingToDateError)
        )
      }

      "from date is later than to date" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, None, fromDate = Some("2022-12-01"), toDate = Some("2022-11-01"), None, None, None, None, None, None)
            .validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RangeToDateBeforeFromDateError)
        )
      }

      "multiple invalid values are supplied" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, None, fromDate = Some("invalid"), Some(validToDate), None, None, None, removePOA = Some("invalid"), None, None)
            .validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, BadRequestError, Some(List(FromDateFormatError, RemovePaymentOnAccountFormatError)))
        )
      }

      "a request where docNumber is provided and onlyOpenItems is true" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, Some(validDocNumber), None, None, Some("true"), None, None, None, None, None)
            .validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleInconsistentQueryParamsError)
        )
      }

      "a request where valid dates are provided and onlyOpenItems is true" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, None, Some(validFromDate), Some(validToDate), Some("true"), None, None, None, None, None)
            .validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleInconsistentQueryParamsError)
        )
      }

//      TODO
//      "a request where docNumber and valid dates are provided when onlyOpenItems is false" in {
//        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
//          validator(validNino, Some(validDocNumber), Some(validFromDate), Some(validToDate), None, None, None, None, None, None)
//            .validateAndWrapResult()
//
//        result shouldBe Left(
//          ErrorWrapper(correlationId, RuleInconsistentQueryParamsError)
//        )
//      }

      "a request with everything true is supplied" in {
        val result: Either[ErrorWrapper, RetrieveBalanceAndTransactionsRequestData] =
          validator(validNino, Some(validDocNumber), None, None, Some("true"), Some("true"), Some("true"), Some("true"), Some("true"), Some("true"))
            .validateAndWrapResult()

        result shouldBe Left(
          ErrorWrapper(correlationId, RuleInconsistentQueryParamsError)
        )
      }
    }
  }

}
