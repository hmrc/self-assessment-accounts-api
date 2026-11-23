/*
 * Copyright 2026 HM Revenue & Customs
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

package v4.retrieveChargeHistoryByTransactionId

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveNino, ResolveTransactionId}
import api.models.errors.MtdError
import cats.data.Validated
import cats.implicits.catsSyntaxTuple2Semigroupal
import v4.retrieveChargeHistoryByTransactionId.model.request.RetrieveChargeHistoryByTransactionIdRequestData

import javax.inject.Singleton

@Singleton
class RetrieveChargeHistoryByTransactionIdValidator(
    nino: String,
    transactionId: String
) extends Validator[RetrieveChargeHistoryByTransactionIdRequestData] {

  def validate: Validated[Seq[MtdError], RetrieveChargeHistoryByTransactionIdRequestData] =
    (
      ResolveNino(nino),
      ResolveTransactionId(transactionId)
    ).mapN(RetrieveChargeHistoryByTransactionIdRequestData.apply)

}
