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

package common.errors

import play.api.http.Status.{BAD_REQUEST, NOT_FOUND}
import shared.models.errors.MtdError

// MtdError types that are common to SA Accounts API.

// Format Errors
object SourceFormatError extends MtdError(code = "FORMAT_SOURCE", message = "The format of the supplied source is not valid", BAD_REQUEST)

object PaymentLotFormatError extends MtdError(code = "FORMAT_PAYMENT_LOT", message = "The provided paymentLot value is invalid", BAD_REQUEST)

object PaymentLotItemFormatError
    extends MtdError(code = "FORMAT_PAYMENT_LOT_ITEM", message = "The provided paymentLotItem value is invalid", BAD_REQUEST)

object DocNumberFormatError extends MtdError(code = "FORMAT_DOC_NUMBER", message = "The provided docNumber is invalid", BAD_REQUEST)

object OnlyOpenItemsFormatError extends MtdError(code = "FORMAT_ONLY_OPEN_ITEMS", message = "The provided onlyOpenItems is invalid", BAD_REQUEST)

object IncludeLocksFormatError extends MtdError(code = "FORMAT_INCLUDE_LOCKS", message = "The provided includeLocks is invalid", BAD_REQUEST)

object CalculateAccruedInterestFormatError
  extends MtdError(code = "FORMAT_CALCULATE_ACCRUED_INTEREST", message = "The provided calculateAccruedInterest is invalid", BAD_REQUEST)

object CustomerPaymentInformationFormatError
  extends MtdError(code = "FORMAT_CUSTOMER_PAYMENT_INFORMATION", message = "The provided customerPaymentInformation is invalid", BAD_REQUEST)

object RemovePaymentOnAccountFormatError
  extends MtdError(code = "FORMAT_REMOVE_PAYMENT_ON_ACCOUNT", message = "The provided removePOA is invalid", BAD_REQUEST)

object IncludeEstimatedChargesFormatError
  extends MtdError(code = "FORMAT_INCLUDE_ESTIMATED_CHARGES", message = "The provided includeEstimatedCharges is invalid", BAD_REQUEST)

object ChargeReferenceFormatError
  extends MtdError(code = "FORMAT_CHARGE_REFERENCE", message = "The provided charge reference is invalid", BAD_REQUEST)

// Rule Errors
object MissingPaymentLotError
  extends MtdError(code = "MISSING_PAYMENT_LOT", message = "The paymentLotItem has been provided, but the paymentLot is missing", BAD_REQUEST)

object MissingPaymentLotItemError
  extends MtdError(code = "MISSING_PAYMENT_LOT_ITEM", message = "The paymentLot has been provided, but the paymentLotItem is missing", BAD_REQUEST)

object RuleInconsistentQueryParamsError
    extends MtdError(code = "RULE_INCONSISTENT_QUERY_PARAMS", message = "Provide date range or docNumber when onlyOpenItems is false", BAD_REQUEST)

object RuleInconsistentQueryParamsErrorListSA
    extends MtdError(
      code = "RULE_INCONSISTENT_QUERY_PARAMS",
      message = "Provide either paymentLot & paymentLotItem or fromDate & toDate",
      BAD_REQUEST)

object RuleBusinessPartnerNotExistError
  extends MtdError("RULE_BUSINESS_PARTNER_NOT_EXIST", "Provided NINO is not registered as business partner", BAD_REQUEST)

object RuleItsaContractObjectNotExistError extends MtdError("RULE_ITSA_CONTRACT_OBJECT_NOT_EXIST", "ITSA contract object does not exist", BAD_REQUEST)

object RuleAlreadyOptedInError extends MtdError("RULE_ALREADY_OPTED_IN", "Customer is already opted in to coding out", BAD_REQUEST)

object RuleAlreadyOptedOutError extends MtdError("RULE_ALREADY_OPTED_OUT", "Customer is already opted out of coding out", BAD_REQUEST)

object RuleOutsideAmendmentWindowError extends MtdError("RULE_OUTSIDE_AMENDMENT_WINDOW", "You are outside the amendment window", BAD_REQUEST)

// Other Errors
object CodingOutNotFoundError
  extends MtdError("MATCHING_RESOURCE_NOT_FOUND", "Coded out amounts could not be found for the supplied nino and taxYear", NOT_FOUND)
