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

package common.hateoas

import common.hateoas.RelType._
import shared.config.SharedAppConfig
import shared.hateoas.Link
import shared.hateoas.Method.{DELETE, GET, PUT}

trait HateoasLinks {

  // Payments and Liabilities links

  // L1
  def retrieveTransactionDetails(appConfig: SharedAppConfig, nino: String, transactionId: String, isSelf: Boolean): Link =
    Link(
      href = s"/${appConfig.apiGatewayContext}/$nino/transactions/$transactionId",
      method = GET,
      rel = if (isSelf) SELF else RETRIEVE_TRANSACTION_DETAILS
    )

  // L2
  def retrieveChargeHistory(appConfig: SharedAppConfig, nino: String, transactionId: String, isSelf: Boolean): Link =
    Link(
      href = s"/${appConfig.apiGatewayContext}/$nino/charges/$transactionId",
      method = GET,
      rel = if (isSelf) SELF else RETRIEVE_CHARGE_HISTORY
    )

  // Coding Out Underpayments and Debts links

  // L1
  def createOrAmendCodingOut(appConfig: SharedAppConfig, nino: String, taxYear: String): Link =
    Link(
      href = s"/${appConfig.apiGatewayContext}/$nino/$taxYear/collection/tax-code",
      method = PUT,
      rel = CREATE_OR_AMEND_CODING_OUT_UNDERPAYMENTS
    )

  // L2
  def retrieveCodingOut(appConfig: SharedAppConfig, nino: String, taxYear: String): Link =
    Link(
      href = s"/${appConfig.apiGatewayContext}/$nino/$taxYear/collection/tax-code",
      method = GET,
      rel = SELF
    )

  // L3
  def deleteCodingOut(appConfig: SharedAppConfig, nino: String, taxYear: String): Link =
    Link(
      href = s"/${appConfig.apiGatewayContext}/$nino/$taxYear/collection/tax-code",
      method = DELETE,
      rel = DELETE_CODING_OUT_UNDERPAYMENTS
    )

}
