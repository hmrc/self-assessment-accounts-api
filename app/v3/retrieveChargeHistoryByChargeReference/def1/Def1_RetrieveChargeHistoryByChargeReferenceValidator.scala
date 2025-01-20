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

package v3.retrieveChargeHistoryByChargeReference.def1

import cats.data.Validated
import cats.implicits.catsSyntaxTuple2Semigroupal
import common.resolvers.ResolveChargeReference
import shared.controllers.validators.Validator
import shared.controllers.validators.resolvers.ResolveNino
import shared.models.errors.MtdError
import v3.retrieveChargeHistoryByChargeReference.def1.model.request.Def1_RetrieveChargeHistoryByChargeReferenceRequestData
import v3.retrieveChargeHistoryByChargeReference.model.request.RetrieveChargeHistoryByChargeReferenceRequestData

import javax.inject.Singleton

@Singleton
class Def1_RetrieveChargeHistoryByChargeReferenceValidator(
                                                            nino: String,
                                                            chargeReference: String
                                                          ) extends Validator[RetrieveChargeHistoryByChargeReferenceRequestData]  {

      def validate: Validated[Seq[MtdError], RetrieveChargeHistoryByChargeReferenceRequestData] =
        (
          ResolveNino(nino),
          ResolveChargeReference(chargeReference)
        ).mapN(Def1_RetrieveChargeHistoryByChargeReferenceRequestData)

}
