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

object RelType {
  val SELF = "self"

  val RETRIEVE_TRANSACTION_DETAILS = "retrieve-transaction-details"
  val RETRIEVE_CHARGE_HISTORY      = "retrieve-charge-history"

  val CREATE_OR_AMEND_CODING_OUT_UNDERPAYMENTS = "create-or-amend-coding-out-underpayments"
  val DELETE_CODING_OUT_UNDERPAYMENTS          = "delete-coding-out-underpayments"
}
