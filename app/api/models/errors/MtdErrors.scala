/*
 * Copyright 2022 HM Revenue & Customs
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

package api.models.errors

import play.api.libs.json.{Json, OWrites}

case class MtdError(code: String, message: String, paths: Option[Seq[String]] = None)

object MtdError {
  implicit val writes: OWrites[MtdError] = Json.writes[MtdError]

  implicit def genericWrites[T <: MtdError]: OWrites[T] =
    writes.contramap[T](c => c: MtdError)

}

object MtdErrorWithCustomMessage {
  def unapply(arg: MtdError): Option[String] = Some(arg.code)
}

// Format Errors

object NinoFormatError
    extends MtdError(
      code = "FORMAT_NINO",
      message = "The format of the supplied nino value is invalid"
    )

object TaxYearFormatError
    extends MtdError(
      code = "FORMAT_TAX_YEAR",
      message = "The format of the supplied taxYear value is invalid"
    )

object FromDateFormatError
    extends MtdError(
      code = "FORMAT_FROM_DATE",
      message = "The provided From date is invalid"
    )

object ValueFormatError
    extends MtdError(
      code = "FORMAT_VALUE",
      message = "The value must be between 0.00 and 99999999999.99"
    )

object ToDateFormatError
    extends MtdError(
      code = "FORMAT_TO_DATE",
      message = "The provided To date is invalid"
    )

object PaymentIdFormatError
    extends MtdError(
      code = "FORMAT_PAYMENT_ID",
      message = "The provided payment ID is invalid"
    )

object TransactionIdFormatError
    extends MtdError(
      code = "FORMAT_TRANSACTION_ID",
      message = "The provided transaction ID is invalid"
    )

object IdFormatError
    extends MtdError(
      code = "FORMAT_ID",
      message = "The format of the ID is invalid"
    )

object SourceFormatError
    extends MtdError(
      code = "FORMAT_SOURCE",
      message = "The format of the supplied source is not valid"
    )

object PaymentLotFormatError
  extends MtdError(
    code = "FORMAT_PAYMENT_LOT",
    message = "The provided paymentLot is invalid."
  )

object PaymentLotItemFormatError
  extends MtdError(
    code = "FORMAT_PAYMENT_LOT_ITEM",
    message = "The provided paymentLotItem is invalid."
  )

object DateFromFormatError
  extends MtdError(
    code = "FORMAT_DATE_FROM",
    message = "The provided dateFrom is invalid"
  )

object DateToFormatError
  extends MtdError(
    code = "FORMAT_DATE_TO",
    message = "The provided dateTo is invalid"
  )

// Rule Errors
object RuleTaxYearNotSupportedError
    extends MtdError(
      code = "RULE_TAX_YEAR_NOT_SUPPORTED",
      message = "The specified taxYear is not supported. The taxYear specified is before the minimum tax year value"
    )

object RuleIncorrectOrEmptyBodyError
    extends MtdError(
      code = "RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED",
      message = "An empty or non-matching body was submitted"
    )

object RuleTaxYearRangeInvalidError
    extends MtdError(
      code = "RULE_TAX_YEAR_RANGE_INVALID",
      message = "A taxYear range of one year is required"
    )

object RuleTaxYearNotEndedError
    extends MtdError(
      code = "RULE_TAX_YEAR_NOT_ENDED",
      message = "Tax year not ended"
    )

object RuleDuplicateIdError
    extends MtdError(
      code = "RULE_DUPLICATE_ID_NOT_ALLOWED",
      message = "Duplicate IDs are not allowed"
    )

object RuleFromDateNotSupportedError
    extends MtdError(
      code = "RULE_FROM_DATE_NOT_SUPPORTED",
      message = "The specified from date is not supported as too early"
    )

object RuleDateRangeInvalidError
    extends MtdError(
      code = "RULE_DATE_RANGE_INVALID",
      message = "The specified date range is invalid"
    )

object RuleInconsistentQueryParamsError
  extends MtdError(
    code = "RULE_INCONSISTENT_QUERY_PARAMS",
    message = "Provide date range or docNumber when onlyOpen items are false"
  )

object RuleDateToBeforeDateFromError
  extends MtdError(
    code = "RULE_DATE_TO_BEFORE_DATE_FROM",
    message = "The dateTo cannot be earlier than the dateFrom"
  )

// Date Errors
object MissingFromDateError
    extends MtdError(
      code = "MISSING_FROM_DATE",
      message = "The From date parameter is missing"
    )

object MissingToDateError
    extends MtdError(
      code = "MISSING_TO_DATE",
      message = "The To date parameter is missing"
    )

object RangeToDateBeforeFromDateError
    extends MtdError(
      code = "RANGE_TO_DATE_BEFORE_FROM_DATE",
      message = "The To date must be after the From date"
    )
// Invalid Errors

object InvalidDocNumberError extends MtdError(code = "INVALID_DOC_NUMBER", message = "The provided docNumber is invalid.")

object InvalidOnlyOpenItemsError extends MtdError(code = "INVALID_ONLY_OPEN_ITEMS", message = "The provided onlyOpenItems is invalid.")

object InvalidIncludeLocksError extends MtdError(code = "INVALID_INCLUDE_LOCKS", message = "The provided includeLocks is invalid.")

object InvalidCalculateAccruedInterestError
  extends MtdError(code = "INVALID_CALCULATE_ACCRUED_INTEREST", message = "The provided calculateAccruedInterest is invalid.")

object InvalidCustomerPaymentInformationError
  extends MtdError(code = "INVALID_CUSTOMER_PAYMENT_INFORMATION", message = "The provided customerPaymentInformation is invalid.")

object InvalidDateFromError extends MtdError(code = "INVALID_DATE_FROM", message = "The provided dateFrom is invalid.")

object InvalidDateToError extends MtdError(code = "INVALID_DATE_TO", message = "The provided dateTo is invalid.")

object InvalidRemovePaymentOnAccountError extends MtdError(code = "INVALID_REMOVE_PAYMENT_ON_ACCOUNT", message = "The provided removePOA is invalid.")

object InvalidIncludeChargeEstimateError
  extends MtdError(code = "INVALID_INCLUDE_CHARGE_ESTIMATE", message = "The provided includeChargeEstimate is invalid.")

object InvalidDateRangeError extends MtdError(code = "INVALID_DATE_RANGE", message = "The provided date range is invalid.")

// Standard Errors
object NotFoundError
    extends MtdError(
      code = "MATCHING_RESOURCE_NOT_FOUND",
      message = "Matching resource not found"
    )

object CodingOutNotFoundError
    extends MtdError(
      code = "MATCHING_RESOURCE_NOT_FOUND",
      message = "Coding out amounts could not be found for the supplied nino and taxYear"
    )

object NoTransactionDetailsFoundError
    extends MtdError(
      code = "NO_DETAILS_FOUND",
      message = "No transaction details found"
    )

object BadRequestError
    extends MtdError(
      code = "INVALID_REQUEST",
      message = "Invalid request"
    )

object BVRError
    extends MtdError(
      code = "BUSINESS_ERROR",
      message = "Business validation error"
    )

object ServiceUnavailableError
    extends MtdError(
      code = "SERVICE_UNAVAILABLE",
      message = "Internal server error"
    )

// Authorisation Errors
object UnauthorisedError
    extends MtdError(
      code = "CLIENT_OR_AGENT_NOT_AUTHORISED",
      message = "The client and/or agent is not authorised"
    )

object InvalidBearerTokenError
    extends MtdError(
      code = "UNAUTHORIZED",
      message = "Bearer token is missing or not authorized"
    )

// Accept header Errors
object InvalidAcceptHeaderError
    extends MtdError(
      code = "ACCEPT_HEADER_INVALID",
      message = "The accept header is missing or invalid"
    )

object UnsupportedVersionError
    extends MtdError(
      code = "NOT_FOUND",
      message = "The requested resource could not be found"
    )

object InvalidBodyTypeError
    extends MtdError(
      code = "INVALID_BODY_TYPE",
      message = "Expecting text/json or application/json body"
    )

object InternalError extends MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred")
