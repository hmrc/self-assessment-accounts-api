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

package v2.controllers.requestParsers

import api.controllers.requestParsers.RequestParser
import v2.controllers.requestParsers.validators.RetrieveSelfAssessmentChargeHistoryValidator

import javax.inject.Inject
import api.models.domain.Nino
import v2.models.request.retrieveSelfAssessmentChargeHistory.{RetrieveSelfAssessmentChargeHistoryRawData, RetrieveSelfAssessmentChargeHistoryRequest}

class RetrieveSelfAssessmentChargeHistoryRequestParser @Inject() (val validator: RetrieveSelfAssessmentChargeHistoryValidator)
    extends RequestParser[RetrieveSelfAssessmentChargeHistoryRawData, RetrieveSelfAssessmentChargeHistoryRequest] {

  override protected def requestFor(data: RetrieveSelfAssessmentChargeHistoryRawData): RetrieveSelfAssessmentChargeHistoryRequest =
    RetrieveSelfAssessmentChargeHistoryRequest(Nino(data.nino), data.transactionId)

}
