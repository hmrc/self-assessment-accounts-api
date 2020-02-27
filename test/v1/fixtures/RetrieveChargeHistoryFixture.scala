/*
 * Copyright 2020 HM Revenue & Customs
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

package v1.fixtures

import v1.models.request.retrieveChargeHistory.RetrieveChargeHistoryRawRequest

object RetrieveChargeHistoryFixture {

  val validNino = "AA123456A"
  val validChargeId = "ABC123"
  val invalidNino = "A12344A"
  val invalidChargeId = "123456789012345678901234567890123456" // too long

  val validRetrieveChargeHistoryRawRequest: RetrieveChargeHistoryRawRequest =
    RetrieveChargeHistoryRawRequest(validNino, validChargeId)
  val invalidRetrieveChargeHistoryRawRequestInvalidNino: RetrieveChargeHistoryRawRequest =
    RetrieveChargeHistoryRawRequest(invalidNino, validChargeId)
  val invalidRetrieveChargeHistoryRawRequestInvalidChargeId: RetrieveChargeHistoryRawRequest =
    RetrieveChargeHistoryRawRequest(validNino, invalidChargeId)
  val invalidRetrieveChargeHistoryRawRequestInvalidNinoAndChargeId: RetrieveChargeHistoryRawRequest =
    RetrieveChargeHistoryRawRequest(invalidNino, invalidChargeId)

}
