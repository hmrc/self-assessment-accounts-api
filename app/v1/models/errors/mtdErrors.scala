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

package v1.models.errors

import play.api.libs.json.{Json, Writes}

case class MtdError(code: String, message: String, paths: Option[Seq[String]] = None)

object MtdError {
  implicit val writes: Writes[MtdError] = Json.writes[MtdError]
}

object MtdErrorWithCustomMessage {
  def unapply(arg: MtdError): Option[String] = Some(arg.code)
}

// Format Errors
object NinoFormatError extends MtdError("FORMAT_NINO", "The format of the supplied nino value is invalid")
object TaxYearFormatError extends MtdError("FORMAT_TAX_YEAR", "The format of the supplied taxYear value is invalid")
object FromDateFormatError extends MtdError("FORMAT_FROM_DATE", "The provided From date is invalid")
object ValueFormatError extends MtdError("FORMAT_VALUE", "The value must be between 0.00 and 99999999999.99")
object ToDateFormatError extends MtdError("FORMAT_TO_DATE", "The provided To date is invalid")
object PaymentIdFormatError extends MtdError("FORMAT_PAYMENT_ID", "The provided payment ID is invalid")
object TransactionIdFormatError extends MtdError("FORMAT_TRANSACTION_ID", "The provided transaction ID is invalid")
object SourceFormatError extends MtdError("FORMAT_SOURCE", "The format of the supplied source is not valid")

// Rule Errors
object RuleTaxYearNotSupportedError extends MtdError(
  code = "RULE_TAX_YEAR_NOT_SUPPORTED",
  message = "The specified taxYear is not supported. The taxYear specified is before the minimum tax year value"
)

object RuleIncorrectOrEmptyBodyError extends MtdError(
  code = "RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED",
  message = "An empty or non-matching body was submitted"
)

object RuleTaxYearRangeInvalidError extends MtdError(
  code = "RULE_TAX_YEAR_RANGE_INVALID",
  message = "A taxYear range of one year is required"
)

object RuleTaxYearNotEndedError extends MtdError(
  code = "RULE_TAX_YEAR_NOT_ENDED",
  message = "Tax year not ended"
)

object RuleFromDateNotSupportedError extends MtdError(
  code = "RULE_FROM_DATE_NOT_SUPPORTED",
  message = "The specified from date is not supported as too early"
)

object RuleDateRangeInvalidError extends MtdError(
  code = "RULE_DATE_RANGE_INVALID",
  message = "The specified date range is invalid"
)

// Date Errors
object MissingFromDateError extends MtdError("MISSING_FROM_DATE", "The From date parameter is missing")
object MissingToDateError extends MtdError("MISSING_TO_DATE", "The To date parameter is missing")
object RangeToDateBeforeFromDateError extends MtdError("RANGE_TO_DATE_BEFORE_FROM_DATE", "The To date must be after the From date")

// Standard Errors
object NotFoundError extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "Matching resource not found")

object CodingOutNotFoundError extends MtdError(
  code = "MATCHING_RESOURCE_NOT_FOUND",
  message = "Coding out amounts could not be found for the supplied nino and taxYear"
)

object NoTransactionDetailsFoundError extends MtdError("NO_DETAILS_FOUND", "No transaction details found")

object DownstreamError extends MtdError("INTERNAL_SERVER_ERROR", "An internal server error occurred")

object BadRequestError extends MtdError("INVALID_REQUEST", "Invalid request")

object BVRError extends MtdError("BUSINESS_ERROR", "Business validation error")

object ServiceUnavailableError extends MtdError("SERVICE_UNAVAILABLE", "Internal server error")

// Authorisation Errors
object UnauthorisedError extends MtdError("CLIENT_OR_AGENT_NOT_AUTHORISED", "The client and/or agent is not authorised")
object InvalidBearerTokenError extends MtdError("UNAUTHORIZED", "Bearer token is missing or not authorized")

// Accept header Errors
object  InvalidAcceptHeaderError extends MtdError("ACCEPT_HEADER_INVALID", "The accept header is missing or invalid")

object  UnsupportedVersionError extends MtdError("NOT_FOUND", "The requested resource could not be found")

object InvalidBodyTypeError extends MtdError("INVALID_BODY_TYPE", "Expecting text/json or application/json body")
