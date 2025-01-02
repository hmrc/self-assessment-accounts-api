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

package v3.common.errors

import play.api.http.Status.BAD_REQUEST
import shared.models.errors.MtdError

object RuleBusinessPartnerNotExistError
    extends MtdError("RULE_BUSINESS_PARTNER_NOT_EXIST", "Provided NINO is not registered as business partner", BAD_REQUEST)

object RuleItsaContractObjectNotExistError extends MtdError("RULE_ITSA_CONTRACT_OBJECT_NOT_EXIST", "ITSA contract object does not exist", BAD_REQUEST)

object RuleAlreadyOptedInError extends MtdError("RULE_ALREADY_OPTED_IN", "Customer is already opted in to coding out", BAD_REQUEST)

object RuleAlreadyOptedOutError extends MtdError("RULE_ALREADY_OPTED_OUT", "Customer is already opted out of coding out", BAD_REQUEST)
