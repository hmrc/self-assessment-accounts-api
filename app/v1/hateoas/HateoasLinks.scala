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

package v1.hateoas

import config.AppConfig
import v1.models.hateoas.Link
import v1.models.hateoas.Method._
import v1.models.hateoas.RelType._

trait HateoasLinks {

  // Payments and Liabilities links

  // L1
  def retrieveBalance(appConfig: AppConfig, nino: String, isSelf: Boolean): Link =
    Link(
      href = s"/${appConfig.apiGatewayContext}/$nino/balance",
      method = GET,
      rel = if (isSelf) SELF else RETRIEVE_BALANCE
    )

  // L2
  def listTransactions(appConfig: AppConfig, nino: String, from: String, to: String, isSelf: Boolean): Link =
    Link(
      href = s"/${appConfig.apiGatewayContext}/$nino/transactions?from=$from&to=$to",
      method = GET,
      rel = if (isSelf) SELF else LIST_TRANSACTIONS
    )

  // L3
  def retrieveTransactionDetails(appConfig: AppConfig, nino: String, transactionId: String, isSelf: Boolean): Link =
    Link(
      href = s"/${appConfig.apiGatewayContext}/$nino/transactions/$transactionId",
      method = GET,
      rel = if (isSelf) SELF else RETRIEVE_TRANSACTION_DETAILS
    )

  // L4
  def listCharges(appConfig: AppConfig, nino: String, from: String, to: String, isSelf: Boolean): Link =
    Link(
      href = s"/${appConfig.apiGatewayContext}/$nino/charges?from=$from&to=$to",
      method = GET,
      rel = if (isSelf) SELF else LIST_CHARGES
    )

  // L5
  def retrieveChargeHistory(appConfig: AppConfig, nino: String, transactionId: String, isSelf: Boolean): Link =
    Link(
      href = s"/${appConfig.apiGatewayContext}/$nino/charges/$transactionId",
      method = GET,
      rel = if (isSelf) SELF else RETRIEVE_CHARGE_HISTORY
    )

  // L6
  def listPayments(appConfig: AppConfig, nino: String, from: String, to: String, isSelf: Boolean): Link =
    Link(
      href = s"/${appConfig.apiGatewayContext}/$nino/payments?from=$from&to=$to",
      method = GET,
      rel = if (isSelf) SELF else LIST_PAYMENTS
    )

  // L7
  def retrievePaymentAllocations(appConfig: AppConfig, nino: String, paymentId: String, isSelf: Boolean): Link =
    Link(
      href = s"/${appConfig.apiGatewayContext}/$nino/payments/$paymentId",
      method = GET,
      rel = if (isSelf) SELF else RETRIEVE_PAYMENT_ALLOCATIONS
    )

  // Coding Out Underpayments and Debts links

  // L1
  def createOrAmendCodingOut(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = s"/${appConfig.apiGatewayContext}/$nino/$taxYear/collection/tax-code",
      method = PUT,
      rel = CREATE_OR_AMEND_CODING_OUT_UNDERPAYMENTS
    )

  // L2
  def retrieveCodingOut(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = s"/${appConfig.apiGatewayContext}/$nino/$taxYear/collection/tax-code",
      method = GET,
      rel = SELF
    )

  // L3
  def deleteCodingOut(appConfig: AppConfig, nino: String, taxYear: String): Link =
    Link(
      href = s"/${appConfig.apiGatewayContext}/$nino/$taxYear/collection/tax-code",
      method = DELETE,
      rel = DELETE_CODING_OUT_UNDERPAYMENTS
    )

}
